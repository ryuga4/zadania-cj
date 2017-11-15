(ns zadania.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [zadania.storage :as storage]
            [zadania.handler :refer :all]
            [clojure.data.json :as json]
            [cheshire.core :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/api/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(->((app) (-> (request :post "/api/insert" (generate-string {:path {:group "TEST"
                                                                      :year 2000
                                                                      :month 1
                                                                      :day 1}
                                                               :event {:ev_name "test name"
                                                                       :ev_date "200001011200"
                                                                       :ev_type "homework"
                                                                       :ev_content "content test"}}))
              (content-type "application/json")))
   (update :body slurp))
