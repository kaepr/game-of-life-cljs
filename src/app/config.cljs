(ns app.config
  (:require [cljs.reader :as reader]
            [app.game :as game]
            [app.settings :as settings]))

(def config-keys [:board :max-rows :max-cols :cell-size :grid-type])

(def initial-config {:board (game/empty-board settings/default-rows settings/default-cols)
                     :max-rows settings/default-rows
                     :max-cols settings/default-cols
                     :cell-size settings/default-cell-size
                     :grid-type :square})

(defn- validate-keys [m required-keys]
  (let [missing-keys (remove #(contains? m %) required-keys)]
    (if (seq missing-keys)
      (throw (ex-info "Missing required configuration parameters." {}))
      m)))

(defn- validate-data [{:keys [board max-rows max-cols cell-size grid-type] :as c}]
  (assert (number? max-rows) "Expected rows to be a number.")
  (assert (number? max-cols) "Expected columns to be a number.")
  (assert (number? cell-size) "Expected cell size to be a number.")
  (assert (contains? settings/valid-grid-types grid-type) "Grid type is not valid.")
  (assert (vector? board) "Expected board to a be vector.")
  (assert (= (count board) (* max-rows max-cols)) "Expected board size to match rows * cols.")
  (assert (every? boolean? board) "Expected board to only contain booleans.")
  c)

(defn encode
  "Returns a URL friendly encoding of configuration."
  [config]
  (-> config
      (select-keys config-keys)
      ((fn [x] (validate-keys x config-keys)))
      validate-data
      pr-str
      js/encodeURIComponent))

(defn decode [encoded-config]
  (-> encoded-config
      js/decodeURIComponent
      reader/read-string
      ((fn [x] (validate-keys x config-keys)))
      validate-data))

(comment

  (encode {:a 1})

  (encode initial-config)

  (decode (encode {:a 1}))

  (decode (encode initial-config))

  ())
