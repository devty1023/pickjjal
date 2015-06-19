(ns pickjjal.core
  (:require [ring.util.codec :refer [form-encode]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [pickjjal.config :refer [slack-webhook-url]]))

(def google-img-url-regex #"(?<=imgurl=)[^&]*")

(def ^:dynamic send-slack #(client/post slack-webhook-url %))

(defn- best-jjal [urls]
  (letfn [(large-img? [url]
            "returns true if not 200 or img is > 60 kb"
            (let [resp (client/get url {:throw-exceptions false})]
              (if (= 200 (:status resp))
                (-> (get-in resp [:headers "Content-Length"])
                    Long/parseLong
                    (> 60000))
                true)))]
    (first (drop-while large-img? urls))))

(defn get-jjal [query]
  "Retrieves first relevant image from google image search based on the query string"
  (let [opts (form-encode {:q query :source "lnms" :tbm "isch" :tbs "isz:m,ift:jpg" :safe "active" })
        url (str "https://www.google.com/search?" opts)
        headers {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}]
    (->> (client/get url {:headers headers :throw-exceptions false})
         :body
         (re-seq google-img-url-regex)
         best-jjal)))

(defn send-jjal [msg]
  (send-slack
    {:body (json/write-str
            {:text (:query msg) :username "짤검색기" :channel (:channel msg)})}))

