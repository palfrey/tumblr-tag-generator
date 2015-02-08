(defproject tumblr-tag-generator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
				 [org.clojure/clojure "1.6.0"]
				 [org.clojure/data.json "0.2.3"]
				 [clj-oauth "1.5.2"]
				 [http-kit "2.1.16"]
				 [compojure "1.3.1"]
				 [javax.servlet/servlet-api "2.5"]
				 ]
  :plugins [[lein-ring "0.8.11"]]
  :main ^:skip-aot tumblr-tag-generator.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
