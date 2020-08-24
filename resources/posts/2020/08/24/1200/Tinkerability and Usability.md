Fragments are a moderately hot topic this week in Android, because the fragments API has started to see some additional love from Google. Thus the embers of this long settled debate have received a little oxygen.
The work of software development always rests on the cutting edge, and thus I'm not particularly interested in relitigating the whole fragments question. Cash App (my current employer) doesn't use them, and I'm damned happy with the ecosystem there. But I am interested in how these past decisions can shed light on how to make future design decisions, particularly with regard to the idea of the Single Responsibility Principle, one of the SOLID object oriented design principles.

### Choosing Expediency over SRP

Nobody does anything without some reason for it, good or bad. When I was at Big Nerd Ranch, we chose fragments becase they solved problems we didn't have a ready answer for: how do you navigate between screens? How do you compose different screens together on a tablet?
Android's Activity was not a viable solution: it was deeply tied to the operating system, such that you could not put two side by side. Neither was building our own framework; we were teaching others how to build Android apps, after all, not how to bikeshed their own tools or use frameworks we had constructed.
Doing this gave us a strong dependency on a component with many responsibilities. Fragments and the fragment manager were connected to:

* Navigation
* Lifecycle management
* View compositing (your fragment has a "location" in the layout)
* Transition animations

And that's assuming that you don't put any of your own logic in the fragment.
As I said, I got a bit of religion on SRP towards the end of my time there. And judged by the rubric of the Single Responsibility Principle, the fragment idea was not great.

### Choosing SRP Over All Else

At Instagram, I took a few years away from higher level architectural concerns and focused more on common infrastructure. (On a big app, client side infrastructure can keep whole teams of people busy as other folks write code that is actually useful.) In doing that, I attempted to build out a larger system to solve a big problem at the interface of a legacy system.
In building that out, I took the Single Responsibility Principle as my guide. Each conceptual moving piece would have a system that owned it and did only that job to the best of its ability. I assembled them like Lego, driven by tests, led by SRP into an Elysium of pure and good design.
This was a grave error. We ended up with an interesting and powerful new system with some new capabilities, but fitting the system into its integration sites and debugging them was a nightmare.
That experience really soured me on the notion of fixed design rules. The clarity of thinking, while comfortable, did not lead to clarity in discourse with the surrounding system.
So how do I guide my design without a fixed design rule? Some principle is required, even if it is not so firm as to be blindly applied. The two ideas that I've gravitated toward are what I call _tinkerability_ and _utility_.

### Utility

Utility is all about the direction from which you approach the problem: approach in such a way that you create something useful that does something clearly material in the system. This sounds obvious, but the opposite approach is seductive because it will present any number of appealing solutions. That's why I chose to apply a principle like SRP from the inside out in a legacy system: by making new and clean components, I would magically get the flexibility and sophistication to solve whole new classes of problems better.
But that violated the idea of utility: blindly applying that rule produced new artifacts that were testable, sure, and they did the same job as the legacy components they replaced. But the primary motivator was the principle, not the job, and they were harder to verify and to integrate because the core job was secondary to the other bright ideas I tried to execute on.
In contrast, the decision to use fragments did satisfy my criteria of utility. They answered our major needs: first party, comprehensive enough to serve our rapid development needs and those of our readers, and not a bear to teach to others. We tailored our implementations to fit what we were doing with them. Had we started from architectural perfection, we would likely have failed to meet our other goals.

### Tinkerability

Tinkerability is my word for a property of software systems that has been much belabored: that they can be adapted to changing circumstances. But mostly by tinkerability I mean the ability to fiddle with it easily in isolation. This is closely related to the idea of unit testing, but it's a bit broader than that: a component that interfaces with other systems outside your control may not be fit to write unit tests against, but if you can fire it up independently and put it through its paces without running the whole rest of your program, then it's tinkerable by my definition.
I often think about dynamicism when I think about tinkerability. I love nothing more than to be able to modify the system at runtime without compiling and redeploying the whole thing. Debug menus are often used to do that; I've become a fan of using the Kotlin expression evaluator to do the same thing. But either approach forces one towards the same design territory: design components so that they can be used and modified independently in order to see how they work.
At Instagram, some aspects of what I built were tinkerable, and others were not. Because it had been SRP'd to death, every internal piece was unit tested and tinkerable by this description. But because it had not been constructed with utility in mind, it was not actually tinkerable in real integrations.
And of course the fragment is hardly tinkerable at all: the integration surface is so large, that it can't realistically be put through its paces to cover many real-world scenarios. But that's a bit of a trap, too: if you don't put some thought into all the different things you need to exercise to validate your fragment's operation, it will seem like a pretty clean unit.
Will these ideas serve me perfectly going into the future? If past is prologue, probably not. And I often fall back into the trap of trying to figure out the perfect thing and build up from that, rather than the other way around. But this time I'll try to go into the future without believing in the ideas above all else.
