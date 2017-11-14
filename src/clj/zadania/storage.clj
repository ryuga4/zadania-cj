(ns zadania.storage
  #_(:require [clojure.spec.alpha :as s]))



(defprotocol Storage
  (add-event [this time event] "adds event to a day")
  (get-all [this] "returns map containing entire storage"))


(defn map->path [{:keys [group year month day]}]
  [group year month day :events])


(defn local-storage
  "siema"
  []
  (let [st (atom {:counter 0})]
    (reify Storage
      (add-event [_ path-map event]
        (let [path (map->path path-map)]
          (swap! st (fn [{:keys [counter] :as m}]
                      (let [ev (assoc event :id counter)]
                        (-> (if (nil? (get-in m path))
                              (assoc-in m path [])
                              m)
                            (update :counter inc)
                            (update-in path conj ev)))))))
      (get-all [_] @st))))



