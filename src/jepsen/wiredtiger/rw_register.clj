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
           (com.wiredtiger.db WiredTigerRollbackException)))

(def table-name "table:txn")
(def table-format "key_format=S,value_format=S")            ; q means long in java

(defn apply-mop!
  "Applies a transactional micro-operation to a connection."
  [test session [f k v :as mop]]
  (with-open [cursor  (c/get-cursor session table-name)]
    (case f
      :r  [f k (c/read-from cursor k)]
      :w  (let [_ (c/write-into cursor k v)]
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
      (c/create-table conn table-name table-format)))

  (invoke! [this test op]
    (let [_ (info "Begin invoke!")]
      (c/with-errors op
                     (timeout 5000 (assoc op :type :info, :error :timeout)
                              (let [start-timestamp (atom nil)
                                    commit-timestamp (atom nil)
                                    txn' (let [session (c/start-session conn)]
                                           (try (let [_   (reset! start-timestamp (util/relative-time-nanos))
                                                      ret (c/begin-transaction session "isolation=snapshot")
                                                      ;_   (info "Executing op" op)
                                                      res (mapv (partial apply-mop! test session) (:value op))
                                                      ;_   (info "Result is " res)
                                                      _   (c/commit-transaction session)
                                                      _   (reset! commit-timestamp (util/relative-time-nanos))]
                                                  res)
                                                (catch WiredTigerRollbackException e#
                                                  (c/rollback-transaction session)
                                                  nil)
                                                (finally
                                                  (c/close-session session)))
                                           )]
                                (if (= txn' nil)
                                  (assoc op :type :info, :error :conflict-rollback)
                                  (assoc op :type :ok, :value txn', :start-timestamp @start-timestamp, :commit-timestamp @commit-timestamp)))))))

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