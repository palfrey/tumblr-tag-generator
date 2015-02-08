(ns tumblr-tag-generator.core
	(:require
		[oauth.client :as oauth]
		[clojure.edn :as edn]
		[org.httpkit.client :as client]
		[clojure.data.json :as json]
		[clojure.pprint :as pprint]
	)
	(:use
		[clojure.walk :only [keywordize-keys]]
		[tumblr-tag-generator.oauth :only [run-oauth consumer]]
		[slingshot.slingshot :only [throw+]]
	)
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

(defn items [config] (->
	@(client/get "http://api.tumblr.com/v2/tagged" {:query-params {:tag (:tag config) :api_key (:consumer-key config)}})
	:body
	json/read-str
	keywordize-keys
	:response
	((partial map #(select-keys % [:id :reblog_key :type :format])))
))

(defn existing-posts [config] (->
	@(client/get
		(str "http://api.tumblr.com/v2/blog/" (:hostname config) "/posts")
		{:query-params {:api_key (:consumer-key config)}}
	)
	:body
	json/read-str
	keywordize-keys
	:response
	:posts
	((partial map :reblog_key))
))

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn post-items [config items existing]
	(->
		(remove #(seq-contains? existing (:reblog_key %)) items)
		((partial map #(client/post (reblog-url config) (gen-reblog (assoc % :api_key (:consumer-key config)) config))))
  ;;((partial map #(gen-reblog (assoc % :api_key (:consumer-key config)) config))))
	)
)

(defn dopost [config]
	(let [existing (existing-posts config)
		  topost (items config)
		  posted (post-items config topost existing)
		  ]
		(doseq [resp posted]
			;; wait for server response synchronously
			(if (not= (:status @resp) 201)
				(throw+ {:type :bad-response :status (:status @resp) :query @resp})
			)
		)
	)
)

(defn -main
	[& args]
	(let [config (read-config)]
		(if (contains? config :oauth_token)
			(dopost config)
			(run-oauth config)
		)
	)
)
