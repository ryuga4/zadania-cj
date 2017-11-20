(ns zadania.storage
  (:require [clojure.spec.alpha :as s]
            [cheshire.core :refer :all]
            [monger.core :as mg]
            [clojure.data.json :as json]
            [monger.collection :as mc]
            [clojure.string :as str]))
(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

(s/check-asserts true)

(s/def ::month (s/and string?
                      #(= 2 (count %))
                      #(let [a (parse-int %1)]
                         (and (<= 1 a)
                              (>= 12 a)))))
(s/def ::year (s/and string?
                     #(= 4 (count %))))
(s/def ::day (s/and string?
                    #(= 2 (count %))
                    #(let [a (parse-int %1)]
                       (and (<= 1 a)
                            (>= 31 a)))))
(s/def ::time-h (s/and string?
                       #(= 2 (count %))
                       #(let [a (parse-int %1)]
                          (and (<= 0 a)
                               (> 24 a)))))
(s/def ::time-m (s/and string?
                       #(= 2 (count %))
                       #(let [a (parse-int %1)]
                          (and (<= 0 a)
                               (> 60 a)))))

(map str/join(partition 2 "123123"))
(defn map->path [{:keys [group year month day] :as m}]
  [group year month day])


(def uri (or #_(System/getenv "MONGOHQ_URL") "mongodb://heroku_5nfs6hsl:mdpceu2cdagii9ulrtgvdbcr9d@ds163595.mlab.com:63595/heroku_5nfs6hsl"))

(defonce connection (atom {}))

 (defn st-clear []
   (mc/drop (:db @connection) "col1"))

(defn st-get-all [ ]
  (mc/find-maps (:db @connection) "col1"))

(defn st-find-group [group]
  (dissoc (mc/find-one-as-map (:db @connection) "col1" {:group group})
          :_id))
(defn st-insert [gr event]
  (let [[y1 y2 m d time-h time-m] (mapv str/join(take 6(partition 2(str/split (:ev_date event) #""))))
        _ (do(s/assert ::time-h time-h)
             (s/assert ::time-m time-m))
        [group year month day :as path] (mapv keyword [gr
                                                       (s/assert ::year (str y1 y2))
                                                       (s/assert ::month m)
                                                       (s/assert ::day d)])
        {:keys [counter] :as record} (st-find-group group)]
    (if (nil? record)
      (mc/insert (:db @connection) "col1" (-> {:group group}
                                              (assoc-in path [event])))
      (mc/update (:db @connection)
                 "col1"
                 {:group group}
                 (if (get-in record path)
                   (update-in record path (fn [a b]
                                            (conj (set a) b)) event)
                     (assoc-in record path [event]))
                     ))))

(defn st-get-month [group year month]
  (s/assert ::year year)
  (s/assert ::month month)
  (if-let [record (st-find-group group)]
    (if-let [m (get-in record (mapv keyword [group year month]))]
      m
      {})
    {}))

(defn st-delete [gr event]
  (let [[y1 y2 m d time-h time-m] (mapv str/join(take 6(partition 2(str/split (:ev_date event) #""))))
        _ (do(s/assert ::time-h time-h)
             (s/assert ::time-m time-m))
        [group year month day :as path] (mapv keyword [gr
                                                       (s/assert ::year (str y1 y2))
                                                       (s/assert ::month m)
                                                       (s/assert ::day d)])
        record (st-find-group group)]
    (if (nil? record)
      "brak grupy"
      (if (get-in record path)
        (mc/update (:db @connection) "col1" {:group gr}
                   (update-in record path (fn [a b]
                                            (disj (set a) b)) event))
        "brak eventów w tym dniu"))))


#_(st-insert {:group "TEST"
            :year "2017"
            :month "12"
            :day "20"}
           {:ev_type "other2"
            :ev_name "jakies gowno"
            :ev_date "201712201200"
            :ev_content "duopa"})

(defn start []
  (println "\n\n\n --------------- START DATABASE --------------- \n\n\n")
  (let [{:keys [conn db]} (mg/connect-via-uri uri)]
    (reset! connection {:conn conn
                        :db db})))

(defn stop []
  (println "\n\n\n --------------- STOP DATABASE --------------- \n\n\n")
  (mg/disconnect (:conn @connection))
  (reset! connection {}))
