So: I had a little break from work. I used it to take a break from my professional weapon of choice for many years: Android.
Why did I do it? Honestly, I didn't know at the time. The heart of a decision is rarely a reason; usually it's a feeling. I had some negative feelings. I don't have a good reason to share them, so I'll keep them to myself.
My vacation, though, brought up a lot of fun new things. So at the risk of writing one of those weird gushy posts about a hopelessly niche thing, I'll go ahead and share them.

### Small server; team of one

In my vacation I built something in Clojure: a small web server that dynamically serves content from a hacky little data back end I wrote. It was a great experience that changed how I viewed the way I'd been writing code for all these years.
So what made it so different?

### Programming for fun

The idea of programming in my spare time simply to sharpen the professional saw never spoke to me, and it still doesn't. Free time is free time, and fun is fun: don't mix the two. Writing code to further my professional career is just more work.
That's not how I started programming, though. I wrote my first code to have fun: I was bored in math class. Why listen to the teacher explain how last night's assignments were done when I can build something on my TI-83 instead?
I was only trying to kill time. But I wasn't programming to make someone else happy, or to be useful. Using something other than Android helped set my efforts at a considerable distance from those motivations.

### Programming as a creative impulse

Even then, sitting down as a lark to write some code wouldn't have lasted long. What's the point?
It did last, though, because the root was a creative impulse. And it wasn't a creative impulse in the sense of a project I wanted to accomplish, a standard of performance I wanted to meet. It was escape: escape from the television, from YouTube, from video games, Facebook, Twitter, Slack and all the attention loops that lay scattered about my life like antipersonnel mines.
Once I cut those out, what was I left with? I had a huge empty space in my life. And out of a desire to fill that space, up came the creative impulse.
I loved it. I felt invigorated. I don't know that Clojure has anything to do with that, but when you're in that mood you just have to roll with whatever sates your desire.

### Immediacy!

Programming in Clojure was immediate! Everything that I wanted to do could be accomplished right there, instantly.
People use the acronym "REPL" (short for Read-Eval-Print-Loop) to refer to these immediate environments. I had fired up command line REPL interpreters for Python, Ruby, and so on, but I didn't get it.
But I worked through [Clojure For the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/), I started using Emacs, and I followed instructions from other people about what to do. When I got up to speed, I found that from within my editor I could use the language and runtime to write code and investigate its behavior within the running system.
I could tinker!  Soon my runtime became a running web server with quite a bit of overhead and startup time. I found that I could jack straight in to the server and patch it live with new and altered code, never needing to wait to recompile and redeploy.
And as maligned as the parens are in Lisps like Clojure, their unwavering structure gave the same immediacy to evaluating and observing each piece of my program. Bind the right names to the right values, and any piece of any function can be run on its own.

### A different model of mutability

Clojure is not a side-effect free world. Learning Clojure effectively requires one to understand a mutable runtime. As a Clojure application is brought into a running state, every single function definition mutates a namespace's bindings to a new configuration.
Once the app is up and running, the steady state is indeed by and large immutable. All the best tools in the toolbox work with immutable values, and additional effort has to be expended to make a piece of the program work in a stateful manner. Mutability is reserved for those parts of the system that demand it (like the network, services that must be started and stopped, or library dependences that are stateful).
The reliance on mutability to wire up the system rather than get work done makes it possible to tinker with the system as it's running. This is a drawback to lexical bindings: if you lexically bind a large piece of machinery and hand it off, it's no longer possible to tinker with it in this way. You have to build a whole new large piece of machinery and hand that off. In my case, I ended up needing to reboot the web server to change the routing table.
At runtime, tinkering is terrible. The program must never be allowed to tinker with itself. But when you've built something non-trivial --- oh, tinkering is heaven. It is a powerful way to get shit done.

### Primacy of the data itself

