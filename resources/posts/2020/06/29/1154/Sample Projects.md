You know, there are some problems that people humblebrag about, even if they're a failure mode.
At my last job, we had something like this with large stacks of diffs. A "stacked diff" was like a chain of pull requests: each of them was big enough to merit independent review, but they were dependent upon each other. A developer complaining about the management issues specific to a stack of 12 or 15 diffs might be airing a legitimate complaint about the problems they needed to solve, but listeners couldn't help but pick up a second message: "This person is working on something enormous."
It's a perverse incentive. A giant stack is not a good thing. All other things being equal, giant stacks are worse than single PRs, and a codebase that requires giant stacks to get anything done is worse than one that doesn't.
So it is with a large codebase. From the point of view of maintainability, extensibility, speed, and who knows what other measures, a small codebase is preferable to a large one. If a large codebase could magically be turned into a small one, this would almost without exception be the right move. But real work on real big problems requires large codebases, jobs working on large codebases are glamorous and pay well, and so large codebase problems will always have a sort of humble brag property to them.
So if you're reading this, please keep that in mind as context. This problem may not be relevant to you, and that's fine. If you can successfully keep it from being relevant, you'll be happier and more productive.
Having said that, let's talk about build times, focusing on Android (my domain of choice),  with over, say, 200kloc.

### Build Times: The Head On Front

Large codebases have problems with build times. I'm not an expert in this problem, but I've been party to what I'll call the "head on" approach.
The head on approach is simply to reduce the total incremental build time for the application to the point where it can be used for iterative development on the same scale as a small application.
You can do this with two kinds of tactics. The first is to attack it with the build tools themselves. Migrating from gradle to buck or bazel is a choice many companies small and large have made, but the tooling journey can go even further than that. Buck itself may be customized, or tooling may be built to coordinate intermediate build products. This can even lead to process changes for the developer. You might build on a server instead of your own machine. What about source control, branch changes? Deep this rabbit hole goes, yes.
This rabbit hole has a big downside: you pay all the penalties of being a special snowflake, up to and including not being able to shoot the breeze with folks about build tooling at I/O.
A second category of tactics is in the codebase itself. Modularization is usually necessary: a well-modularized codebase will build faster than a monolithic codebase, because the build can be parallelized, and incremental builds require less work.
The tools used in the code affect this as well. An ill-behaved annotation processor could tank your build. The compiler itself can be a cost: I would be a fool to try and enumerate Kotlin's many merits over Java, but compile time is not one of them.
Regardless of what tactics you apply, attacking build times on the head on front places the bar for success high. Getting the build down from eight minutes to five minutes may be an impressive feat, but to the engineer iterating to perfect a screen it still counts as a failure.

### Sample Apps: The Vulnerable Flank

At my latest gig, I've been using sample apps to iterate quickly. This is a new approach to me, and one I've come to enjoy as a discipline. A lot of the following is received wisdom from colleagues at Square (including John Rodriguez and Aleksei Zakharov), but I also have some battle scars from my time at Instagram.
Here's the idea of the sample app: instead of iterating in the main app, create a separate app off to the side that can be deployed. Use it to exercise components like views in isolation from the rest of the app, and use unit tests and the like for more granular validations.
Now, for the sample app approach to work at all, the app has to be modularized already. If a `View` cannot display a `Banana` without pulling in the gorilla and the entire jungle (as the aphorism goes), then there will be no wins here. Don't even try. If you can, though, the sample app is a pleasant discipline.
It is a discipline, though. And if your sample app can't abide by the discipline, it will fail and become a drag on the codebase.

### Limit Dependencies

Keep the dependency surface of the sample app small, small, small. As a rule of thumb, do not include any internal dependencies you do not own. A sample app with a raft of dependencies will quickly turn into cruft and start to rot.
This isn't me telling you what to do, by the way. This is me telling you what I did and what happened when I did it.
Every single sample app is an additional build target. That's a penalty: it's one more thing to break that has to be tested independently. If that sample app is limited to dependencies you are responsible for, then you've made it much less likely that work on an unrelated part of the app will break your new special snowflake.
There's no getting around that additional build target. Every additional dependency will expand the surface of what can break. If your own dependencies conform to a common API that might be subject to change, even those might be risky.

### Keep It Simple

Every sample app should start its life with the minimum viable bits of code to be deployable: a manifest, an activity, and that's it. Only add to that what you need to survive.
Complex things break. The more complex the sample app is, the more quickly it will turn into unmaintained cruft, and eventually become a target for deletion.
There will be good reasons to make the sample app complicated. The temptation to introduce some frameworkiness, for example, will be strong, since you'll be doing similar things in each sample app. This should be resisted as much as is possible, because it will tie all of these ostensibly separate sample apps together. Woe betide the engineer validating their refactoring against all those build target!
This is also a pressure to avoid frameworkiness in your own components: if the framework changes, then all of a sudden all the sample apps need to change, too.
Validation will also be a temptation. The more "real" the sample app is, the more useful it seems to be. At some point, the bullet will have to be bit, and the validation will need to be run in the real application.

### Sample Apps: Our Dearest, Non-Codependent Friend

With that I think I want to wrap this up. But first, some caveats.
While I'm having a positive experience right now, instinctively I know that this cannot be the primary approach for addressing poor build times. I'm currently doing feature work, which is always at the edge of the system, and thus is ideal for this kind of thing. This gets harder and harder closer to the core. Sample apps may offer no further advantage there above and beyond unit tests. I may be wrong, though.
As such, sample apps are always going to be a flanking attack on the build time problem. By themselves, they will fall short, but they can ease the standard for success on the main front.
Keep it simple, limit dependencies. Don't build frameworks, and don't build cruft.
