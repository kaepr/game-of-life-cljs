(ns app.components
  (:require [app.ui :as ui]
            [uix.core :refer [$ use-state defui]]))

(defui counter []
  (let [[count set-count!] (use-state 0)]
    ($ :.flex.flex-row.gap-12.items-center.ml-4.mt-2
       ($ :pre.text-3xl.mb-2.w-4.dark:text-white count)
       ($ :.gap-2
          ($ ui/button {:on-click #(set-count! inc)} "+")
          ($ ui/button {:on-click #(set-count! dec)} "-")))))

(defui app []
  ($ :main.flex.flex-col.gap-8.min-h-screen.justify-center.items-center.dark:bg-neutral-950.px-12.py-12
     ($ ui/heading "ClojureScript Starter"
        ($ ui/p {:class '[my-2]} "Made with asdlasd"
           ($ ui/a {:href "https://github.com/pitch-io/uix"} "UIx, ")
           ($ ui/a {:href "https://tailwindcss.com"} "TailwindCSS, ")
           ($ ui/a {:href "https://github.com/thheller/shadow-cljs"} "ShadowCLJS, ")
           ($ ui/a {:href "https://bun.sh"} "and BunJS.")))
     ($ :.bg-neutral-100.dark:bg-neutral-900.rounded-xl.py-8.px-12
        ($ counter))
     ($ ui/button {:class '[my-20 rounded-xl]
                   :on-click #(js/window.open "https://github.com/harismh/utsb-cljs-starter")}
        "Clone Repo")
     ($ :pre.text-lg.text-gray-600.dark:text-gray-400.text-center.text-wrap
        "React 18.20 * UIx 1.1.1 * TailwindCSS 3.4.13 * ShadowCLJS 2.28.16 * Bun 1.1.24")))
