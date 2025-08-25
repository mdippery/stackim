(ns stackim.env)

(def ^:dynamic *env* (System/getenv))

(defn getenv [k default] (or (get *env* k) default))
