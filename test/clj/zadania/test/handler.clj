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

(->((app) (-> (request :post "/api/insert" (generate-string
                                            {:group "TEST2"
                                             :event {:ev_name "tescik"
                                                     :ev_date "201711181200"
                                                     :ev_type "other"
                                                     :ev_content "content test"}}

                                            ))
              (content-type "application/json")))
   (update :body slurp))
(storage/st-clear) 

(slurp (:body ((app) (content-type (request :post "/api/x" "{\"a\":10}") "application/json"))))
(slurp (:body ((app) (request :get "/api/month?group=TEST&year=2017&month=11"))))
(slurp (:body ((app) (request :get "/api/all"))))
(generate-string (storage/st-get-month "TEST" "2017" "12"))
(generate-string (dissoc (storage/st-find {:group "TEST3"}) :_id))
(storage/st-get-all)



(into {}
      (map (fn [[k v]] [(name k) v])
           (storage/st-get-month "TEST" "2017" "11")))



(into {} (map (fn [[a b]] [a b]) {:a 1 :b 2}))


