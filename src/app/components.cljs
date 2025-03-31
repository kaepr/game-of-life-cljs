(ns app.components
  (:require
   [app.game :as game]
   [uix.core :as uix :refer [$ defui]]))

(def grid-types
  {:square {:name "Square Tiles"
            :neighbors [[-1 -1] [-1 0] [-1 1]
                        [0 -1]  [0 1]
                        [1 -1]  [1 0]  [1 1]]}})

(defn- to-fill [alive?]
  (if alive? "black" "white"))

(def grid-type->draw-f
  {:square (fn [ctx row col cell-size alive?]
             (let [x (* row cell-size)
                   y (* col cell-size)]
               (set! (.-fillStyle ctx) (to-fill alive?))
               (.fillRect ctx x y cell-size cell-size)
               (set! (.-strokeStyle ctx) "black")
               (.strokeRect ctx x y cell-size cell-size)))
   :triangle (fn [ctx row col cell-size alive?])
   :hexagon (fn [ctx row col cell-size alive?])})

(def grid-type->get-cell
  {:square (fn [x y cell-size max-cols]
             (let [row (js/Math.floor (/ y cell-size))
                   col (js/Math.floor (/ x cell-size))]
               (game/coordinates->index col row max-cols)))
   :triangle ()
   :hexagon ()})

(defui header []
  ($ :header.text-left.px-8.pt-8
     ($ :h1.text-3xl.font-bold.mb-2 "Conway's Game of Life")
     ($ :p.text-lg "Implemented using "
        ($ :a.link.text-lg {:href "https://clojurescript.org/index"} "ClojureScript")
        " and "
        ($ :a.link.text-lg {:href "https://github.com/pitch-io/uix"} "UIX")
        ".")
     ($ :a.link.text-lg {:href "https://github.com/kaepr/game-of-life-cljs"} "Github")))

(defui tile-selector [{:keys [running change-grid-type]}]
  ($ :select.select
     {:disabled running
      :default-value "square"
      :on-change #(change-grid-type (keyword (.. % -target -value)))}
     ($ :option {:key "square" :value "square"} "Square")
     ($ :option {:key "triangle" :value "triangle"} "Triangle")
     ($ :option {:key "hexagon" :value "hexagon"} "Hexagon")))

(defui settings [{:keys [running change-grid-type] :as props}]
  ($ :div.px-8.py-4
     ($ tile-selector props)))

(defui action-panel [{:keys [running start-simulation stop-simulation]}]
  ($ :div.px-8.join
     ($ :button.btn.join-item {:disabled running
                               :on-click start-simulation} "Start")
     ($ :button.btn.join-item {:disabled (not running)
                               :on-click stop-simulation} "Stop")))

(defui board-view [{:keys [width height on-click canvas-ref]}]
  ($ :div.w-full.max-w-full.py-4.px-8.overflow-auto
     ($ :canvas.block.cursor-pointer.mx-auto.border
        {:ref canvas-ref
         :width width
         :height height
         :on-click on-click})))

(defn- clear-canvas [{:keys [canvas]}]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) "white")
    (.fillRect ctx 0 0 (.-width canvas) (.-height canvas))))

(defn- draw-canvas [{:keys [canvas cell-size board max-cols draw-f]}]
  (let [ctx (.getContext canvas "2d")]
    (dotimes [idx (count board)]
      (let [[r c] (game/index->coordinates idx max-cols)
            alive? (game/alive? (game/get-cell board idx))]
        (draw-f ctx r c cell-size alive?)))))

(defui app []
  (let [[config set-config] (uix/use-state {:max-rows game/default-rows
                                            :max-cols game/default-cols
                                            :cell-size game/default-cell-size
                                            :running false
                                            :grid-type :square
                                            :speed 100
                                            :generation 0
                                            :board (game/empty-board game/default-rows game/default-cols)})
        {:keys [max-rows speed max-cols cell-size running grid-type board]} config
        canvas-dimensions (case grid-type
                            :square {:width (* cell-size max-cols)
                                     :height (* cell-size max-rows)}
                            :triangle {:width ()
                                       :height ()}
                            :hexagon {:width {}
                                      :height ()})
        animation-request-id-ref (uix/use-ref nil)
        canvas-ref (uix/use-ref nil)
        toggle-cell (fn [idx]
                      (set-config
                       (fn [c]
                         (update c
                                 :board
                                 (fn [b] (assoc b idx (not (get b idx false))))))))
        change-grid-type (fn [grid-type]
                           (set-config (fn [c]
                                         (-> c
                                             (assoc :grid-type grid-type)
                                             (assoc :board (game/empty-board (:max-rows c) (:max-cols c)))
                                             (assoc :generation 0)
                                             (assoc :running false)))))
        start-simulation (fn [])
        stop-simulation (fn [])
        handle-canvas-click (fn [e]
                              (when-let [canvas (.-current canvas-ref)]
                                (let [rect (.getBoundingClientRect canvas)
                                      x (- (.-clientX e) (.-left rect))
                                      y (- (.-clientY e) (.-top rect))
                                      get-cell-f (get grid-type->get-cell grid-type)
                                      idx (get-cell-f x y cell-size max-cols)]
                                  (when (and (>= idx 0) (< idx (count board)))
                                    (toggle-cell idx)))))]
    (uix/use-effect
     (fn []
       (when-let [canvas (.-current canvas-ref)]
         (let [draw-f (get grid-type->draw-f grid-type)]
           (clear-canvas {:canvas canvas})
           (draw-canvas {:canvas canvas
                         :cell-size cell-size
                         :board board
                         :draw-f draw-f
                         :max-cols max-cols})))
       (fn []))
     [board grid-type max-rows cell-size max-cols])
    (uix/use-effect
     (fn []
       (let [timer-id (when running
                        (js/setTimeout
                         #(set-config (fn [c]
                                        (-> c
                                            (update :generation inc)
                                            (update :board (game/next-gen-board c)))))
                         speed))]
         #(when timer-id (js/clearTimeout timer-id)))))
    ($ :div.h-screen.bg-white-50
       ($ header)
       ($ settings {:change-grid-type change-grid-type})
       ($ action-panel {:running running
                        :stop-simulation stop-simulation
                        :start-simuation start-simulation})
       ($ board-view {:canvas-ref canvas-ref
                      :width (:width canvas-dimensions)
                      :height (:height canvas-dimensions)
                      :on-click handle-canvas-click}))))
