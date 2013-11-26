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

(defn remove-trailing-slash [url]
  (if (and url (.endsWith url "/"))
    (recur (.substring url 0 (- (count url) 1)))
    url))

(defn key->url [base-url k]
    (if (and k (.startsWith k "/")) 
      (recur base-url (clojure.string/replace-first k #"/" ""))
      (str base-url "/v2/keys/" k)))

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

(defn -invoke [base-url f & {:keys [key
                                    value
                                    ttl
                                    prev-val
                                    prev-index
                                    prev-exist
                                    wait
                                    recursive]}]
  (let [url (-> base-url
                (remove-trailing-slash)
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
        nil))))

(defn set! [base-url k v & {:keys [ttl
                          prev-val
                          prev-index
                          prev-exist]}]
  "Will set the value of a key, the
   following options are allowed: :ttl, :prev-val :prev-index
   and :prev-exist."
  (log/debug "set!" k "=" v ", ttl =" ttl
             ", prev-val =" prev-val
             ", prev-index =" prev-index
             ", prev-exist =" prev-exist)
  (-invoke base-url
           client/put
           :key k
           :value v
           :ttl ttl
           :prev-val prev-val
           :prev-index prev-index
           :prev-exist prev-exist))

(defn exists? [base-url k]
  "Returns true if the key exists."
  nil)

(defn dir? [base-url k]
  "Returns true if the key exists and is a directory."
  nil)

(defn ls [base-url k]
  "Returns a function to perform a listing of the given key if it is a
  directory."
  (log/debug "ls:" k)
  (-invoke base-url client/get :key k))

(defn delete! [base-url k]
  "Returns a function that will perform a delete of the specified key."
  (log/debug "delete!" k)
  (-invoke base-url client/delete :key k))

(defn wait [base-url k & {:keys [recursive]}]
  "Returns a function that takes an instance and returns a future, blocking
   until a change is made on the requested key. Valid options are
   :recursive true."
  (log/debug "wait" k "recursive:" recursive)
  (future (-invoke base-url
                   client/get
                   :key k
                   :wait true
                   :recursive recursive)))
