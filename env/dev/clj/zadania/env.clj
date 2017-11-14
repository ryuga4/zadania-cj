(ns zadania.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [zadania.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[zadania started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[zadania has shut down successfully]=-"))
   :middleware wrap-dev})
