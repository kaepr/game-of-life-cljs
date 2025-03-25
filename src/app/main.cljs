(ns app.main
  (:require [app.components :refer [app]]
            [uix.dom]
            [uix.core :refer [$]]))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn ^:export ^:dev/after-load init []
  (uix.dom/render-root ($ app) root))
