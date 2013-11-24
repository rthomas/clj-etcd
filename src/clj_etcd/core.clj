(ns clj-etcd.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(defn connect [url]
  {:url url})

(defn- key-url [instance k]
  (str (:url instance) "/v2/keys/" k))

(defn- param-map [& {:keys [value ttl prev-val]}]
  (cond-> {}
          value (merge {:value value})
          ttl (merge {:ttl ttl})
          prev-val (merge {:prev-val prev-val})))

(defn- parse-response [resp]
  (println resp))

(defn set! [k v & {:keys [ttl prev-val]}]
  (fn [instance]
    (let [resp (client/put (key-url instance k)
                {:form-params (param-map :value v
                                         :ttl ttl
                                         :prev-val prev-val)})]
      (parse-response resp))))

(defn ls [k]
  (fn [instance]
    nil))
