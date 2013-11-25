(ns clj-etcd.core-test
  (:require [clojure.test :refer :all]
            [clj-etcd.core :refer :all]))

(def test-url "http://localhost:4001")

(deftest test-invoke
  (let [instance (connect test-url)]
    (testing "Assert URL build."
      ((-invoke (fn [url f]
                  (is (= (str test-url "/v2/keys/my-key") url)))
                :key "my-key") instance))
    (testing "Assert the query parameters in the URL."
      ((-invoke (fn [url f]
                  (is (= (str test-url "/v2/keys/some-key?prevValue=x1&prevIndex=10&prevExist=true") url)))
                :key "some-key"
                :prev-val "x1"
                :prev-index 10
                :prev-exist true) instance))
    (testing "Assert form parameters are correct."
      ((-invoke (fn [url form-params]
                  (is (= "someValue!" (:value (:form-params form-params))))
                  (is (= 100 (:ttl (:form-params form-params)))))
                :value "someValue!"
                :ttl 100) instance))
    (testing "nil is returned on Exception"
      (is (= nil ((-invoke (fn [u p]
                             (throw (Exception. "test"))))
                  instance)))) 
    ))

(deftest test-connect
  (testing "Assert trailing /'s are removed from the url"
    (is (= (:url (connect test-url)) test-url))
    (is (= (:url (connect (str test-url "/"))) test-url))
    (is (= (:url (connect (str test-url "////////"))) test-url))))

(deftest test-key->url
  (let [i (connect test-url)]
    (testing "Assert leading /'s are removed from the key"
      (is (= (key->url i "my-key") (str test-url "/v2/keys/my-key")))
      (is (= (key->url i "/my-key") (str test-url "/v2/keys/my-key")))
      (is (= (key->url i "///my-key") (str test-url "/v2/keys/my-key")))
      (is (= (key->url i "/my/key/") (str test-url "/v2/keys/my/key/"))))))
