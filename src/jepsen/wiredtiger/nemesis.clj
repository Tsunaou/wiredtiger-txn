(ns jepsen.wiredtiger.nemesis
  "Nemesis for WiredTiger"
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :refer [info warn]]
            [dom-top.core :refer [real-pmap]]
            [jepsen [nemesis :as n]
             [net :as net]
             [util :as util]]
            [jepsen.generator :as gen]
            [jepsen.nemesis [combined :as nc]
             [time :as nt]]
            [jepsen.wiredtiger.db :as db]))

(defn wiredtiger-generator
  []
  (cycle [(gen/sleep 1)]))

(defn nemesis-package
  "Construct a nemesis and generators for WiredTiger"
  [opts]
  {:generator (wiredtiger-generator)}
  {:nemesis n/noop})