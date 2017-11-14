(ns zadania.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [zadania.storage :as storage]
            [zadania.handler :refer :all]
            [clojure.data.json :as json]))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/api/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))





(def st (storage/local-storage))

(storage/add-event st
                   {:group "1CA"
                    :year 2017
                    :month 12
                    :day 12}
                   {:ev_name "Name"
                    :ev_type "Type"
                    :ev_date "199806071200"
                    :ev_content "content"})
