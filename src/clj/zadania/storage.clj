(ns zadania.storage
  (:require [clojure.spec.alpha :as s]
            [monger.core :as mg]
            [monger.collection :as mc]))

(s/check-asserts true)

(defprotocol Storage
  (add-event [this time event] "adds event to a day")
  (get-all [this] "returns map containing entire storage"))


(s/def ::group string?)
(s/def ::year int?)
(s/def ::month int?)
(s/def ::day int?)
(s/def ::path-map (s/keys :req-un [::group ::year ::month ::day]
                          :opt-un []))

(s/def ::ev_type string?)
(s/def ::ev_name string?)
(s/def ::ev_content string?)
(s/def ::ev_date (s/and string? #(= 12 (count %1))))
(s/def ::ev_map (s/keys :req-un [::ev_type ::ev_name ::ev_date ::ev_content]
                        :opt-un []))

(defn map->path [{:keys [group year month day] :as m}]
  (s/assert ::path-map m)
  [group year month day])


(defn local-storage
  "siema"
  []
  (let [st (atom {:counter 0})]
    (reify Storage
      (add-event [_ path-map event]
        (s/assert ::ev_map event)
        (let [path (map->path path-map)]
          (swap! st (fn [{:keys [counter] :as m}]
                      (let [ev (assoc event :id counter)]
                        (-> (if (nil? (get-in m path))
                              (assoc-in m path [])
                              m)
                            (update :counter inc)
                            (update-in path conj ev)))))))
      (get-all [_] @st))))


(def uri (or (System/getenv "MONGOHQ_URL") "mongodb://heroku_5nfs6hsl:mdpceu2cdagii9ulrtgvdbcr9d@ds163595.mlab.com:63595/heroku_5nfs6hsl"))

(def connection (atom {}))

(defn start []
  #_(let [{:keys [conn db]} (mg/connect-via-uri uri)]
    (reset! connection {:conn conn
                        :db db}))
  nil)





(defn stop []
  #_(mg/disconnect (:conn @connection))
  #_(reset! connection {})
  nil)
