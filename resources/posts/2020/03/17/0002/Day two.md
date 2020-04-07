I'm a bit slow to get started today, although I've gotten some stuff done. I've written out code to pull the title from the Markdown format, which is nice.

It strikes me that the interned-post is different in a couple of ways from the extern post. One would necessarily be that the filepaths would have to be relative to the project root, whatever that is. It will be rehomed somehow, eventually.

### Backtracking on data rep
The way I initially implemented my parsing assumed that I would point the parser at the document without regard for how it was represented on disk, and that the parser would figure it out.
This doesn't make sense now that I've implemented it: I want my data type to have meaningful fields that refer directly to the items of interest. So I'm switching to working on the record parser. For remote posts, it will need to know where the files of interest are, and how deep they are (folder structure may need to be replicated).

### Making progress on interning
This went way better than I thought it would. I went down a brief rabbit hole future tripping on how little I'd be able to learn from Java about various project filepaths, but it turns out that `(System/getProperty "user.dir")` will work just fine.
Once that went my way, everything else was just hunky-dory. I created an `intern.clj` file, and defined the various operations that I want interning to consist of: for the most part, this is just saving files in `gascan/resources`, with an optional degree of filepath preservation, and yielding an appropriate relpath.
This is pretty much done, so now I can go work on actually writing the import code. That will require doing a little bit of Markdown editing (I need to strip out the title information), and then writing the contents out using my intern tools.

### Tooling
I got a little sidetracked on debugging. This part of the job is still unfamiliar to me, but I at least have a few new tools to check out:
One is a set of little tools I scrabbed from the internet. Essentially, it's an ad-hoc tool to flip debug printfs on and off:

        (def ^:dynamic *verbose* false)

        (defmacro printlnv
          [& args]
          `(when *verbose*
             (printf ~@args)))

        (defmacro with-verbose
          [& body]
          `(binding [*verbose* true] ~@body))

These feel a little scrummy, since they're copypasta, but they don't have much drawback that I can see. In prod, they do nothing, but you can invoke them in an REPL like so and get debug printfs: `(with-verbose (my-function some-input))`.
`clojure.tools.trace` also popped up on my radar. This seems like more of a power tool, but it's similar in the sense that you use it at an REPL and get some insight into how your code runs.
One of the disciplines I'm learning here is simply to keep the pieces of my program small and operating on digestible pieces of data. I don't have test cases, but I do have the discipline of always keeping things going at an REPL (Which... means I have test cases.). If I need some more tooling at a certain point to keep that going, then that's what I'm going to do.

