(ns zadania.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [zadania.storage :as storage]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]))

(s/defschema Path
  {:group s/Str
   :year s/Str
   :month s/Str
   :day s/Str})
(s/defschema Event
  {:ev_name s/Str
   :ev_type s/Str
   :ev_content s/Str
   :ev_date s/Str})
(s/defschema Path-Event
  {:group s/Str
   :event Event})


(s/defschema X
  {:a s/Int})


(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Service?s"}}}}



  (context "/api" []
    :tags ["thingie"]
    (POST "/x" []
          :return String
          :body [a {:a Long}]
          (ok (str a)))
    (POST "/insert" []
          :return String
          :body [pe Path-Event]
          (do (storage/st-insert (:group pe) (:event pe))
              (ok "ok")))
    (POST "/delete" []
          :return String
          :body [pe Path-Event]
          (ok (str (storage/st-delete (:group pe) (:event pe)))))

    (GET "/month" []
         :return {String [{:ev_type String
                           :ev_date String
                           :ev_name String
                           :ev_content String}]}
         :query-params [group :- String, year :- String, month :- String]
         (ok (into {}
                   (map (fn [[k v]] [(name k) v])
                        (storage/st-get-month group year month)))))
    (GET "/month-str" []
         :return String
         :query-params [group :- String, year :- String, month :- String]
         (ok (generate-string (storage/st-get-month group year month))))
    (GET "/clear" []
            :return String
            (do (storage/st-clear)
                (ok "Baza wyczyszczona")))
    ))
