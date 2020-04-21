(ns gascan.what-it-is-view
  (:require [hiccup.core :as hc]
            [gascan.debug :refer :all]
            [gascan.template :as tmpl]
            [gascan.view-common :as view-common]
            [gascan.routing :as routing]
            [clojure.string :as string]))

(def whats-it-ises
  {:reactionary
   (hc/html
    [:p
     "The Gas Can is a reactionary web site built by Bill Phillips as a place to publish things he makes."]
    [:p
     "\"Hoo boy,\" says reader. "
     "\"That's a lot to unpack there. "
     "Let's take this one phrase at a time.\" "]
    [:h3
     "What is that you mean by \"reactionary\"? "] 
    [:p
     "\"Isn't a reactionary someone who says, 'Hey! Turn that Simpsons devil music off! You're turning my children into little sugar gliders, tucked in the pocket of a culture I don't understand!'\" "
     "Or, 'Democracy was a mistake!'\" "
     ]
    [:p 
     "Well, sure. It sounds bad when you say it like that. But maybe a little Howard Beale isn't so bad when taken as inspiration."]
    [:p
     "I was sick and tired of everything I write being plugged into a web site somebody else owns. "
     "I was sick and tired of everything being too damned easy all the time, so that you end up writing what you write not because it's what you wanted to write, but because it was the easy thing to write in the place you were writing it. "]
    [:p
     "Have you ever responded to someone because their writing was right in front of you, right alongside a little square to type your response in? "
     "I sure have."]
    [:p
     "Oftentimes that's okay, but telling a friend or relative of ages ago or a perfect stranger how awful they are for no other reason than because that was the easiest choice to make is no way to live."]
    [:p
     "But maybe it all came down to the fact that I woke up one day and I was making things and I cared more about making them in a way I liked than anything else."]
    [:p
     "You can find more writing about what this is under \"meta\". That's what it's there for, anyway. "]
    [:p
     "\"Okay,\" says reader. \"Now what about Bill Phillips?\" "]
    [:h3
     "Who is \"Bill Phillips\"? "]
    [:p
     "Bill Phillips is your author. "
     "He pays the bills by writing software; since 2010 that software has been mostly Android. "
     "As you can see, he is not a web developer, and this is not his business card. "
     "You can find him on Amazon in the books section if you scroll past a sufficient amount of beefcake."]
    [:p
     "He came into the world in Garland, Texas, grew into adulthood in Jackson, Mississippi, got his higher education in Atlanta, Georgia, and stayed there by his own voliton for a long time. "]
    [:p
     "Having let go of that will for a spell he moved to San Francisco, a place that perhaps even in these dark days has some pull for pragmatic idealists. "
     "It no longer seems guaranteed that the fog will outlast the burritos, but I continue to bet on the fog."]
    [:h3
     "\"How do I best get in touch with Bill Phillips?\""]
    [:p
     "I'm not sure. I do a lot of writing, and that seems to help."]
    [:p
     "\"Well okay, then,\" says the reader. \"But I have one last question:\"  "]
    [:h3 "\"What sort of things do you make, anyway?\""]
    [:p
     "Empirically speaking, I seem to make little blog posts about programming and life, and I write music. "])
   :art-project
   (hc/html
    [:p
     "The Gas Can is an ongoing art project by programmer, musician, writer, doctor, nuclear physicist, and theoretical philosopher Bill Phillips. "
     "By creating this place and working within it, he seeks to answer the questions that have long plagued him: "
     [:ul
      [:li "What purpose should each day serve? "]
      [:li "What is the proper relation between God and man?"]
      [:li "What laws govern the popularity of music in high society?"]
      [:li "In low society?"]
      [:li "What should be private? What should be public?"]
      [:li "How can we best address the Great Problems of the day?"]
      [:li "Is there a God? Why? Or why not?"]
      [:li "What kind of web site is required to solve the economic problem of connecting information producers with information consumers?"]
      ]]
    [:p
     "Eager grant writers are invited to reach out to him on LinkedIn regarding paid residency opportunities. "
     "Interpreters of his work are invited to contemplate it without any further explanation."])
   
   :goals
   (hc/html
    [:p
     "The Gas Can is a web site I built because I wanted a place to publish things. "
     "Well, that's how it started, anyway..."]
    [:p
     "As I've gotten into the project more, I've found that there's a lot of fun to be had from a programming point of view. "
     "So some of the things you'll see here will just be programming fun. "
     "I'm not a web developer by trade, so it feels nice to build something that I'm not thinking of as a business card. "]
    [:p
     "But other things I'll publish here will be writing, or music, or who knows what. "
     "Maybe that writing will be professionally relevant. "
     "Maybe it will be personal. "
     "Maybe I'll get bored and won't publish anything at all. "]
    [:p
     "Whatever happens, it will be something that I made from start to finish. "
     "And whenever you check it out, it will be here. "
     "I won't make it any easier or harder for anyone. "
     "There will be no likes, no shares, no comments, no moderation. "]
    [:p
     "This is my cassette tape. "
     "And if you don't listen to music on tapes anymore, well that's kind of the idea."]
    [:i
     [:p {:style "text-align: right"}
      (interpose [:br] ["Bill Phillips"
                        "looking out over the Fillmore"
                        "nighttime"
                        "San Francisco"
                        "4/20/2020"])
      ]])

   :technical
   (hc/html
    [:p
     "The Gas Can is a Clojure publishing platform built and used by Bill Phillips. "
     "It was originally designed around publishing content written in Scrivener and exported to MultiMarkdown."]
    [:p
     "You can find the source code to The Gas Can at " [:a {:href "https://github.com/jingibus/gascan"} "GitHub"] "."])

})

(defn meta-what-it-is
  []
  (let [whats-it-is (->> whats-it-ises
                         keys
                         (map name)
                         (map #(str "\"" % "\""))
                         (string/join ", "))]
    (hc/html
     [:html
      [:head
       [:script {:type "text/javascript"}
        (str "
function nav() {
  whats = [ " whats-it-is " ]
  what = whats[Math.floor(Math.random() * whats.length)];
  window.location.href = \"/what-it-is?which-what=\" + what;
}

window.onload = nav;
")]]
      [:body
]])))

(defn view
  [sess query-params]
  (let [{which-what :which-what} 
        (routing/what-it-is-query-params->map query-params)
        the-what (get whats-it-ises which-what)]
    (pprint-symbols which-what)
    (cond (not which-what)
          (meta-what-it-is)
          the-what
          (tmpl/enframe 
           "What It Is"
           (hc/html
            [:img {:src "/images/what-it-is-header.jpg?width=500"
                   :style "width: 100%; height: auto"}]
            [:div {:style "height: 10px"}]
            the-what)
           :up-link (view-common/up-link "/"))
          :else
          nil))
  )
