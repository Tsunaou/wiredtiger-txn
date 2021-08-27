(ns jepsen.wiredtiger-test
  (:require [clojure.test :refer :all]
            [jepsen.wiredtiger :refer :all]
            [elle.list-append :as a]
            [elle.rw-register :as rw]
            [clojure.pprint :as p]
            [jepsen.wiredtiger.client :as c]
            [jepsen.wiredtiger.list-append :as li]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

;(def h [{:type :ok, :value [[:append :x 1] [:r :y [1]]]}
;        {:type :ok, :value [[:append :x 2] [:append :y 1]]}
;        {:type :ok, :value [[:r :x [1 2]]]}])
;
;(p/pprint (a/check {:consistency-models [:serializable], :directory "out"} h))

;(def h-rw [
;           {:type :invoke, :value ([:w :x 1] [:w :y 2]), :process 0}
;           {:type :ok, :value [[:w :x 1] [:w :y 2]], :process 0}
;           {:type :invoke, :value ([:w :x 2] [:w :y 1]), :process 1}
;           {:type :ok, :value [[:w :x 2] [:w :y 1]], :process 1}
;           {:type :invoke, :value ([:r :x nil]), :process 0}
;           {:type :ok, :value [[:r :x 2]], :process 0}
;           {:type :invoke, :value ([:r :y nil]), :process 1}
;           {:type :ok, :value [[:r :y 2]], :process 1}]
;  )
;
;(p/pprint (rw/check {:consistency-models [:serializable], :directory "out"} h-rw))

(defn Example
  []
  (def res (atom []))
  (def x (atom 1))
  (while (< @x 5)
    (do
      (prn @x)
      (swap! x inc)
      (reset! res (conj @res @x))
      (prn @res)))
  (if (= 0 (count @res))
    nil
    @res))

(prn (Example))

;(defn test-list
;  []
;  (doseq [i (range li/key-account)]
;    (prn i)))
;
;(test-list)

