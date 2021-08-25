(ns jepsen.wiredtiger-test
  (:require [clojure.test :refer :all]
            [jepsen.wiredtiger :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(def a (atom nil))
(if (= @a nil)
  (prn "GG"))

(prn a)
(prn @a)
(reset! a "xx")
(prn a)
(prn @a)
