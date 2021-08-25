(ns jepsen.wiredtiger.db
  (:require [clojure [pprint :refer [pprint]]
                     [string :as str]]
            [clojure.tools.logging :refer [info warn]]
            [jepsen [db :as db]
                    [control :as c]]
            [jepsen.wiredtiger [client :as client]]))

(defn install!
  [test]
  "Install WiredTiger on the current node"
  (c/exec :mkdir :-p (:dir test))
  (let [dir (:dir test)
        _ (info "dir in opts is " dir)
        conn (client/open dir)
        _ (swap! client/wt-conn assoc :conn conn)]))

(defn remove!
  [test]
  "Remove the old WiredTiger database on the current node"
  (c/su
    (c/exec :rm :-rf (:dir test))))

(defn wiredtiger-db
  "This database represent a WiredTiger deployment"
  [opts]
  (reify db/DB
    (setup! [_ test node]
      ; For wiredtiger, just create a directory.
      (info node "installing wiredtiger")
      (install! test))
    (teardown! [_ test node]
      ; For wiredtiger, just remove the directory
      (info node "tearing down wiredtiger")
      (remove! test))))