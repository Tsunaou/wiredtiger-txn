(ns jepsen.wiredtiger
  (:require [clojure.tools.logging :refer :all]
            [clojure [string :as str]
                     [pprint :refer [pprint]]]
            [jepsen [cli :as cli]
                    [tests :as tests]
                    [util :as util :refer [parse-long]]]
            [jepsen.os.debian :as debian]
            [jepsen.wiredtiger [db :as db]
                               [list-append :as list-append]
                               [nemesis :as nemesis]]))

(def wiredtiger-node
  "All nodes consist of wiredtiger database"
  {:nodes ["localhost"]})

(defn wiredtiger-test
  "Given an options map from the command line runner(e.g. :nodes, :ssh,
  :concurrency, ...), constructs a test map"
  [opts]
  (let [db (db/wiredtiger-db opts)]
    (merge tests/noop-test
           opts
           {:name "wiredtiger"
            :os  debian/os
            :db  db
            :pure-generators true})))

(def cli-opts
  "Addtional CLI options"
  [[nil "--max-txn-length NUM" "Maximum number of operations in a transaction."
       :default  4
       :parse-fn parse-long
       :validate [pos? "Must be a positive integer"]]

   [nil "--max-writes-per-key NUM" "Maximum number of writes to any given key."
    :default  256
    :parse-fn parse-long
    :validate [pos? "Must be a positive integer."]]

   [nil "--dir" "Where is the directory of wiredtiger database?"
    :parse-fn read-string
    :default "/tmp/wiredtiger"]])

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results"
  [& args]
  (cli/run! (cli/single-test-cmd {:test-fn wiredtiger-test
                                  :opt-spec cli-opts})
            args))
