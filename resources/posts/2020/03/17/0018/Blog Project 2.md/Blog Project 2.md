So I've started with the utter basics of gas can. I thought first that I'd get a quick model of what I wanted to build in terms of data structures.
Heretofore I've just modeled in terms of objects. Having hacked through a bit of Clojure code now, though, I thought it'd make more sense to model in terms of the kinds of data I'll have and the kinds of transformations that will be made to it:
![][post-creation-workflow]

Circles are stateless processes, and rectangles are process-free data.
(Note that I have now, by including this diagram, forced myself to support images. Egads.)
There are two main kinds of data here: remote posts, and interned posts. A remote post has all the metadata we require, but it only has external referents to somewhere in the filesystem that lives outside gascan. So import will process that into an interned post, which has all of its referred-to data living somewhere within gascan itself.
Finally, the loop in the lower right is a simple data store: we slurp posts out from an EDL list we save somewhere, and have a process to append a new post to it and spit it right back out. The only unknown in this section of the code to me right now is the mechanics of reading and writing to my own project storage; the serde will be trivial, since EDL makes that easy.
I've started today by working on the user input portion. This needs to take in a file location for the new post we want to slurp in. Once we do that, we should be able to process the MultiMarkdown into something with a title, and then present that plus a timestamp to the user and create a remote post with it.
### Command line arguments
I initially thought to tackle this by using the argument parsing from core Clojure. These tools are basic, though --- if you want to emit help or anything, you're on your own. Thankfully, it referred another library called `cli-matic` that I've picked up that takes care of almost all of that in a declarative way:

        (def CONFIGURATION
          {:app         {:command        "gascan"
                         :description    "A small blog content tool."}
           :commands    [{:command       "new"
                          :description   "Add a new post."
                          :opts          [{:option "file" :as "MultiMarkdown File" 
                                           :type :slurp
                                           :default :present}]
                          :runs          new-post}]})

Given that configuration, and `cli-matic` will invoke `new-post` with the `file` as an already slurped-in file contents. So I get some basic file validation, help documentation, and a little plumbing all in one tool. Nifty.
### Markdown export
I didn't think this would be an issue! But I had to spend some time in the woodshed figuring out how to make Markdown export from Scrivener work the way I wanted it to. After some confusion and realizing that my version of Scrivener was way out of date, I was able to tweak my export settings and styles and I was good to go.
One odd thing: I assumed in building my command line arguments that I could just pull a raw file using the magic in `cli-matic` and I'd be good to go. But it turns out that, depending on whether images are included, my MultiMarkdown export could be either a directory or a flat file.
Bad assumption: my arg parsing was already doing so much. It was not wise to write it tied to a specific file format. Oh well, lesson learned.

### Markdown parsing
This got a bit more annoying.
My needs for markdown parsing are a little more detailed than "parse; emit HTML." I need support for simple MultiMarkdown extensions, and I also need some ability to walk the AST so that I can pull out the title automagically. So at a glance, the most popular `markdown-clj` library doesn't cut it, since it's just converting to HTML.
`commonmark-java` looks a bit better, and that leads me to `flexmark-java`, which has better MultiMarkdown support. But I've started to get a bit lost in the weeds trying to figure out how to work with `flexmark-java`'s AST representation. Part of that is just getting lost in how to reflect on the API, or navigate a project that is about 3/4 baked. I'm leaning a lot on code samples, but... not entirely happy with this dependency.

### Clojure learning
Finally, I just went down a rabbit hole playing around with clojure.
Clojure is a very functional list. Once upon a time, I worked through most of SICP, which meant I was fluent in that style. Once upon a time has long passed, though, and I'm not anymore!
So that meant some embarrassing head bonking against the tools. I'm ashamed to admit it took me a couple of hours to figure out how to get a list of all the methods available on a Java class:

        (defn class-hierarchy
          ([class-instance]
           (class-hierarchy class-instance #{}))
          ([class-instance traversed]
           (if (traversed class-instance)
             #{}
             (let [traversed (conj traversed class-instance)
                   subsequent (apply clojure.set/union 
                                     (map #(class-hierarchy % traversed)
                                          (bases class-instance)))]
               (conj subsequent class-instance)))))

        (defn all-methods
          [instance]
          (let [all-classes (class-hierarchy (class instance))
                total-reflection (apply merge-with 
                                        clojure.set/union 
                                        (map reflect all-classes))] 
            (->> total-reflection 
                 :members
                 (filter :return-type)
                 (map :name)
                 sort
                 distinct)))

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px
