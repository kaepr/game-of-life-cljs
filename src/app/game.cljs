(ns app.game
  (:require
   [app.settings :as settings]))

(def grid-configs
  {:square {:name "Square"
            :neighbors [[-1 -1] [-1 0] [-1 1]
                        [0 -1]  #_cell [0 1]
                        [1 -1]  [1 0]  [1 1]]}})

(defn dead-cell [] false)

(defn alive-cell [] true)

(defn alive? [cell] cell)

(defn coordinates->index [row col max-cols]
  (+ col (* row max-cols)))

(defn index->coordinates [idx max-cols]
  [(quot idx max-cols) (rem idx max-cols)])

(defn get-cell [board idx]
  (get board idx false))

(defn update-cell [board [row col] max-cols cell]
  (assoc board (coordinates->index row col max-cols) cell))

(defn empty-board [max-rows max-cols]
  (vec (repeat (* max-rows max-cols) (dead-cell))))

(defn get-neighbors
  "Returns all neighbors using 1d based vector indexing."
  [neighbors [row col] max-rows max-cols]
  (let [valid? (fn [r c] (and (>= r 0) (>= c 0) (< r max-rows) (< c max-cols)))]
    (->> neighbors
         (map (fn [[dr dc]]
                (let [r (+ row dr)
                      c (+ col dc)]
                  (when (valid? r c)
                    (coordinates->index r c max-cols)))))
         (filter some?))))

(defn count-neighbors [board grid-type [row col] max-rows max-cols]
  (assert (grid-type settings/valid-grid-types) "Invalid grid type passed.")
  (let [grid-config (get grid-configs grid-type)
        neighbors (:neighbors grid-config)]
    (condp = grid-type
      :square (let [neighbor-cells (get-neighbors neighbors [row col] max-rows max-cols)
                    alive-cells? (filter alive? (map #(get-cell board %) neighbor-cells))]
                (count alive-cells?)))))

(defn cell-transition [cell count-neighbors]
  (if (or (and (alive? cell) (or (= count-neighbors 2) (= count-neighbors 3)))
          (and (not (alive? cell)) (= count-neighbors 3)))
    (alive-cell)
    (dead-cell)))

(defn next-gen-board [{:keys [board grid-type max-rows max-cols] :as state}]
  (let [next-board (transient board)
        size (* max-rows  max-cols)
        _ (dotimes [idx size]
            (let [coords  (index->coordinates idx max-cols)
                  cell (get-cell board idx)
                  neighbor-count (count-neighbors board grid-type coords max-rows max-cols)]
              (assoc! next-board idx (cell-transition cell neighbor-count))))]
    (persistent! next-board)))

(comment

  (empty-board 10 10)

  (next-gen-board {:board (empty-board 10 10)
                   :max-rows 10
                   :max-cols 10
                   :grid-type :square})

  (next-gen-board {:board (assoc (empty-board 10 10) 0 true)
                   :max-rows 10
                   :max-cols 10
                   :grid-type :square})

  ())
