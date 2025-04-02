(ns app.components
  (:require
   [app.game :as game]
   [app.settings :as settings]
   [app.config :as app-config]
   [uix.core :as uix :refer [$ defui]]
   [app.util :as util]))

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
  ($ :label.select
     ($ :span.label.text-black "Shape")
     ($ :select
        {:disabled running
         :default-value "square"
         :on-change #(change-grid-type (keyword (.. % -target -value)))}
        ($ :option {:key "square" :value "square"} "Square")
        #_($ :option {:key "triangle" :value "triangle"} "Triangle")
        #_($ :option {:key "hexagon" :value "hexagon"} "Hexagon"))))

(defui row-selector [{:keys [max-rows handle-row-change]}]
  ($ :label.input
     ($ :span.label.text-black "Rows")
     ($ :input.validator {:type "number"
                          :value max-rows
                          :required true
                          :min 1
                          :on-change (fn [e]
                                       (let [n (js/Number (.. e -target -value))]
                                         (handle-row-change n)))})))

(defui col-selector [{:keys [max-cols handle-col-change]}]
  ($ :label.input
     ($ :span.label.text-black "Columns")
     ($ :input.validator {:type "number"
                          :value max-cols
                          :required true
                          :min 1
                          :on-change (fn [e]
                                       (let [n (js/Number (.. e -target -value))]
                                         (handle-col-change n)))})))

(defui grid-number-selector [{:keys [max-cols handle-col-change handle-row-change]}]
  ($ :label.input
     ($ :span.label.text-black "Grids")
     ($ :input.validator {:type "number"
                          :value max-cols
                          :required true
                          :min 1
                          :on-change (fn [e]
                                       (let [n (js/Number (.. e -target -value))]
                                         (handle-col-change n)
                                         (handle-row-change n)))})))

(defui cell-size-selector [{:keys [cell-size handle-cell-size-change]}]
  ($ :label.input
     ($ :span.label.text-black "Cell Size (in px)")
     ($ :input.validator {:type "number"
                          :value cell-size
                          :required true
                          :min 1
                          :on-change (fn [e]
                                       (let [n (js/Number (.. e -target -value))]
                                         (handle-cell-size-change n)))})))

(defui settings-panel [{:keys [running change-grid-type handle-row-change
                               max-rows
                               max-cols
                               handle-cell-size-change
                               handle-col-change
                               cell-size] :as _props}]
  ($ :div.px-8.py-4.flex.flex-col.gap-2
     ($ tile-selector {:change-grid-type change-grid-type
                       :running running})
     ;; ($ row-selector {:handle-row-change handle-row-change
     ;;                  :max-rows max-rows})
     ($ grid-number-selector {:max-cols max-cols
                              :handle-row-change handle-row-change
                              :handle-col-change handle-col-change})
     ;; ($ col-selector {:handle-col-change handle-col-change
     ;;                  :max-cols max-cols})
     ($ cell-size-selector {:handle-cell-size-change handle-cell-size-change
                            :cell-size cell-size})))

(defui action-panel [{:keys [running start-simulation stop-simulation reset-simulation]}]
  ($ :div.px-8.flex.flex-col.gap-2
     ($ :button.btn.join-item.max-w-md.block {:disabled running
                                              :on-click start-simulation} "Start")
     ($ :button.btn.join-item.max-w-md.block {:disabled (not running)
                                              :on-click stop-simulation} "Stop")
     ($ :button.btn.join-item.max-w-md.block {:on-click reset-simulation} "Reset")))

(defui share-panel [{:keys [] :as c}]
  (let [seed (app-config/encode c)
        url (util/get-base-url)
        seed-url (str url "?seed=" seed)]
    ($ :div.px-8.flex.flex-col.gap-2.pb-2
       ($ :button.btn.max-w-md.block
          {:on-click (fn [_] (util/copy-to-clipboard! seed-url #(js/console.log "Copied to clipboard")))}
          "Share (copies link to clipboard)"))))

(defui board-view [{:keys [width height on-click canvas-ref generation]}]
  ($ :div.w-full.max-w-full.py-4.px-8.overflow-auto
     ($ :div.stats
        ($ :div.stat
           ($ :div.stat-value.text-lg (str "Generation #" generation))))
     ($ :canvas.block.cursor-pointer.mx-auto.border
        {:ref canvas-ref
         :width width
         :height height
         :style {:width (str width "px")
                 :height (str height "px")}
         :on-click on-click})))

(defn- clear-canvas [{:keys [canvas]}]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) "white")
    (.fillRect ctx 0 0 (.-width canvas) (.-height canvas))
    (set! (.-strokeStyle ctx) "#ff0000")))

(defn- draw-canvas [{:keys [canvas cell-size board max-rows max-cols draw-f]}]
  (let [ctx (.getContext canvas "2d")]
    (dotimes [row max-rows]
      (dotimes [col max-cols]
        (let [idx (+ (* row max-cols) col)
              alive? (game/alive? (game/get-cell board idx))]
          (draw-f ctx row col cell-size alive?))))))

(defn- calc-canvas-dimensions [grid-type cell-size max-rows max-cols]
  (case grid-type
    :square {:width (* cell-size max-cols)
             :height (* cell-size max-rows)}
    :triangle {:width ()
               :height ()}
    :hexagon {:width {}
              :height ()}))

(defn setup-canvas [canvas {:keys [width height]}]
  (set! (.-width canvas) width)
  (set! (.-height canvas) height))

(defn- parse-starting-config-from-url []
  (if-let [seed (util/parse-seed-from-url js/window.location)]
    (try
      (app-config/decode (:seed seed))
      (catch :default _
        (set! (.-location js/window) (util/get-base-url))))
    app-config/initial-config))

(defui app []
  (let [{:keys [max-rows
                max-cols
                board
                grid-type
                cell-size]} (parse-starting-config-from-url)
        [config set-config] (uix/use-state {:max-rows max-rows
                                            :max-cols max-cols
                                            :cell-size cell-size
                                            :running false
                                            :fps 0
                                            :fps-target 5
                                            :canvas-dimensions (calc-canvas-dimensions
                                                                :square
                                                                settings/default-cell-size
                                                                settings/default-rows
                                                                settings/default-cols)
                                            :grid-type grid-type
                                            :generation 0
                                            :board board})
        {:keys [max-rows max-cols
                cell-size running
                grid-type board
                generation
                canvas-dimensions
                fps-target]} config
        animation-request-id-ref (uix/use-ref nil)
        last-update-time-ref (uix/use-ref 0)
        fps-interval (/ 1000 fps-target)
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
        start-simulation #(set-config
                           (fn [c]
                             (-> c
                                 (assoc :running true))))
        stop-simulation #(set-config
                          (fn [c]
                            (-> c
                                (assoc :running false))))
        handle-row-change (fn [rows]
                            (set-config
                             (fn [{:keys [max-cols grid-type cell-size] :as c}]
                               (-> c
                                   (assoc :max-rows rows)
                                   (assoc :running false)
                                   (assoc :canvas-dimensions (calc-canvas-dimensions grid-type cell-size rows max-cols))
                                   (assoc :board (game/empty-board rows max-cols))
                                   (assoc :generation 0)))))
        handle-col-change (fn [cols]
                            (set-config
                             (fn [{:keys [max-rows grid-type cell-size] :as c}]
                               (-> c
                                   (assoc :max-cols cols)
                                   (assoc :canvas-dimensions (calc-canvas-dimensions grid-type cell-size max-rows cols))
                                   (assoc :running false)
                                   (assoc :board (game/empty-board max-rows cols))
                                   (assoc :generation 0)))))
        handle-speed-change #()
        handle-cell-size-change (fn [cell-sz]
                                  (set-config
                                   (fn [{:keys [max-rows max-cols] :as c}]
                                     (-> c
                                         (assoc :cell-size cell-sz)
                                         (assoc :running false)
                                         (assoc :board (game/empty-board max-rows max-cols))
                                         (assoc :generation 0)))))
        reset-simulation #(set-config
                           (fn [c]
                             (-> c
                                 (assoc :running false)
                                 (assoc :board (game/empty-board (:max-rows c) (:max-cols c)))
                                 (assoc :generation 0))))
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
           (set! (.-width canvas) (:width canvas-dimensions))
           (set! (.-height canvas) (:height canvas-dimensions))
           (setup-canvas canvas canvas-dimensions)
           (clear-canvas {:canvas canvas})
           (draw-canvas {:canvas canvas
                         :cell-size cell-size
                         :board board
                         :draw-f draw-f
                         :max-rows max-rows
                         :max-cols max-cols})))
       (fn []))
     [board grid-type max-rows cell-size max-cols canvas-dimensions])
    (uix/use-effect
     (fn []
       (letfn [(animate [timestamp]
                 (when running
                   (let [elapsed (- timestamp (.-current last-update-time-ref))]
                     (when (>= elapsed fps-interval)
                       (set! (.-current last-update-time-ref)
                             (- timestamp (mod elapsed fps-interval)))
                       (let [fps (js/Math.round (/ 1000 elapsed))]
                         (set-config
                          (fn [c]
                            (-> c
                                (assoc :fps fps)
                                (update :generation inc)
                                (assoc :board (game/next-gen-board c)))))))
                     (let [id (js/requestAnimationFrame animate)]
                       (set! (.-current animation-request-id-ref) id)))))]
         (when running
           (set! (.-current last-update-time-ref) (js/performance.now))
           (let [id (js/requestAnimationFrame animate)]
             (set! (.-current animation-request-id-ref) id))))
       (fn []
         (when (.-current animation-request-id-ref)
           (js/cancelAnimationFrame (.-current animation-request-id-ref))
           (set! (.-current animation-request-id-ref) nil)
           (set! (.-current last-update-time-ref) 0))))
     [running fps-interval])
    ($ :div.h-screen.bg-white-50
       ($ header)
       ($ settings-panel {:change-grid-type change-grid-type
                          :max-cols max-cols
                          :max-rows max-rows
                          :cell-size cell-size
                          :handle-cell-size-change handle-cell-size-change
                          :handle-row-change handle-row-change
                          :handle-col-change handle-col-change})
       ($ share-panel config)
       ($ action-panel {:running running
                        :reset-simulation reset-simulation
                        :stop-simulation stop-simulation
                        :start-simulation start-simulation})
       ($ board-view {:canvas-ref canvas-ref
                      :generation generation
                      :width (:width canvas-dimensions)
                      :height (:height canvas-dimensions)
                      :on-click handle-canvas-click}))))
