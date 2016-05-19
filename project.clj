(defn deploy-info
  [url]
  {:url url
   :username :env/nexus_jenkins_username
   :password :env/nexus_jenkins_password
   :sign-releases false})

(def pdb-version "4.1.1-SNAPSHOT")
(def pe-pdb-version "4.1.1-SNAPSHOT")

(def rbac-client-version "0.1.4")
(def rbac-version "1.2.19")

(def activity-version"0.5.1")

(def tk-version "1.3.1")
(def ks-version "1.3.1")
(def i18n-version "0.2.2")

(def pdb-jvm-opts
  (case (System/getProperty "java.specification.version")
    "1.7" ["-XX:MaxPermSize=200M"]
    []))

(defproject puppetlabs/pe-puppetdb-extensions pe-pdb-version
  :pedantic? :abort

  :description "Library for replicating PuppetDB instances"
  :repositories [["releases" "http://nexus.delivery.puppetlabs.net/content/repositories/releases/"]
                 ["snapshots"  "http://nexus.delivery.puppetlabs.net/content/repositories/snapshots/"]]
  :source-paths ["src"]
  :dependencies [[puppetlabs/puppetdb ~pdb-version]
                 [net.logstash.logback/logstash-logback-encoder "4.6"]
                 [puppetlabs/structured-logging "0.1.0" :exclusions [org.slf4j/slf4j-api]]
                 [puppetlabs/rbac-client ~rbac-client-version
                  :exclusions [puppetlabs/http-client
                               commons-codec
                               org.apache.httpcomponents/httpclient
                               prismatic/schema
                               org.clojure/clojure
                               org.clojure/tools.macro
                               puppetlabs/trapperkeeper]]]
  :deploy-repositories [["releases" ~(deploy-info "http://nexus.delivery.puppetlabs.net/content/repositories/releases/")]
                        ["snapshots" ~(deploy-info "http://nexus.delivery.puppetlabs.net/content/repositories/snapshots/")]]
  :resource-paths ["resources"]

  :uberjar-name "puppetdb.jar"
  :lein-ezbake {:vars {:user "pe-puppetdb"
                       :group "pe-puppetdb"
                       :build-type "pe"
                       :main-namespace "puppetlabs.puppetdb.main"}
                :config-dir "ext/config/pe"}

  :puppetdb ~pdb-version
  :pe-rbac-service ~rbac-version
  :rbac-client ~rbac-client-version
  :pe-activity-service ~activity-version
  :liberator-util "fa1005928d58625704ea48126ee8840c6bbb1399"
  :schema-tools "schema-tools-0.1.0"

  :profiles {:dev {:resource-paths ["test-resources"]
                   :dependencies [[org.flatland/ordered "1.5.2"]
                                  [puppetlabs/pe-rbac-service ~rbac-version
                                   :exclusions [hiccup
                                                puppetlabs/http-client
                                                org.apache.httpcomponents/httpclient
                                                puppetlabs/pe-activity-service]]
                                  [puppetlabs/pe-rbac-service ~rbac-version
                                   :classifier "test"
                                   :exclusions [hiccup
                                                puppetlabs/http-client
                                                org.apache.httpcomponents/httpclient
                                                puppetlabs/pe-activity-service]]
                                  [puppetlabs/pe-activity-service ~activity-version
                                   :exclusions [org.slf4j/slf4j-api
                                                puppetlabs/pe-rbac-service]]
                                  [puppetlabs/rbac-client ~rbac-client-version
                                   :classifier "test"
                                   :exclusions [puppetlabs/http-client
                                                org.apache.httpcomponents/httpclient
                                                prismatic/schema
                                                org.clojure/clojure
                                                org.clojure/tools.macro
                                                puppetlabs/trapperkeeper]]
                                  [org.clojure/test.check "0.9.0"]
                                  [ring-mock "0.1.5"]
                                  [puppetlabs/puppetdb ~pdb-version :classifier "test"]
                                  [puppetlabs/trapperkeeper ~tk-version :classifier "test"]
                                  [puppetlabs/kitchensink ~ks-version :classifier "test"]
                                  [environ "1.0.0"]]}
             :ezbake {:dependencies ^:replace [[puppetlabs/puppetdb ~pdb-version]
                                               [org.clojure/tools.nrepl "0.2.3"]
                                               [puppetlabs/pe-puppetdb-extensions ~pe-pdb-version]]
                      :plugins [[puppetlabs/lein-ezbake "0.4.1"
                                :exclusions [org.clojure/clojure]]]
                      :version ~pe-pdb-version
                      :name "pe-puppetdb"}
             :ci {:plugins [[lein-pprint "1.1.2"]]}}

  :plugins [[puppetlabs/i18n ~i18n-version]]

  :lein-release {:scm :git, :deploy-via :lein-deploy}
  :aliases {"pdb" ["trampoline"
                   "run" "services"
                   "--config" "dev-resources/example.conf"
                   "--bootstrap-config" "dev-resources/bootstrap.cfg"]}

  :jvm-opts ~pdb-jvm-opts

  :main ^:skip-aot puppetlabs.puppetdb.core)
