(ns datahike-s3.core
  (:require [datahike.store :refer [empty-store delete-store connect-store default-config config-spec release-store store-identity]]
            [datahike.config :refer [map-from-env]]
            [konserve-s3.core :as k]
            [clojure.spec.alpha :as s]))

(defmethod store-identity :s3 [store-config]
  [:s3 (:region store-config) (:bucket store-config)])

(defmethod empty-store :s3 [store-config]
  (k/connect-store store-config))

(defmethod delete-store :s3 [store-config]
  (k/delete-store store-config))

(defmethod connect-store :s3 [store-config]
  (k/connect-store store-config))

(defmethod default-config :s3 [config]
  (merge
   (map-from-env :datahike-store-config {:bucket "datahike"})
   config))

(s/def :datahike.store.s3/backend #{:s3})
(s/def :datahike.store.s3/bucket string?)
(s/def :datahike.store.s3/region string?)
(s/def :datahike.store.s3/access-key string?)
(s/def :datahike.store.s3/secret string?)
(s/def ::s3 (s/keys :req-un [:datahike.store.s3/backend]
                    :opt-un [:datahike.store.s3/region
                             :datahike.store.s3/bucket
                             :datahike.store.s3/access-key
                             :datahike.store.s3/secret]))

(defmethod config-spec :s3 [_] ::s3)

(defmethod release-store :s3 [_ store]
  (k/release store {:sync? true}))
