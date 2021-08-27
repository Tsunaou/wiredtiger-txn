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
           (com.wiredtiger.db WiredTigerRollbackException)))

;; necessary for key -> table -> key
(def table-account 50)
(def key-account 10)

(defn make-table-name
  [key]
  (str "table:" key))

(defn make-table-colgroup
  [key]
  (str "colgroup:" key ":main"))

(def table-format "key_format=r,value_format=q,columns=(id, val),colgroups=(main)")
(def colgroup-format "columns=(val)")

(defn apply-mop!
  "Applies a transactional micro-operation to a connection."
  [test session [f k v :as mop]]
  (let [append (case f
                 :r      nil
                 :append "append")
        _ (info "f is" f ",key is" k ", append is " append)]
    (with-open [cursor  (c/get-cursor session (make-table-name k) append)]
      (case f
        :r      [f k (c/read-from-table cursor k)]
        ;:r      mop
        :append (let [ret (c/append-to cursor k v)
                      _   (info "append result is " ret)]
                   mop)
        ;:append mop
        )  
      )))

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
    (let [_         (info "Begin setup!, conn is " conn)
          tables    (mapv make-table-name (range table-account))
          colgroups (mapv make-table-colgroup (range table-account))
          _         (info "Tables " tables)
          _         (info "Col Groups " colgroups)]
      (doseq [table tables]
        (c/create-table conn table table-format))
      (doseq [colgroup colgroups]
        (c/create-table conn colgroup colgroup-format))
      ;(mapv (partial c/create-key-tables conn table-format) tables)
      ;(mapv (partial c/create-key-tables conn colgroup-format) colgroups)
      ))

  (invoke! [this test op]
    (let [_ (info "Begin invoke!")]
      (c/with-errors op
                     (timeout 5000 (assoc op :type :info, :error :timeout)
                              (let [txn' (let [session (c/start-session conn)]
                                           (try (let [
                                                      ret (c/begin-transaction session "isolation=snapshot")
                                                      ;_   (info "Executing op" op)
                                                      res (mapv (partial apply-mop! test session) (:value op))
                                                      ;_   (info "Result is " res)
                                                      _   (c/commit-transaction session)
                                                      ]
                                                  res)
                                                (catch WiredTigerRollbackException e#
                                                  (c/rollback-transaction session)
                                                  nil)
                                                (finally
                                                  (c/close-session session)))
                                           )]
                                (if (= txn' nil)
                                  (assoc op :type :info, :error :conflict-rollback)
                                  (assoc op :type :ok, :value txn')))))))

  (teardown! [this test]
    (info "Begin teardown!"))

  (close! [this test]
    (let [_ (info "Begin close!")])))

(defn workload
  "A generator, client, and checker for a list-append test."
  [opts]
  (assoc (list-append/test {:key-count          key-account
                            :key-dist           :uniform
                            :max-txn-length     (:max-txn-length opts 4)
                            :max-writes-per-key (:max-writes-per-key opts)
                            :consistency-models [:strong-snapshot-isolation]})
    :client (WtClient. nil)))