### Import almost done
It's late, so I'm not going to finish it, but apart from a bug around some use of concat (tricky: if you place a string in a list position, it'll be treated as a list of chars), this looks just about ready to go. This means I should be able to have the model up and running tomorrow!

I want to get some early thoughts down on what working with Clojure has been like. I'm barely 300 into this project, and already the feels are different. It's fun and loose in a way that Android just hasn't been. Why?
### I'm a noob.
One reason is simply that I am a nooby noob nooby noob noob: I don't know anything about anything. Those 300 lines were hard fought, and include a bunch of code I just wrote to figure out up from down.
So some of the fun is simply in getting to do what I want to do. My brain isn't filled with all the competing strategies for laying out my project, for naming my functions, for making a beautiful codebase. I've got a blank canvas, and the code is beautiful if I say that it's beautiful.
### I'm working by myself, outside of my career path.
I mainly work in Android development. In that world, any sort of learning and play I would do around app development would have to have at least an ear to the ground as to how useful it is and where it would fit in to my career.
I could be more of a hobbyist, yes. But the work I do to get to that headspace is done for me in Clojure by sitting down and writing code.
If I'm going to continue doing Android work, I think that I have to find that spirit in Android somehow. But for the time being, it's difficult to pull up Android Studio and write Java/Kotlin without thinking about how that code should be written in a larger corporate environment. And that's not conducive to play.
### Clojure really is looser.
I could be wrong here. But my sense so far is that the native style of Clojure development and tools is oriented around smaller, tighter teams that maintain more global context on the project than is done elsewhere.
For example, documentation. Documentation is a matter of social norms, but also of tooling. The basic documentation I'm used to in Java is static typing: at a minimum, the type of the input parameter is provided, providing some written description of its usage.
In Clojure, documentation is oriented around usage examples, sometimes with comments. For example, here is an excerpt from the documentation on `->`:

        ;; Use of `->` (the "thread-first" macro) can help make code
        ;; more readable by removing nesting. It can be especially
        ;; useful when using host methods:

        ;; Arguably a bit cumbersome to read:
        user=> (first (.split (.replace (.toUpperCase "a b c d") "A" "X") " "))
        "X"

        ;; Perhaps easier to read:
        user=> (-> "a b c d" 
                   .toUpperCase 
                   (.replace "A" "X") 
                   (.split " ") 
                   first)
        "X"

A short paragraph is also provided, but the real enlightenment is in the usage examples. If this pattern is also followed elsewhere in the community, then it means that communication requires one to explain a way of thinking about the structure of the problem, not just a set of tools that do something.
I'm not sure that my point is well-argued. But the nut of it is this: if usage examples are expected and/or required as a form of documentation, then that is a sign that usage varies a lot. That means that the forms of code also vary a lot more, which means that collaborators are spinning up more implicit context with one another on how they choose to express their solution.

I've been working on other things for a few days (see: pandemic, Square take-home exercise). But now I'm interested in getting back on the horse
Last I worked on it, I pretty much had the back end where I think I want it to be. No doubt I can make a few improvements, but I think I can work on those as I go. The next step will be to generate a view system.
A lot of this comes down to view design, to be honest. But another key component will be templating: how I figure out what to display. Some of that will be a matter of routing, some of it will just be figuring out how to display html. And write html!
### Tooling
At a glance, it seems like hiccup is the way to go. So that's the way we'll go.
Poking at it further leaves me doubt as to how well the markdown will go. So we'll see about that. `markdown-to-hiccup` appears to be an option... I feel iffy about having two markdown parsers in the codebase. But what is life but a series of iffy decisions?
I suppose I could write my own converter from my existing markdown format to hiccup. That would at least mean that I don't have to figure out any wonky API to style the thing.
### Visual Design
I want the design to reflect my values. That means it should be simple; not because I have a clean aesthetic, but because I am lazy.
It should also be a little weird and opinionated. Because I am weird and opinionated.
It should also illustrate the kind of focus I'm going for with my writing: something that is kind of intentionally difficult. Or maybe old school.
I feel pretty certain about the posts themselves: they should have almost no promotional or branding material up top. At the bottom, I can have a signature and some nav and an "about". And that's really all I need.
So I'll start there.
### First steps
I'd like to start by creating a command line tool that just renders a bit of HTML. Then I'll use that to see if I can't get flexmark to play well with hiccup. And then we'll go from there.

### Short session
I'll have to get going, as this was a short session. But I did learn a few different things:

* Debugging context is a thing. I didn't get an REPL fired up when I first came back to my session, which is not great. I'd love to learn better how to manage an REPL that I can just wind up and go immediately.
* I found myself wanting to have a method "as-loaded" that would have an InternedPost with a loaded set of markdown (I had decided to leave this data out of the serialized representation, but I'll need it elsewhere.) As I sat down to write this in `multimarkdown.clj`, I realized this would be awkward: to do it, `multimarkdown` would have to understand how to load up resources. This doesn't make a lot of sense, and neither does the location of `InternedPost` for that matter. I think that it should be moved to another module; furthermore, `LoadedInternedPost` should be treated as a different kind of entity.
* Related to the above: am I dealing with different entities in a clojure-y way? I don't know if I am, but I do know that I've got enough battle scars from excessive data dynamism in Python to really like knowing what kinds of data I'm dealing with. So even if it's not stylish, I'd like to keep on creating distinct records for distinct kinds of data.

I only got about an hour's work done tonight. I tweaked the reporting a bit, and got a super simple render function up and running. Currently I am trying to load the markdown that's been written out, which isn't working b/c I'm trying to slurp up a resource path. That won't work, of course.
Honestly, it was a rough day today because I was home all day on this lockdown. I have a pit in my stomach, and I don't know how to deal with anything. But getting into this project will help things, I'm sure.

I started today staring at this stack trace:

JVM Exception: #error {
:cause clojure.lang.PersistentArrayMap cannot be cast to com.vladsch.flexmark.util.ast.Node
:via
[{:type java.lang.ClassCastException
:message clojure.lang.PersistentArrayMap cannot be cast to com.vladsch.flexmark.util.ast.Node
:at [gascan.core$render_post invokeStatic core.clj 40]}]
:trace
[[gascan.core$render_post invokeStatic core.clj 40]
[gascan.core$render_post invoke core.clj 36]
[gascan.core$render_post_command invokeStatic core.clj 47]
[gascan.core$render_post_command invoke core.clj 44]
...

Which came from this code:

        html-renderer (-> (HtmlRenderer/builder) 
                          (.build))

Debugging Clojure code can be odd. The style encourages you to build these long chains of values that feed into one another. Inspecting values at points in the chain is... challenging. And that's what I want to to do here: what's the value emitted by `(HtmlRenderer/builder)`? I need to have a better solution in general for this problem, so I'm going to dive into it a bit today.
### First problem: debugging pipelines
Having looked at this, I think this problem is easier than I thought it was at first glance. If I want to see a value in a `->` pipeline, all I need is a function that looks like this:

        (defn monitor->
              [value]
              (do
                  (printlnv value)
                  value))

Inserting this into my pipeline should work just fine. Add a little chrome, and I end up with `monitor->` and `monitor->>`.

