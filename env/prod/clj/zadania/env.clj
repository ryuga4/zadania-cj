(ns zadania.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[zadania started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[zadania has shut down successfully]=-"))
   :middleware identity})
