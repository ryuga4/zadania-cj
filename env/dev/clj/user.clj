(ns user
  (:require 
   [mount.core :as mount]
   [zadania.storage :as storage]
            zadania.core))

(defn start []
  (storage/start)
  (mount/start-without #'zadania.core/repl-server))

(defn stop []
  (storage/stop)
  (mount/stop-except #'zadania.core/repl-server))

(defn restart []
  (stop)
  (start))


