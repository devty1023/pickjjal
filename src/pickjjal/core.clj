(ns pickjjal.core
  (:require [ring.util.codec :refer [form-encode]]
            [clj-http.client :as client]
            [pickjjal.config :refer [slack-webhook-url]]))

(def google-img-url-regex #"(?<=imgurl=)[^&]*")

(defn- best-jjal [urls]
  (letfn [(large-img? [url]
            "returns true if img is > 100 kb"
            (when-let [len (-> (client/get url {:throw-exceptions false})
                               (get-in [:headers "Content-Length"]))]
            (-> len
                Long/parseLong
                (> 100000))))]
    (first (drop-while large-img? urls))))

(defn get-jjal [query]
  "Retrieves first relevant image from google image search based on the query string"
  (let [opts (form-encode {:q query :source "lnms" :tbm "isch" :tbs "isz:m" :safe "active" })
        url (str "https://www.google.com/search?" opts)
        headers {"User-Agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}]
    (->> (client/get url {:headers headers :throw-exceptions false})
         :body
         (re-seq google-img-url-regex)
         best-jjal)))

(defn send-jjal [msg]
  (client/post slack-webhook-url 
    {:body (str "{\"text\": \"" msg "\","
                "\"username\": \"" "짤검색기" "\"}")}))

