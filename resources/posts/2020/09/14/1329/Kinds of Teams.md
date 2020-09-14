My team at Square is a different sort of team than I've worked on in the past, in ways I'm only just now starting to feel in my bones. Not different in the sense of the kind of discipline we bring to the work (although that is different, I suppose), but different in the way we relate to other teams.
So here are some bottom-up lay engineer observations on different kinds of team structures. These often correspond to an org unit, but not always: particular projects may tend towards one style or another.

### Feature Teams

At Big Nerd Ranch, I mostly worked on what I suppose would be called feature teams. We were a consultancy, though, so we didn't often think of them that way. Each team was standing up a whole app on its own.
Nevertheless, that's effectively what it was: there were programmers, designers, and someone in roughly a product manager style role. Being a consultancy does throw a bit of a monkey wrench into that: the "product manager" role ends up being a bit of an accountability dance between folks on our side and on their side, and "success" is dependent on the quality of that relationship. Nevertheless, it has the key properties of what I think of as a feature team:

* Coming from the top down, here is the kind of thing you should build and your criteria for success
* Coming from the world, you have the actual success of the thing you're building in the market
* Underneath you, here are the tools/teams/collaborators whose tools you will use to achieve the above

In different contexts and companies, those elements will be different. For consulting, the top down is primarily the business relationship with the client, plus a dash of your own reporting pipeline. In a smaller company, it might simply be conversations with leadership. For larger companies, it's a direction provided by the hierarchy.
(Here's a dirty little secret, by the way: the direction is always provided by the hierarchy. In an interview at a BigCo in the modern SV vein, it is likely that you'll hear something like this: "Oh, we set our own direction." Usually this means that leadership has delegated the task of project ideation. That's nice, but leadership is always leadership. If you're actually setting your own direction without interaction with leadership, it's probably a good idea to start looking for a better job.)
On the flip side, the tools you use will come from one of three places: open source, a platform (e.g. all the stuff built by the Android team), or an internal team. The mix of those will vary depending on the kind of company, too. Suffice to say that at a BigCo, those relationships can be all internal.
Given all that verbiage, the important thing to know is that a feature team will (hopefully!) have a clear mandate for an area in which to build things, and it will build things in that area to achieve concrete goals for the company at large. Cross talk with the goals of other teams may happen, but more often than not it shouldn't be necessary to initiate a collaboration effort to get things done.

### Infra Teams

At larger companies, you also have a beast called an "infra team". Intoning this word will usually call to mind the domains patrolled by production engineers at larger companies, but the same style of work is done by developer experience teams, "product infra" teams (like the media team I worked on at Instagram), or any kind of team that builds tools used by others.
This kind of work is much different. Although it looks similar at first glance:

* Coming from the top down, here is the kind of thing you should build and your criteria for success
* Coming from the world, you have the teams who build on top of the thing you're building or maintaining/enhancing
* Underneath you, here are the tools/teams/collaborators whose tools you will use to achieve the above

The difference almost all lies in #2: the relationships you have with technical folks who use your work. Depending on the company, you may find yourself working on other people's code to migrate them onto your stuff, communicating firm API guidance to people, or holding fun requirements gathering meetings with developers.
As a programmer, the most important difference is simply that your technical decisions are not as much your own. A feature team should of course justify its decisions in code review and make them clear enough to pick up on, but that is a far cry from what an infra team needs. An infra team needs others to actually use the things it builds. That means that, at a minimum, others should be able to understand those decisions and work with them, and hopefully also agree with them and support them. It is a higher bar than the maintenance bar that feature teams need to meet.
Of course, this work still happens at smaller companies. It just doesn't rise to the level of showing up on the org chart. I love the open source Contour Layout that Cash App built internally, but we don't have a team dedicated to this kind of thing at this moment. Nevertheless, these are the kinds of relationships that come into play. The hallmark of infra work is supporting others, which means more collaboration, more communication, and more thought required for technical judgements.

### Strike Teams

Okay, now to get what inspired all this thought.
My current team is a bit of a different animal (for the moment, at least). We're acting as what I would call a _strike team_. It looks exactly like a feature team, except for one important wrinkle:

* Coming from the top down, here is the kind of thing you should build and your criteria for success
* Coming from the world, you have the actual success of the thing you're building in the market, _plus the other teams whose domains you are working in_
* Underneath you, here are the tools/teams/collaborators whose tools you will use to achieve the above

See, a strike team is given the exact same sort of mandates that a feature team is given, except their mandate draws them into work that other teams are already concerned with.
What a nightmare! Why on earth would anyone give a team this kind of mandate?
In our case, without getting too much into the specifics of things, it's so that we can focus our efforts on breaking into new markets. A team tasked with, say, helping the app take off in a new country, may be considering a similar set of initiatives as a team that's supporting the whole userbase. The prioritization, though, will be dramatically different. Which is a good thing, as far as the business is concerned.
The thing about the strike team is that, in the long run, the fruits of their labor will eventually be handed off to other teams. So the job is to get in there, achieve the goal (ideally with the same kind of efficiency that a regular old feature team has), and get out without leaving a big mess. So the collaboration wrinkle comes into play again here.
I'm still new to this and figuring out what I think about it, which means that I'm still making mistakes. But it seems to me that a strike team must lead into the effort by establishing a clear playing field. After all, the strike team's job is to move with some speed; they can't go into the effort and hold a brainstorming session or something silly like that. But if they Leroy Jenkins their way into the project, a mess will be had by all. So it seems wise to prepare the way: let the other team know who you are, what you're doing, and why you need to build something in their territory.
At the end of the project, there's a related problem: where do you stop? I like to think of myself as a good engineer, and so I like to put on what I call the God Hat: the 50,000 foot perspective that tries to move the code towards the Good Place, where all is in order and in its right place. In practice, though, putting the finish line at The Good Place will mean that your strike team will assign itself work that has nothing to do with its mandate. This may cause leadership to tilt its head to one side quizzically like baffled pug dogs.
Collaboration is required here: ideally the collaborator team will be able to pitch in and pick up part of the work, but if not the project plan will need to provide an endpoint that sets the collaborator team up to finish up the work. Thankfully, this kind of handoff is more straightforward than the idea transfer infra teams need to accomplish.
Anyway, that's what I think about teams today.