When I first started working on my project, I knew that I would be in for a values-first system. I diagrammed out my idea of where data would flow through the system, and gave a name to the kinds of values that I would have. "Hmm," I thought, "how can I use Clojure to give a name to this class of value?"
That's what I'd do in Java, after all: I'd define a class called `RemotePost` with the fields I want, and use that as my immutable value object.
Clojure has a tool called the Record that seems to match this concept. After some time spent with it, though, I realized that I was fighting the APIs.
See, I wanted to attach a name to this idea of a `RemotePost`. By and large, though, Clojure-y abstractions don't care what the name is. They only care about the shape of the data: if it's a map with keys for `:title,` `:timestamp`, and so on, then it will serve as a `RemotePost`.
I found this deeply disconcerting. I did some work early in my career with Python where I didn't rely on strict typing; the resulting mess was bad enough that I have cast wards against for the rest of my life. But this is indeed the best practice in Clojure.

### Structure-oriented data discipline

There is a spirit of discipline about it all, though, if an unfamiliar one. The `spec` framework provides a common toolset for establishing the shape of data objects, and a lot can be learned about Clojure design sensibilities from understanding it.
`spec` has a way of working with keys that surprised me. In `spec`, if you want to ensure that `:timestamp-ms` is bound to a valid millisecond timestamp, you bind the data spec to the key name. Here is a construct that uses the `s/and` function to create a data spec and bind it to the `::timestamp-ms` key:

        (s/def ::timestamp-ms 
          (s/and int? 
                 can-be-converted-to-instant? 
                 is-semi-plausibly-within-bills-lifespan?))

The names ending in `?` all refer to predicates: functions that take in a value and yield either true or false. So this spec says that a valid `::timestamp-ms` is described by the given three predicate functions, all of which will return true for a valid timestamp.
Within this namespace, the name `::timestamp-ms always` refers to the same kind of data wherever it is seen. How interesting is that? It's not what I expected, but now that I've seen it I think it would be confusing to treat names differently in my own codebase.
So if I want to describe a data structure that has a timestamp in it, I can define it by using `s/keys` to define a data spec on maps. Then I use `def` to assign that data spec to a regular old variable:

        (def timestamped-map
          "A map with a timestamp in it."
          (s/keys :req-un [::timestamp-ms])

So if I mapped a value to the `:timestamp-ms` key (`:req-un` allows me to use the non-namespace-qualified `:timestamp-ms` instead):

        {:timestamp-ms (System/currentTimeMillis)}

I could use `timestamped-map` to validate it:

        > (s/valid? timestamped-map 
                    {:timestamp-ms (System/currentTimeMillis)})
        true
        > (s/valid? timestamped-map {:timestamp-ms 15})
        false 

This is a simplified view of spec, which is capable of more than this. But in some ways, this is still less than what a more typical type system does. There's no strictness provided: there's no way to know exactly what sort of object came into your system, nor is there a way to tell from an instance where it was defined, as you can in Java.
In return, though, with even this small scenario you get a validatable guardrail that maintains flexibility and dynamicism in the system, and retains the ability to create value objects with the standard REPL tools that can be consumed anywhere, without needing to refer to a particular classpath and a particular implementation. As a result, most of the things that drive the app live in an interoperable _lingua franca_.
This is a serious trade-off. As a hobbyist I love the flexibility, but as a professional the idea of being unable to find where my data was constructed in an unfamiliar codebase is a little terrifying. There's a mechanism to namespace keys that would fix this, but it's not in common usage.

### Distinction between data and functions

From a design perspective, I appreciated that data and functions lived separate lives. There were certain entities that did live an object-y life of keeping track of a changing bit of state, but this was the exception rather than the rule.
This was at its best in a design context. The lack of expressivity in objects had started to wear thin on me:
[![][ScreenShot2020-05-03at92816AM]](https://twitter.com/billjings/status/1254364892653424641)
The inability to express different roles in any way other than to put different words on things had gotten old. Drawing a distinction between functions and data and denying capabilities to one or the other makes a lot of sense! This restriction forces a lot of things to be teased out that would otherwise stay put.

### The vacation is over

I recently started working at Cash, so my vacation is at an end. I'm once again working on a big client, with big build times. Sigh.
There are some nice things: we have a firmer line between processes and data than any other codebase I've worked on. But is it possible to tinker with it?
I've got lots of real work to do now. But maybe...

[ScreenShot2020-05-03at92816AM]: ScreenShot2020-05-03at92816AM.png width=377px height=152px
