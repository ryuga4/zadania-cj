(ns user
  (:require 
            [mount.core :as mount]
            zadania.core))

(defn start []
  (mount/start-without #'zadania.core/repl-server))

(defn stop []
  (mount/stop-except #'zadania.core/repl-server))

(defn restart []
  (stop)
  (start))


