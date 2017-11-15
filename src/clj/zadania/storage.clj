(ns zadania.storage
  (:require [clojure.spec.alpha :as s]
            [cheshire.core :refer :all]
            [monger.core :as mg]
            [clojure.data.json :as json]
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

(defonce connection (atom {}))

(defn start []
  (let [{:keys [conn db]} (mg/connect-via-uri uri)]
    (reset! connection {:conn conn
                        :db db})))
(defn st-clear []
  (mc/drop (:db @connection) "col1"))

(defn st-get-all []
  (mc/find-maps (:db @connection) "col1"))

(defmacro st-find [a]
  `(mc/find-one-as-map (:db @connection) "col1" ~a))

(defn json-repair [a]
  (json/read-str (json/write-str a)))
(defn st-insert [{:keys [group year month day] :as m} event]
  (s/assert ::path-map m)
  (let [path (map->path m)
        f (fn [{:keys [counter] :as m}]
            (let [ev (assoc event :id counter)]
              (-> (if (nil? (get-in m path))
                    (assoc-in m path [])
                    m)
                  (update :counter inc)
                  (update-in path conj ev))))]
    (if-let [record (st-find {:group group})]
      (mc/update (:db @connection) "col1" {:group group} (json-repair (f record)))
      (mc/insert (:db @connection) "col1" (json-repair (f {:counter 0 :group group}))))
    ))

(defn st-get-month [group year month]
  (s/assert string? group)
  (s/assert int? year)
  (s/assert int? month)
  (dissoc (if-let [gr (st-find {:group group})]
           (get-in gr (mapv keyword [group (str year) (str month)]))
           {:error "no such group"})
          :_id))




#_(st-insert {:group "1CA"
            :year 2017
            :month 11
            :day 12}
           {:ev-type "homework"
            :ev-name "namew"
            :ev-date "201711121200"
            :ev-content "content"})


#_(st-get-all)
#_(st-clear)

(defn stop []
  (mg/disconnect (:conn @connection))
  (reset! connection {}))
