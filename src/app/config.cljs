(ns app.config
  (:require [cljs.reader :as reader]))

(defn valid?
  [config])

(defn encode
  "Returns a URL friendly encoding of configuration."
  [config]
  (-> config
      pr-str
      js/encodeURIComponent))

(defn decode [encoded-config]
  (-> encoded-config
      js/decodeURIComponent
      reader/read-string))

(comment

  (encode {:a 1})

  (decode (encode {:a 1}))

  ())
