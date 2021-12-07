(ns stackim.db)


(defn- env? [var]
  (not (nil? (System/getenv var))))

(def region
  (or (System/getenv "AWS_DEFAULT_REGION") "us-west-1"))

(def ^:private local-options
  {:access-key (System/getenv "AWS_ACCESS_KEY_ID")
   :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")
   :endpoint (str "https://dynamodb." region ".amazonaws.com")})

(def ^:private lambda-options {})

(def db-options
  (if (env? "AWS_ACCESS_KEY_ID") local-options lambda-options))

(def table
  (or (System/getenv "STACKIM_TAGS_TABLE") "stackim-tags"))
