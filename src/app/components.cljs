(ns app.components
  (:require [app.ui :as ui]
            [uix.core :refer [$ use-state defui]]))

(defui my-button []
  ($ :div.btn
     "I'm a button"))

(defui app []
  ($ :div.h-screen.flex.flex-col.items-center.justify-center.gap-4
     ($ :h1.text-xl.font-semibold "Welcome to my app")
     ($ my-button)))
