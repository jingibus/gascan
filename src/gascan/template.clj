(ns gascan.template
  (:require [hiccup.core :as hc]))

(defn enframe
  [title body & {:keys [up-link]}]
  (let [main-bg-color "#dcc48d"
        secondary-bg-color "#dca200"
        secondary-bg-color-but-darker "#a07601"
        accent-color "#1a0aa2"
        top-banner-top-margin "10px"
        top-banner-height "50px"
        top-banner-bottom-margin "10px"
        left-accent-strip-left-margin "50px"
        left-accent-strip-width "10px"
        left-accent-strip-right-margin "20px"
        main-content-width "400px"
        main-content-right-margin "50px"
        main-content-bottom-padding "50px"]
    (hc/html
     [:html
      [:head
       [:link {:href "https://fonts.googleapis.com/css2?family=Domine&display=swap" :rel "stylesheet"}]
       [:style (str  "h1, h2, h3 { font-family: 'Domine', serif; } "
                     "a:link { color: " secondary-bg-color-but-darker "; } "
                     "a:visited { color: " accent-color "; } ")]
       [:title title]]
      [:body {:style (str "margin: 8px; display: flex; flex-direction: row; ")
              }
       [:div {:style (str "flex: 1 50000 " left-accent-strip-left-margin "; "
                          "display: flex; flex-direction: column; "
                          "background-color: " main-bg-color )}
        [:div {:style (str "flex: 0 0 " top-banner-height "; "
                           "margin-top: " top-banner-top-margin "; "
                           "background-color: " secondary-bg-color "; ")}]]
       [:div {:style (str "flex: 0 0 " left-accent-strip-width "; "
                          "background-color: " accent-color)}]
       
       [:div {:style (str "flex: 0 1 " main-content-width "; "
                          "display: flex; flex-direction: column; "
                          "background-color: " main-bg-color "; ")}
        [:div {:style (str "flex: 0 0 " top-banner-height "; "
                           "margin-top: " top-banner-top-margin "; "
                           "background-color: " secondary-bg-color "; ")}]
        [:div {:style (str "padding-left: " left-accent-strip-right-margin "; "
                           "padding-top: 0px; "
                           "padding-bottom: " main-content-bottom-padding "; ")}
         [:h1 title]
         body
         (when up-link 
           [:div {:style "justify-content:center; display: flex"} up-link])]]
       [:div {:style (str "flex: 2 500 " main-content-right-margin "; "
                          "display: flex; flex-direction: column; "
                          "background-color: " main-bg-color)}
        [:div {:style (str "flex: 0 0 " top-banner-height "; "
                           "margin-top: " top-banner-top-margin "; "
                           "background-color: " secondary-bg-color "; ")}]        ]
       ]])))
