(ns stackim.db)

(def region
  (or (System/getenv "AWS_DEFAULT_REGION") "us-west-1"))

(def db-options
  {:access-key (System/getenv "AWS_ACCESS_KEY_ID")
   :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")
   :endpoint (str "https://dynamodb." region ".amazonaws.com")})

(def table
  (or (System/getenv "STACKIM_TAGS_TABLE") "stackim-tags"))
