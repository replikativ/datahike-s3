(ns datahike-s3.core-test
  (:require
   #?(:cljs [cljs.test    :as t :refer-macros [is deftest]]
      :clj  [clojure.test :as t :refer        [is deftest]])
   [datahike.api :as d]
   [datahike-s3.core]))

(deftest ^:integration test-s3
  (let [config {:store {:backend :s3
                        :region   "us-west-1"
                        :bucket   "datahike-server-test"}
                :schema-flexibility :read
                :keep-history? false}
        _ (d/delete-database config)]
    (is (not (d/database-exists? config)))
    (let [_ (d/create-database config)
          conn (d/connect config)]

      (d/transact conn [{:db/id 1, :name  "Ivan", :age   15}
                        {:db/id 2, :name  "Petr", :age   37}
                        {:db/id 3, :name  "Ivan", :age   37}
                        {:db/id 4, :age 15}])
      (is (= (d/q '[:find ?e :where [?e :name]] @conn)
             #{[3] [2] [1]}))

      (d/release conn)
      (is (d/database-exists? config))
      (d/delete-database config)
      (is (not (d/database-exists? config))))))

