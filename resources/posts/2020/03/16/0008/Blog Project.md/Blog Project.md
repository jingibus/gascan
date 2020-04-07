So I've started with the utter basics of gas can. I thought first that I'd get a quick model of what I wanted to build in terms of data structures.
Heretofore I've just modeled in terms of objects. Having hacked through a bit of Clojure code now, though, I thought it'd make more sense to model in terms of the kinds of data I'll have and the kinds of transformations that will be made to it:
![][post-creation-workflow]

Circles are stateless processes, and rectangles are process-free data.
(Note that I have now, by including this diagram, forced myself to support images. Egads.)
There are two main kinds of data here: remote posts, and interned posts. A remote post has all the metadata we require, but it only has external referents to somewhere in the filesystem that lives outside gascan. So import will process that into an interned post, which has all of its referred-to data living somewhere within gascan itself.
Finally, the loop in the lower right is a simple data store: we slurp posts out from an EDL list we save somewhere, and have a process to append a new post to it and spit it right back out. The only unknown in this section of the code to me right now is the mechanics of reading and writing to my own project storage; the serde will be trivial, since EDL makes that easy.
I've started today by working on the user input portion. This needs to take in a file location for the new post we want to slurp in. Once we do that, we should be able to process the MultiMarkdown into something with a title, and then present that plus a timestamp to the user and create a remote post with it.
###Command line arguments
I initially thought to tackle this by using the argument parsing from core Clojure. These tools are basic, though --- if you want to emit help or anything, you're on your own. Thankfully, it referred another library called ``cli-matic`` that I've picked up that takes care of almost all of that in a declarative way:

        ```(def CONFIGURATION
          {:app         {:command        "gascan"
                         :description    "A small blog content tool."}
           :commands    [{:command       "new"
                          :description   "Add a new post."
                          :opts          [{:option "file" :as "MultiMarkdown File" 
                                           :type :slurp
                                           :default :present}]
                          :runs          new-post}]})```

Given that configuration, and ``cli-matic`` will invoke ``new-post`` with the ``file`` as an already slurped-in file contents. So I get some basic file validation, help documentation, and a little plumbing all in one tool. Nifty.
###Markdown export
I didn't think this would be an issue! But I had to spend some time in the woodshed figuring out how to make Markdown export from Scrivener work the way I wanted it to.

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px
