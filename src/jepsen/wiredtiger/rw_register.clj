(ns jepsen.wiredtiger.rw-register
  "Elle rw register workload"
  (:require [clojure [pprint :refer [pprint]]]
            [clojure.tools.logging :refer [info warn]]
            [dom-top.core :refer [with-retry]]
            [jepsen [client :as client]
             [checker :as checker]
             [util :as util :refer [timeout]]]
            [jepsen.tests.cycle :as cycle]
            [jepsen.tests.cycle.wr :as rw-register]
            [jepsen.wiredtiger [client :as c]]
            [slingshot.slingshot :as slingshot]
            [jepsen.generator :as gen])
  (:import (java.util.concurrent TimeUnit)
           (com.wiredtiger.db wiredtiger)))



(defn apply-mop!
  "Applies a transactional micro-operation to a connection."
  [test session [f k v :as mop]]
  (with-open [cursor  (c/get-cursor session c/table-name)]
    (case f
      :r  (let [_ (info "read from key:" k)]
            mop)
      :w  (let [_ (info "write key:" k "with value " v)]
            mop)
      :append (let [_ (info "append list in key " k "with " v)]
                mop))))

(defrecord WtClient [conn]
  client/Client
  (open! [this test node]
    ; Get the connection to wiredtiger
    (let [_ (info "Begin open!")]
      (if (= conn nil)
        (let [connection c/wt-conn]
          (assoc this
            :conn (:conn @connection))))))

  (setup! [this test]
    (let [_ (info "Begin setup!, conn is " conn)]
      (c/create-table conn c/table-name c/table-format)))

  (invoke! [this test op]
    (let [_ (info "Begin invoke!")]
      (c/with-errors op
                     (timeout 5000 (assoc op :type :info, :error :timeout)
                              (let [txn' (let [session (c/start-session conn)]
                                           (try (let [ret (c/begin-transaction session "isolation=snapshot")
                                                      _   (info "Executing op" op)
                                                      res (mapv (partial apply-mop! test session) (:value op))
                                                      _   (info "Result is " res)]
                                                  res)
                                                (finally
                                                  (c/commit-transaction session)
                                                  (c/close-session session)))
                                           )]
                                (assoc op :type :ok :value txn'))))))

  (teardown! [this test]
    (info "Begin teardown!"))

  (close! [this test]
    (let [_ (info "Begin close!")])))

(defn rw-test
  [opts]
  {:generator (rw-register/gen opts)
   :checker   (rw-register/checker opts)
   })

(defn workload
  "A generator, client, and checker for a rw-register test."
  [opts]
  (assoc (rw-test {:key-count          10
                   :key-dist           :exponential
                   :max-txn-length     (:max-txn-length opts 4)
                   :max-writes-per-key (:max-writes-per-key opts)
                   :consistency-models [:strong-snapshot-isolation]})
    :client (WtClient. nil)))