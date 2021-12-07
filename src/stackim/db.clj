(ns stackim.db)

(def db-options
  {:access-key (System/getenv "AWS_ACCESS_KEY_ID")
   :secret-key (System/getenv "AWS_SECRET_ACCESS_KEY")})
