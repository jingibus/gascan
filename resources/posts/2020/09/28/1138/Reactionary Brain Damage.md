At my new gig, I've started working on a lot of reactive code, which I've never done before in production. It probably puts me quite behind the times to say out loud in public that it's a nice experience.
But I'm not completely happy with it at times. Our presenters are generally of the form of transformers from view events to view models, with a side effect stream of navigation events. Internally, most of them are written exclusively in terms of various RxJava operators.
This has been worrisome to me from the word go, because I've always been suspicious of the amount of discussion that happens purely in the RxJava domain. It's a powerful abstraction, but how fine and clean is the work, really, if I can see so many top notch minds chatting with each other about how to accomplish X, Y, or Z? If it truly simplified things, I'd expect less chatter about the tool.
Anyway, a colleague has written a tool that I and some others have been using internally to address this. And while I'd like to make the case for it, practically speaking I don't feel like "Oh, presenters have state, you should have a representation of their state" is good enough. I need to understand the problem.
And last week I had a perfect opportunity to do so. I spent a couple of days struggling to refactor a reactive presenter and its view, and it was not a pleasant experience. I was trying to establish the `ViewModel` pattern I wrote about [here](https://cashapp.github.io/2020-09-23/what-i-like-about-views). After many different stabs at it I came up for air, and my hands were empty. But I'm at a loss to explain how I failed. And here's why:
I didn't write a single thing down.
Here was my process: I came up with some idea purely in my head, and then I started working towards it in code. When I came upon a problem, I responded by attempting to immediately conceive a solution and put it into practice. And I did this over and over and over again.
If it had all worked, that would have been well and good. But it didn't. And what do I have to show for it?

### Reactionary brain damage

The worst thing that we can do to ourselves as knowledge workers is damage our understanding of what we're doing. And that's exactly what I was doing: I was so laser focused on the work itself, that I failed to use it to nourish my starter dough, my understanding of what I'm doing.
Being reactionary means going through life in a stateless fashion. Every time I run into an obstacle, I react to it with my mindset at that moment. And then I get to the end of the day, and what do I have?
Well, if everything goes well, I have a finished work product. It will have solved my problem, so it will contain an understanding of that solution.
But what if I don't have a finished work product? In that case, all I have is my memory and my feelings about what I did. I don't have any kind of hard record. And thus, when I approached my keyboard this morning to try to write about it, I had little other than frustration to report.

### Write things down!

I'm new to this practice, and obviously still imperfect at it, but I can't recommend enough the merits of writing things down. Even if all you have is a stream of consciousness, that pushes your perspective a valuable gap away from the reactionary point of view. My old colleague Mark Dalrymple wrote an [evergreen post](https://www.bignerdranch.com/blog/adventures-in-debugging-keeping-a-log/) about using this technique for debugging, but don't limit yourself to debugging. Understanding is the starter for our work; write down everything you can. If you don't, it's like kicking your past self off the team.
