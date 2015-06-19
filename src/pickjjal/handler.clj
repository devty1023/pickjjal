(ns pickjjal.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [pickjjal.core :refer [get-jjal send-jjal]]
            [pickjjal.config :refer [slack-incoming-token]]))


(defn pickjjal [request]
  (let [token (get-in request [:params :token])
        query (get-in request [:params :text])
        username (get-in request [:params :user_name])
        channel-id (get-in request [:params :channel_id])]
    (when (= token slack-incoming-token)
      (->> query
           get-jjal
           (str username ": " query " ")
           (assoc {:channel channel-id} :query)
           send-jjal)
      (str "Processed " query))))

(defroutes app-routes
  (GET "/pickjjal" [] pickjjal)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
