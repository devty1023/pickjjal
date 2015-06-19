(ns pickjjal.core-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [pickjjal.core :refer :all]))

(deftest test-get-jjal
  (let [jjal (get-jjal "doge")
        resp (client/get jjal)]
    (is (= 200 (:status resp)))
    (is (> 60000 (-> (get-in resp [:headers "Content-Length"])
                     Long/parseLong)))))

(deftest test-send-jjal
  (with-bindings {#'pickjjal.core/send-slack (fn [conf] conf)}
    (let [body (-> (send-jjal {:query "foobar" :username "devty" :channel "2chan"}) :body json/read-str)]
      (is (= "foobar" (get body "text")))
      (is (= "짤검색기" (get body "username"))))))
