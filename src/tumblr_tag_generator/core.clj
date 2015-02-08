(ns tumblr-tag-generator.core
	(:require
		[oauth.client :as oauth]
		[clojure.edn :as edn]
		[org.httpkit.client :as client]
		[clojure.data.json :as json]
		[clojure.pprint :as pprint]
	)
	(:use [clojure.walk :only [keywordize-keys]]
		  [tumblr-tag-generator.oauth :only [run-oauth consumer]])
	(:gen-class)
)

(defn read-config [] (edn/read-string (slurp "config.edn")))

(defn reblog-url [config] (str "http://api.tumblr.com/v2/blog/" (:hostname config) "/post/reblog"))

(defn gen-reblog [params config]
	(->
		(oauth/credentials
			(consumer config)
			(:oauth_token config)
			(:oauth_token_secret config)
			:POST
			(reblog-url config)
			params
		)
		(#(oauth/build-request % params))
	)
)

(defn -main
	[& args]
	(let [config (read-config)]
		(if (contains? config :oauth_token)
			(println "Config ready")
			(run-oauth config)
		)
	)
)

(defn items [config] (->
	@(client/get "http://api.tumblr.com/v2/tagged" {:query-params {:tag (:tag config) :api_key (:consumer-key config)}})
	:body
	json/read-str
	keywordize-keys
	:response
	((partial map #(select-keys % [:id :reblog_key :type :format])))
))


(defn post-items [config items]
  (->
   items
  ((partial map #(client/post (reblog-url config) (gen-reblog (assoc % :api_key (:consumer-key config)) config))))
  ;;((partial map #(gen-reblog (assoc % :api_key (:consumer-key config)) config))))
  )
)

(def futures (items (read-config)))

futures
(list (first futures))

(def posted (post-items (read-config) (list (first futures))))


(gen-reblog (first futures) (read-config))

posted

(doseq [resp posted]
    ;; wait for server response synchronously
    (println @resp " status: " (:status @resp))
)
