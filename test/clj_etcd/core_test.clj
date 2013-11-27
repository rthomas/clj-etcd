(ns clj-etcd.core-test
  (:require [clojure.test :refer :all]
            [clj-etcd.core :refer :all]))

(def test-url "http://localhost:4001")

(deftest test-invoke
  (testing "Assert URL build."
    (-invoke test-url
             (fn [url f]
               (is (= (str test-url "/v2/keys/my-key") url)))
             "my-key"))
  (testing "Assert the query parameters in the URL."
    (-invoke test-url
             (fn [url f]
               (is (= (str test-url "/v2/keys/some-key?prevValue=x1&prevIndex=10&prevExist=true") url)))
             "some-key"
             {:prev-val "x1"
             :prev-index 10
             :prev-exist true}))
  (testing "Assert form parameters are correct."
    (-invoke test-url
             (fn [url form-params]
               (is (= "someValue!" (:value (:form-params form-params))))
               (is (= 100 (:ttl (:form-params form-params)))))
             "my-key"
             {:value "someValue!"
             :ttl 100}))
  (testing "nil is returned on Exception"
    (is (= nil (-invoke test-url (fn [u p]
                                   (throw (Exception. "test"))) "key")))))

(deftest test-remove-trailing-slash
  (testing "Assert trailing /'s are removed from the url"
    (is (= (remove-trailing-slash test-url) test-url))
    (is (= (remove-trailing-slash (str test-url "/")) test-url))
    (is (= (remove-trailing-slash (str test-url "////////")) test-url))))

(deftest test-key->url
  (testing "Assert leading /'s are removed from the key"
    (is (= (key->url test-url "my-key") (str test-url "/v2/keys/my-key")))
    (is (= (key->url test-url "/my-key") (str test-url "/v2/keys/my-key")))
    (is (= (key->url test-url "///my-key") (str test-url "/v2/keys/my-key")))
    (is (= (key->url test-url "/my/key/") (str test-url "/v2/keys/my/key/")))))
