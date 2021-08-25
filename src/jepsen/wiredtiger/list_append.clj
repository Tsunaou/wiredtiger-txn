(ns jepsen.wiredtiger.list-append
  "Elle list append workload"
  (:require [clojure [pprint :refer [pprint]]]
            [clojure.tools.logging :refer [info warn]]
            [dom-top.core :refer [with-retry]]
            [jepsen [client :as client]
             [checker :as checker]
             [util :as util :refer [timeout]]]
            [jepsen.tests.cycle :as cycle]
            [jepsen.tests.cycle.append :as list-append]
            [jepsen.wiredtiger [client :as c]]
            [slingshot.slingshot :as slingshot]
            [jepsen.generator :as gen])
  (:import (java.util.concurrent TimeUnit)
           (com.wiredtiger.db wiredtiger)))

(def table-name "table:txn")
(def table-format "key_format=S,value_format=S")

(defn apply-mop!
  "Applies a transactional micro-operation to a connection."
  [test session [f k v :as mop]]
  (let [cursor  (c/get-cursor session table-name)]

    ))

(defrecord WtClient [conn]
  client/Client
  (open! [this test node]
    ; Get the connection to wiredtiger
    (let [_ (info "Begin open!")]
      (assoc this :conn (c/open (:dir test)))))

  (setup! [this test]
    (let [_ (info "Begin setup!, conn is " conn)]
      (c/create-table conn table-name table-format)
      (c/close conn)))

  (invoke! [this test op]
    (let [_ (info "Begin invoke!")]
      (c/with-errors op
                     (timeout 5000 (assoc op :type :info, :error :timeout)
                              (with-open [session (c/start-session conn)]
                                (let [ret (c/begin-transaction session "isolation=snapshot")])
                                (info "Executing op" op)
                                (c/commit-transaction session))))))

  (teardown! [this test]
    (info "Begin teardown!"))

  (close! [this test]
    (let [_ (info "Begin close!")]
      (c/close conn))))

(defn workload
  "A generator, client, and checker for a list-append test."
  [opts]
  (assoc (list-append/test {:key-count          10
                            :key-dist           :exponential
                            :max-txn-length     (:max-txn-length opts 4)
                            :max-writes-per-key (:max-writes-per-key opts)
                            :consistency-models [:strong-snapshot-isolation]})
    :client (WtClient. nil)))

(defn r [_ _] {:type :invoke, :f :read, :value nil})
(defn w [_ _] {:type :invoke, :f :write, :value (rand-int 5)})

(defn rw-workload
  "A read-write register workload"
  [opts]
  {:generator (->> (gen/mix [r w])
                   (gen/stagger 1)
                   (gen/nemesis nil)
                   (gen/time-limit 15))
   :client    (WtClient. nil)})