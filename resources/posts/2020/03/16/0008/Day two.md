I'm a bit slow to get started today, although I've gotten some stuff done. I've written out code to pull the title from the Markdown format, which is nice.

It strikes me that the interned-post is different in a couple of ways from the extern post. One would necessarily be that the filepaths would have to be relative to the project root, whatever that is. It will be rehomed somehow, eventually.

###Backtracking on data rep
The way I initially implemented my parsing assumed that I would point the parser at the document without regard for how it was represented on disk, and that the parser would figure it out.
This doesn't make sense now that I've implemented it: I want my data type to have meaningful fields that refer directly to the items of interest. So I'm switching to working on the record parser.

###Making progress on interning
This went way better than I thought it would. I went down a brief rabbit hole future tripping on how little I'd be able to learn from Java about various project filepaths, but it turns out that ``(System/getProperty "user.dir")`` will work just fine.
Once that went my way, everything else was just hunky-dory. I created an ``intern.clj`` file, and defined the various operations that I want interning to consist of: for the most part, this is just saving files in ``gascan/resources``, with an optional degree of filepath preservation, and yielding an appropriate relpath.
This is pretty much done, so now I can go work on actually writing the import code. That will require doing a little bit of Markdown editing (I need to strip out the title information), and then writing the contents out using my intern tools.

###Tooling
I got a little sidetracked on debugging. This part of the job is still unfamiliar to me, but I at least have a few new tools to check out:
One is a set of little tools I scrabbed from the internet. Essentially, it's an ad-hoc tool to flip debug printfs on and off:

        ```(def ^:dynamic *verbose* false)

        (defmacro printlnv
          [& args]
          `(when *verbose*
             (printf ~@args)))

        (defmacro with-verbose
          [& body]
          `(binding [*verbose* true] ~@body))

These feel a little scrummy, since they're copypasta, but they don't have much drawback that I can see. In prod, they do nothing, but you can invoke them in an REPL like so and get debug printfs: ``(with-verbose (my-function some-input))``.
``clojure.tools.trace`` also popped up on my radar. This seems like more of a power tool, but it's similar in the sense that you use it at an REPL and get some insight into how your code runs.
One of the disciplines I'm learning here is simply to keep the pieces of my program small and operating on digestible pieces of data. I don't have test cases, but I do have the discipline of always keeping things going at an REPL (Which... means I have test cases.). If I need some more tooling at a certain point to keep that going, then that's what I'm going to do.
