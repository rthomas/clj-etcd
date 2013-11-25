# clj-etcd

A client library for the https://github.com/coreos/etcd project.

## Usage

Usage of the library is simple and it returns a map of data the same as the json returned by etcd.

#### In your project

Leiningen

`[clj-etcd "0.1.1"]`

#### From the REPL

Require the library

    user=> (require '[clj-etcd.core :as etcd])
    nil

Define an instance to connect to, here we will just use a local instance of etcd.

    user=> (def instance (etcd/connect "http://localhost:4001"))
    #'user/instance

All functions in clj-etcd return a function that takes the instance we defined as an argument.

    user=> (etcd/set! "my/key" "my-value")
    #<core$_invoke$fn__128 clj_etcd.core$_invoke$fn__128@68c26bb0>

So we need to pass in the instance.

    user=> ((etcd/set! "my/key" "my-value") instance)
    {:action "set", :key "/my/key", :value "my-value", :modifiedIndex 328}

clj-etcd also supports all of the set options as optional keys - valid options are :ttl, :prev-val, :prev-index and :prev-exist.

    user=> ((etcd/set! "my/key" "my-value" :ttl 100) instance)
    {:action "set", :key "/my/key", :prevValue "my-value", :value "my-value", :expiration "2013-11-25T22:08:05.323038105+11:00", :ttl 100, :modifiedIndex 329}
    
    user=> ((etcd/set! "my/key" "my-new-value" :prev-val "my-value") instance)
    {:action "compareAndSwap", :key "/my/key", :prevValue "my-value", :value "my-new-value", :expiration "2013-11-25T22:08:05.323038105+11:00", :ttl 70, :modifiedIndex 330}

We can also wait on a change for a key, this also support the :recursive optional parameter. The future will have the map data when it is dereferenced (when an update is made to that key).

    user=> ((etcd/wait "some/key") instance)
    #<core$future_call$reify__6267@43e3cae: :pending>

## License

Copyright © 2013 Ryan Thomas

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
