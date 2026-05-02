(ns website.routes-test
  (:require [clojure.test :refer [deftest is testing]]
            [website.routes]))

(deftest routing-smoke-test
  (testing "namespace loads"
    (is (resolve 'website.routes/handle))))
