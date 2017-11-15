(ns zadania.storage
  (:require [clojure.spec.alpha :as s]
            [cheshire.core :refer :all]
            [monger.core :as mg]
            [clojure.data.json :as json]
            [monger.collection :as mc]))


(defn map->path [{:keys [group year month day] :as m}]
  [group year month day])


(def uri (or #_(System/getenv "MONGOHQ_URL") "mongodb://heroku_5nfs6hsl:mdpceu2cdagii9ulrtgvdbcr9d@ds163595.mlab.com:63595/heroku_5nfs6hsl"))

(defonce connection (atom {}))

(defn start []
  (println "\n\n\n --------------- START DATABASE --------------- \n\n\n")
  (let [{:keys [conn db]} (mg/connect-via-uri uri)]
    (reset! connection {:conn conn
                        :db db})))
(defn st-clear []
  (mc/drop (:db @connection) "col1"))

(defn st-get-all []
  (mc/find-maps (:db @connection) "col1"))

(defn st-find-group [group]
  (dissoc (mc/find-one-as-map (:db @connection) "col1" {:group group})
          :_id))

(defn st-insert [path-map event]
  (let [[group year month day :as path] (mapv keyword (map->path path-map))
        {:keys [counter] :as record} (st-find-group group)]
    (if (nil? record)
      (mc/insert (:db @connection) "col1" (-> {:group group
                                               :counter 2}
                                              (assoc-in path [(assoc event :id 1)])))
      (mc/update (:db @connection)
                 "col1"
                 {:group group}
                 (-> (if (get-in record path)
                      (update-in record path conj (assoc event :id counter))
                      (assoc-in record path [(assoc event :id counter)]))
                     (update :counter inc))))))

(defn st-fill-month [record group year month]
  (-> record
      (assoc-in (mapv keyword [group year month]) {})))

#_(st-find-group "TEST")
(defn st-get-month [group year month]
  (if-let [record (find-group group)]
    (if-let [m (get-in record (mapv keyword [group year month]))]
      m
      {})))

#_(st-get-month "TEST" "2017" "11")

#_(st-insert {:group "TEST"
            :year "2017"
            :month "11"
            :day "11"}
           {:ev_type "other2"
            :ev_name "jakies gowno"
            :ev_date "201712101200"
            :ev_content "duopa"})



(defn stop []
  (println "\n\n\n --------------- STOP DATABASE --------------- \n\n\n")
  (mg/disconnect (:conn @connection))
  (reset! connection {}))
