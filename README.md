# Datahike S3 Backend

<p align="center">
<a href="https://clojurians.slack.com/archives/CB7GJAN0L"><img src="https://img.shields.io/badge/clojurians%20slack-join%20channel-blueviolet"/></a>
<a href="https://clojars.org/io.replikativ/datahike-s3"> <img src="https://img.shields.io/clojars/v/io.replikativ/datahike-s3.svg" /></a>
<a href="https://circleci.com/gh/replikativ/datahike-s3"><img src="https://circleci.com/gh/replikativ/datahike-s3.svg?style=shield"/></a>
<a href="https://github.com/replikativ/datahike-s3/tree/main"><img src="https://img.shields.io/github/last-commit/replikativ/datahike-s3/main"/></a>
</p>

The goal of this backend is to support [S3](https://aws.amazon.com/s3). S3 is
comparatively cheap, read scalable and requires no dedicated running process for
reading, but has higher latency than using a local file system or JDBC server as
backend. It is therefore optimal to store large databases that are updated in
bulk or mostly appended to. In this case the Datahike cache will reduce the
latency on database access. Transactions will still have high latency.

## Configuration
Please read the [Datahike configuration docs](https://github.com/replikativ/datahike/blob/master/doc/config.md) on how to configure your backend. Details about the backend configuration can be found in [konserve-s3](https://github.com/replikativ/konserve-s3).A sample configuration is
`create-database`, `connect` and `delete-database`:
```clojure
{:store {:backend :s3
         :bucket "datahike-s3"
         :store-id "instance1"
         :region "us-west-1"
         :access-key "YOUR_ACCESS_KEY"
         :secret "YOUR_ACCESS_KEY_SECRET"}}
```
This same configuration can be achieved by setting one environment variable for the s3 backend
and one environment variable for the configuration of the s3 backend:
```bash
DATAHIKE_STORE_BACKEND=s3
DATAHIKE_STORE_CONFIG='{:bucket "datahike-s3-instance" ...}'
```

## Usage
Add to your Leiningen or Boot dependencies:
[![Clojars Project](https://img.shields.io/clojars/v/io.replikativ/datahike-s3.svg)](https://clojars.org/io.replikativ/datahike-s3)

Now require the Datahike API and the datahike-s3 namespace in your editor or REPL using the
keyword `:s3`. If you want to use other backends than S3 please refer to the official
[Datahike docs](https://github.com/replikativ/datahike/blob/master/doc/config.md).

### Run Datahike in your REPL
```clojure
  (ns project.core
    (:require [datahike.api :as d]
              [datahike-s3.core]))

  (def cfg {:store {:backend :s3
                    :bucket "datahike-s3"
                    :store-id "instance1"
                    :region "us-west-1"
                    :access-key "YOUR_ACCESS_KEY"
                    :secret "YOUR_ACCESS_KEY_SECRET"}})

  ;; Create a database at this place, by default configuration we have a strict
  ;; schema validation and keep historical data
  (d/create-database cfg)

  (def conn (d/connect cfg))

  ;; The first transaction will be the schema we are using:
  (d/transact conn [{:db/ident :name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one }
                    {:db/ident :age
                     :db/valueType :db.type/long
                     :db/cardinality :db.cardinality/one }])

  ;; Let's add some data and wait for the transaction
  (d/transact conn [{:name  "Alice", :age   20 }
                    {:name  "Bob", :age   30 }
                    {:name  "Charlie", :age   40 }
                    {:age 15 }])

  ;; Search the data
  (d/q '[:find ?e ?n ?a
         :where
         [?e :name ?n]
         [?e :age ?a]]
    @conn)
  ;; => #{[3 "Alice" 20] [4 "Bob" 30] [5 "Charlie" 40]}

  ;; Clean up the database if it is not needed any more
  (d/delete-database cfg)
```

## Run Tests

```bash
  bash -x ./bin/run-integration-tests
```

## License

Copyright © 2023 lambdaforge UG (haftungsbeschränkt)

This program and the accompanying materials are made available under the terms of the Eclipse Public License 1.0.
