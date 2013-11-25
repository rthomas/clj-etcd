(ns clj-etcd.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(defn connect [url]
  "Specifies the instance to connect to - this needs to be
   passed to the functions returned by the other functions."
  (if (and url (.endsWith url "/"))
    (recur (.substring url 0 (- (count url) 1)))
    {:url url}))

(defn key->url [instance k]
    (if (and k (.startsWith k "/")) 
      (recur instance (clojure.string/replace-first k #"/" ""))
      (str (:url instance) "/v2/keys/" k)))

(defn- query-params [url & {:keys [prev-val prev-index prev-exist wait recursive]}]
  (let [params (cond-> []
                       prev-val (conj (str "prevValue=" prev-val))
                       prev-index (conj (str "prevIndex=" prev-index))
                       prev-exist (conj (str "prevExist=" prev-exist))
                       wait (conj (str "wait=" wait))
                       recursive (conj (str "recursive=" recursive)))]
    (if (empty? params)
      url
      (str url "?" (clojure.string/join "&" params)))))

(defn- param-map [& {:keys [value ttl]}]
  (cond-> {}
          value (merge {:value value})
          ttl (merge {:ttl ttl})))

(defn- parse-response [resp]
  (json/parse-string (:body resp) true))

(defn -invoke [f & {:keys [key
                           value
                           ttl
                           prev-val
                           prev-index
                           prev-exist
                           wait
                           recursive]}]
  (fn [instance]
    (let [url (-> instance
                  (key->url key)
                  (query-params :prev-val prev-val
                                :prev-index prev-index
                                :prev-exist prev-exist
                                :wait wait
                                :recursive recursive))]
      (log/debug "invoke url:" url)
      (try
        (-> url
            (f {:form-params (param-map :value value :ttl ttl)})
            (parse-response))
        (catch Exception e
          (log/info "Exception:" (.getMessage e))
          nil)))))

(defn set! [k v & {:keys [ttl
                          prev-val
                          prev-index
                          prev-exist]}]
  "Returns a function that will set the value of a key, the
   following options are allowed: :ttl, :prev-val :prev-index
   and :prev-exist."
  (log/debug "set!" k "=" v ", ttl =" ttl
             ", prev-val =" prev-val
             ", prev-index =" prev-index
             ", prev-exist =" prev-exist)
  (-invoke client/put
           :key k
           :value v
           :ttl ttl
           :prev-val prev-val
           :prev-index prev-index
           :prev-exist prev-exist))

(defn ls [k]
  "Returns a function to perform a listing of the given key if it is a
  directory."
  (log/debug "ls:" k)
  (-invoke client/get :key k))

(defn delete! [k]
  "Returns a function that will perform a delete of the specified key."
  (log/debug "delete!" k)
  (-invoke client/delete :key k))

(defn wait [k & {:keys [recursive]}]
  "Returns a function that takes an instance and returns a future, blocking
   until a change is made on the requested key. Valid options are
   :recursive true."
  (log/debug "wait" k "recursive:" recursive)
  (fn [instance]
    (future ((-invoke client/get
                     :key k
                     :wait true
                     :recursive recursive) instance))))
