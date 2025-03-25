(ns app.ui
  (:require [uix.core :refer [defui $]]))

(defui button [{:keys [on-click class children]}]
  ($ :button {:on-click on-click
              :class (merge
                      '[text-white
                        bg-gradient-to-r
                        bg-gradient-to-r
                        "from-[#A2DA5F]"
                        from-10%
                        "to-[#97B3F8]"
                        hover:bg-gradient-to-l
                        transition
                        ease-in-out
                        font-medium
                        rounded-full
                        text-sm
                        px-3.5
                        py-2
                        text-center
                        select-none
                        me-2
                        mb-2] class)}
     children))

(defui heading [{:keys [children class]}]
  ($ :h1 {:class (merge
                  '[text-transparent
                    bg-clip-text
                    bg-gradient-to-r
                    "from-[#A2DA5F]"
                    from-0%
                    "to-[#97B3F8]"
                    mb-4
                    text-6xl
                    text-center
                    font-extrabold
                    text-gray-900] class)}
     children))

(defui p [{:keys [children class]}]
  ($ :p {:class (merge
                 '[my-2
                   text-lg
                   font-normal
                   text-gray-700
                   dark:text-gray-100] class)}
     children))

(defui a [{:keys [href children class]}]
  ($ :a {:href href
         :target "_blank"
         :class (merge '[hover:underline] class)}
     children))
