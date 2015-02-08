(ns tumblr-tag-generator.oauth
	(:require
		[oauth.client :as oauth]
		[clojure.edn :as edn]
		[org.httpkit.client :as client]
		[org.httpkit.server :as server]
		[clojure.java.browse :as browse]
		[compojure.route :as route]
		[compojure.handler :as handler]
		[ring.util.response :as response]
		[clojure.pprint :as pprint]
	)
	(:use
		[compojure.core]
	)
)

(def request-token (atom nil))
(def config (atom nil))
(def config_file "config.edn")

(defn consumer [config] (oauth/make-consumer (:consumer-key config) (:consumer-secret config)
                                       "http://www.tumblr.com/oauth/request_token"
                                       "http://www.tumblr.com/oauth/access_token"
                                       "http://www.tumblr.com/oauth/authorize"
                                       :hmac-sha1))

(defn generate-request-token [] (oauth/request-token (consumer @config) "http://localhost:3000"))
(defn auth-uri [] (oauth/user-approval-uri (consumer @config) (:oauth_token @request-token)))

(defn access-token-response [verifier] (oauth/access-token (consumer @config) @request-token verifier))

(defn index-page [req]
	(let [
		verifier (-> req :params :oauth_verifier)
		access (access-token-response verifier)
		]
		(response/redirect (str "/tokens?oauth_token=" (:oauth_token access) "&oauth_token_secret=" (:oauth_token_secret access)))
	)
)

(defn token-printer [req]
	(binding [pprint/*print-right-margin* 80]
		(let [config (with-out-str (pprint/pprint (merge @config (:params req))))]
			(spit config_file config)
			(println "Press Ctrl+C to quit the program, then restart")
			(str "Written the following to " config_file "<br><pre>\n" config "</pre><br />Close the program and restart it.")
		)
	)
)

(defroutes main-routes
  (GET "/" [] index-page)
  (GET "/tokens" [] token-printer)
)

(def app (handler/site main-routes))

(defn run-oauth [configure]
	(println "No OAuth2 config, so opening up browser for Tumblr auth")
	(reset! config configure)
	(reset! request-token (generate-request-token))
	(browse/browse-url (auth-uri))
	(server/run-server app {:port 3000})
)
