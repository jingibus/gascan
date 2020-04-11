I've started working on building a blog. Aren't we all impressed?
I can tell you this with certainty: not everybody is! When I brought it up chit chatting with a potential hiring manager the other day, he said, "Oh, I built a blog ten years ago." So I'm clearly on the right side of this curve here.
Still, it's what I'm doing, and I think I have good reasons for doing it. I'm at the inception of the project, so I thought I'd go into my reasons for doing it, and what my current choices are for executing it.
Get ready, my friends, for my favorite pastime: belaboring a point. Let's sally forth!

I've been writing.
One of the first things I started doing last December was start a writing practice.
See, in the whole time I was at Instagram, the biggest thing that really bugged me was that, in spite of having some big ideas behind what I tried to do there, I never expressed them. I talked about the idea of writing documentation for other people with my manager at one point, but it never seemed to be a priority. And so it didn't get done.
This had a drawback as far as communicating my intent to others, but more importantly it led to some fuzzy thinking in my own design. I assumed that everything I was doing by the seat of my pants was going to work out.
It's so easy to say, "Oh, well I should have done X, or Y," but I just don't believe in the efficacy of standing athwart my failures, saluting God and Man, and saying, "Yes Sir! I will do better next time by doing This Thing I Should Have Done!" My experience has been that, if that new thing doesn't arise out of some daily practice of mine, nothing new is going to change.
And so I started a daily goal to write something or another in longhand. Thoughts periodically arise: "What should my outlet be? What is my goal for this writing?" but first and foremost the goal has simply been to write.

I want an outlet.
Not all the time. But I want to be able to share my work when it's appropriate.
I don't necessarily need an outlet that connects me to anyone at all, either. The modern internet is flush with all kinds of publishing platforms that promise to connect you with an audience in one way or another, and no word fits my feelings about them better than "drained". These platforms want to drain my skull of whatever is in there and use them to create their own thing.
When I first got on the internet in the late 90s, I had my own web site. I wrote for it regularly; had I known what a blog was, that's what it would have been. It was a great joy, and every word that I spilled out onto it came from my own creative process and had to push itself out onto the world. And while it was fantastically popular for the time, I don't think that engagement was ever what kept me writing. If anything, the pressure of the audience kept me from ever returning to it after I abandoned it.
I want that back. I want the work back. I want the fire back. Intellectually, I want independence, but in my gut I think I just want to go back and do it the hard way again.

I want a project.
And when did that happen? I've never been one for side projects of any kind. But as I've finally gotten over my distaste for programming (as can happen if you get spiritually out of whack with it) and started to interview around a bit, my mind has strayed to strange technologies I didn't get to work with when I was at BigCo.
At first I poked at Kotlin. Reading about coroutines was interesting, but I was only interested in Kotlin for instrumental reasons: Kotlin is the language of Android. If I were programming for fun, I'd want it to be totally different from the sorts of client development I've been doing for years.
And if I really want to learn something, I have to have a project.

So what exactly is this project, anyway?
Well, it's a blog.
Here are my goals:

* I want to have fun! I'm not entirely sure how having fun works, but that's what's driven my writing. Check out If You Want To Write by Brenda Ueland to get a flavor of how I'm thinking about this.
* I want to build it in Clojure. Not what I expected to hear myself say! Clojure had the right combination of being high level enough to be fun, disciplined enough to break me out of old habits, practical enough that I could get real work done, and novel enough that I would learn some new ideas. Here's the alternatives I considered:
    * Rust. For the longest time I said to myself, "Oh, if I ever commit to some new language, it'll be Rust," simply on the basis of scuttlebutt. But when I actually started looking at playing around with something, I never even stopped to think about Rust because low level programming with memory management and all is not and has never been something I've done for fun. I'm probably more likely to dick around with Nick Black's new ncurses++ library in C than to play with Rust if I'm being honest with myself.
    * Ruby. Ruby on Rails, specifically. If I were a better, less arrogant person, maybe I would've gone with RoR: everyone I've ever worked with or known who's come out of that community is absolutely dreamy, and their ethics around what sort of things are worth building and how tools should work are super nifty. But Ruby the language has never appealed to me, and Rails seems higher level than what I want to deal with (i.e. if I can avoid ever having to deal with a database, it will be a great development in my life).
    * Python? Kotlin? I never seriously considered these.
* I want a straightforward publishing flow that I can run on my local machine. My current dream flow is to write in Scrivener, export to MultiMarkdown, and run a command line tool to import and/or deploy.
* I want a straightforward deployment, too. I have never done server development, so I'm sure there are landmines here, but I'm guessing that either I'll be able to deploy to a hosted JVM thingamabob somewhere or another, or I'll have to statically render everything and host that instead.

And its name?
I believe that, when you come to any decision in life, you have to ask yourself one question first: Do I have enough information to make this decision? And if the answer is "Yes," then you have no reason not to make your decision immediately.
So the name of the blog engine is Gascan. That's the first word that came into my head; it probably popped in there because whenever I would go into the yard equipment room, it'd always be for the gas can for the lawnmower. And I think that's as good a name as any.
