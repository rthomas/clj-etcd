# clj-etcd

A client library for the https://github.com/coreos/etcd project.

## Usage

Usage of the library is simple and it returns a map of data the same as the json returned by etcd.

#### In your project

Leiningen

`[clj-etcd "0.1.3"]`

#### From the REPL

Require the library

    user=> (require '[clj-etcd.core :as etcd])
    nil

Define an instance to connect to, here we will just use a local instance of etcd.

    user=> (def base-url "http://localhost:4001")
    #'user/base-url

##### Setting the value of a key

All functions in clj-etcd require the base-url as the first parameter.

    user=> (etcd/set! base-url "my/key" "my-value")
    {:action "set", 
             :key "/my/key", 
             :value "my-value", 
             :modifiedIndex 271}

clj-etcd also supports all of the set options as optional keys - valid options are :ttl, :prev-val, :prev-index and :prev-exist.

    user=> (etcd/set! base-url "my/key" "my-value" :ttl 100)
    {:action "set", 
             :key "/my/key", 
             :prevValue "my-value", 
             :value "my-value", 
             :expiration "2013-11-27T16:43:06.005677156+11:00", 
             :ttl 100, 
             :modifiedIndex 272}
    
    user=> (etcd/set! base-url "my/key" "my-new-value" :prev-val "my-value")
    {:action "compareAndSwap", 
             :key "/my/key", 
             :prevValue "my-value", 
             :value "my-new-value", 
             :modifiedIndex 273}

##### Getting the value of a key or directory

We use the `ls` function to list the value of a key.

      user=> (etcd/ls base-url "my/key")
      {:action "get", 
               :key "/my/key", 
               :value "my-new-value", 
               :modifiedIndex 275}

This also works on directories.

     user=> (etcd/ls base-url "my")
     {:action "get", 
              :key "/my", 
              :dir true, 
              :kvs [{:key "/my/key", :value "my-new-value", :modifiedIndex 275}
                    {:key "/my/deep", :dir true, :modifiedIndex 274}], 
              :modifiedIndex 271}

##### Checking if a key exists or if it is a directory

We can test the existence of a key and whether this key is a directory.

    user=> (etcd/set! base-url "my/deep/key" :bam)
    {:action "set", 
             :key "/my/deep/key", 
             :value ":bam", 
             :modifiedIndex 274}
    user=> (etcd/exists? base-url "my/deep/key")
    true
    user=> (etcd/exists? base-url "my/deeper/key")
    false
    user=> (etcd/dir? base-url "my/deep/key")
    false
    user=> (etcd/dir? base-url "my/deep")
    true

##### Waiting on a change for a key

We can also wait on a change for a key, this also support the :recursive optional parameter. The future will have the map data when it is dereferenced (when an update is made to that key).

    user=> (etcd/wait base-url "some/key")
    #<core$future_call$reify__6267@43e3cae: :pending>

## Building from source

### Building

This is a simple leiningen project, a `lein compile` should do or a `lein install` to get it into your local repo.

### Testing

A `lein test` will run the unit tests.

## License

Copyright © 2013 Ryan Thomas

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
