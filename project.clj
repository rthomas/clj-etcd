(defproject clj-etcd "0.1.0-SNAPSHOT"
  :description "etcd client library for Clojure"
  :url "http://github.com/rthomas/clj-etcd"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [clj-http "0.7.7"]]
  :plugins [[lein-kibit "0.0.8"]]
  :scm {:url "git@github.com:rthomas/clj-etcd.git"})