(ns zadania.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [zadania.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [zadania.env :refer [defaults]]
            [mount.core :as mount]
            [zadania.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    #'service-routes
    (route/not-found
      "page not found")))



(defn app [] (middleware/wrap-base #'app-routes))
