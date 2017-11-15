(ns zadania.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [zadania.storage :as storage]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]))
(def st (storage/local-storage))

(storage/add-event st
                   {:group "1CA"
                    :year 2017
                    :month 11
                    :day 12}
                   {:ev_name "Name1"
                    :ev_type "homework"
                    :ev_date "201711121200"
                    :ev_content "Content1"})
(storage/add-event st
                   {:group "1CA"
                    :year 2017
                    :month 11
                    :day 17}
                   {:ev_name "Name2"
                    :ev_type "test"
                    :ev_date "201711172137"
                    :ev_content "Content2"})
(storage/add-event st
                   {:group "1CA"
                    :year 2017
                    :month 11
                    :day 12}
                   {:ev_name "Name3"
                    :ev_type "other"
                    :ev_date "201711122137"
                    :ev_content "Content2"})
(s/defschema Path
  {:group s/Str
   :year s/Int
   :month s/Int
   :day s/Int})
(s/defschema Event
  {:ev_name s/Str
   :ev_type s/Str
   :ev_content s/Str
   :ev_date s/Str})
(s/defschema Path-Event
  {:path Path
   :event Event})


(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Service?s"}}}}



  (context "/api" []
    :tags ["thingie"]
    (GET "/all" []
         :return {:counter Long
                  String {Long {Long {Long [{:ev_name String
                                             :ev_type String
                                             :ev_content String
                                             :ev_date String
                                             :id Long}]}}}}
         (ok (storage/get-all st)))
    (GET "/chyzy" []
         :return String
         (ok(json/write-str (get-in (storage/get-all st) ["1CA" 2017 11]))))
    (POST "/insert" []
          :return String
          :body [pe Path-Event]
          (do (storage/st-insert (:path pe) (:event pe))
              (ok "ok")))
    (GET "/month-str" []
         :return String
         :query-params [group :- String, year :- Long, month :- Long]
         (ok (generate-string (storage/st-get-month group year month))))
    ))
