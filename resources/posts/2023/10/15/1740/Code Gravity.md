I’ve found myself spending an unexpected amount of time in the DI space lately.

This is... unfortunate, maybe? I don’t actually like having to deal with DI frameworks! For my entire career so far, I have dealt with two things: Dagger, and nothing. Dagger I know well enough to get what I need to do done (sort of); nothing I know extremely well, and find a lot more fun to use when I can get away with it.

And yet: here I am, knowing more about Dagger2 than I ever have in my life, and besides that coming to advocate pretty hard for DI frameworks in general.

How did I get here???

That’s a long story, but one huge part of it was seeing a large codebase that did not use a DI framework at all. An enormous business with at least a few hundred thousand lines of code across their Android repo, with no dependency injection framework.

The solution to the dependency problem in that codebase was mostly to use singletons. That is obviously bad and causes its own kinds of problems, but it actually does solve some real problems that company had: for example, startup time was extremely important for their business, and singletons made the shape of that problem clear and solveable in code.

But the singletons themselves were not the primary problem. Singletons themselves are kind of a... linear kind of badness.

Here's what I mean by that:

If you have a dependency that is accessed via a singleton, it causes problems that are roughly proportional to the number of singletons you have. For example, let's say that I want to unit test a thing, and it has 5 singleton dependencies. I can fix this by switching those to injected dependencies. The amount of work that it will take to discover and fix that is (hand waving here) something like 5 times the amount of work that it will take to fix one singleton problem. So singletons are bad, yes, but they each dig a hole about their own size.

No, what really wrecked my day at that job was a problem with a greater order of badness: code gravity.

By “code gravity,” I mean “any force in the codebase that pushes code together”. I see code gravity exert itself via two methods:

* _Culturally_ - the opinions, beliefs, tendencies, standard practices, and even management incentives of the team

* _Mechanically_ - the amount of additional work required to tease two things apart, or introduce a new thing in a separate area

We had singleton problems, yes, but we had _huge_ code gravity problems caused by our dependency patterns. Cultural contributions were significant (e.g. there was not a strong unit testing culture), but the lack of a DI framework was a major contributor.

Code gravity is worse than singletons because it's not a linear kind of badness:

* Your dependency problems have an n^2 shape, not an n shape. That is, if you are threading your dependencies down a constructor call stack, the size of the stack is proportional to the size of your solution you’re building on top of, not the size of the problem you are solving.

* Your code is subject to the gravity produced by your n^2 dependency problems.

That gravity problem is very, very, serious, more serious than it appears at first glance. This is because a growing codebase needs maintenance and gardening in proportion to its increasing size; that is, you need to refactor your code more the bigger it is. But the gravity from your dependency problem is also increasing with the size of your codebase!

This gravity effect ends up being systemic; “god object” is the pithy name for this disease, but it is more like an overgrown garden. Too many things are where you don’t want them, and you can’t prune them away or move them easily to get real work done. And it’s only getting worse over time.

*That's* why dependency injection frameworks are important. They solve a problem, yes. But more importantly, they universally dial down your code gravity problem everywhere you use them. They change how your code grows, and _that_ is a big deal.
