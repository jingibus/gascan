(ns gascan.what-it-is-view
  (:require [hiccup.core :as hc]
            [gascan.template :as tmpl]
            [gascan.view-common :as view-common]))

(defn view
  [sess query-params]
  (tmpl/enframe 
   "What It Is"
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
   :up-link (view-common/up-link "/")))
