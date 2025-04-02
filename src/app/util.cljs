(ns app.util
  (:require [clojure.string :as str]))

(defn get-base-url
  "Returns the base URL of the current page, including protocol, hostname, and port.
   Example: https://example.com:8080"
  []
  (let [location (.-location js/window)
        protocol (.-protocol location)
        hostname (.-hostname location)
        port (.-port location)
        port-str (if-not (str/blank? port)
                   (str ":" port)
                   "")]
    (str protocol "//" hostname port-str)))

(defn parse-seed-from-url [url]
  (try
    (let [url-obj (js/URL. url)
          params (js/URLSearchParams. (.-search url-obj))
          seed (.get params "seed")]
      (if (and seed (not (str/blank? seed)))
        {:base-url (str (.-protocol url-obj) "//" (.-host url-obj))
         :seed seed}
        false))
    (catch :default _
      false)))

(defn copy-to-clipboard! [text cb]
  (try
    (-> (.writeText js/navigator.clipboard text)
        (.then (fn [_] (cb)))
        (.catch (fn [_]
                  ;; Fallback for browsers without clipboard API
                  (let [temp-element (js/document.createElement "textarea")]
                    (set! (.-value temp-element) text)
                    (.appendChild js/document.body temp-element)
                    (.select temp-element)
                    (let [success (js/document.execCommand "copy")]
                      (.removeChild js/document.body temp-element)
                      (cb))))))
    (catch :default _
      ;; Pure fallback approach for environments where clipboard API is completely unavailable
      (let [temp-element (js/document.createElement "textarea")]
        (set! (.-value temp-element) text)
        (.appendChild js/document.body temp-element)
        (.select temp-element)
        (let [success (js/document.execCommand "copy")]
          (.removeChild js/document.body temp-element)
          (cb))))))
