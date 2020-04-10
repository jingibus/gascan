Yesterday, having gotten far too little sleep the night before, I decided it would be a good time to work on local file import for Gas Can, the publishing tool I've been working on. (And that you're reading this on, if all goes well.)
It didn't go as easily as I wanted it to. I thought that Flexmark would cooperate with me, and I ended up fighting hard with it. But in the process, I found myself in a great example of a spike --- a long period of intense focus on a single clear problem, in a tight feedback loop.
In some ways, this was awesome and fun. I built tools to reload my test content and see the result (or failure) that I wanted to see, try out different solutions, and analyze the library I was grappling with.
And in other ways it was kind of a disaster. I was completely wrapped up in the work. From 5pm to 10:30pm, I kept rolling on the ice cream I ate in the afternoon and didn't eat a proper meal. I didn't get up from the computer or take a moment to think about anything except the problem --- I spent the whole time in front of emacs: clicking on things, keying in C-c C-e over and over, and cursing deep inheritance hierarchies. (Maybe that last bit was time well spent, actually.)
So what's good and bad about this? Let's start with the good:

### You'll solve it

I did solve the problem! This in spite of the fact that it was mired in a bunch of code that I didn't like, that I found to be overcomplex and obscure. Rage, rage, rage, but --- it works. The solution isn't guaranteed to come, but it probably will.

### It's engaging and fun

For better or worse, when you get into something like this the spirit of the work possesses you. You forget about eating, your personal life, work anxiety, and anything else that might be occupying spare cycles in your brain.
It's wonderful. Every moment spent not thinking about myself is bliss.

### Unexpected fruits

The bulldozer of a good focus loop will, for good or ill, crush everything in its path. If there's some obstacle between you and your objective, you'll find something that helps you get past it.
I was struggling to make an object with a huge inheritance hierarchy work my way. As a result, I have a pile of tools for investigating huge inheritance hierarchies. I didn't plan on building that, but I did and now I have it. Nifty.
It's not all good, though. What's bad:

### You'll leave a mess

Laser focusing on the problem is wonderful, but what about the organization of the code? What about blind alleys, things you tried that didn't work?
If you thought about those things, you wouldn't have been able to lose yourself in the tight feedback loop in the first place. That's why the loop works: your whole thought process can focus around one thing, discarding every other concern.
Those other concerns don't go away, though. When you reach the goal, you'll wake up with a little coding hangover: "What did I do today, anyway?"

### You'll be inefficient

Focus changes the way you think, as does frustration. Lateral solutions superior to the tool at hand will not present themselves, and you will do things the hard way.
Last night I spent a lot of time hammering away at my problem by applying a variety of other techniques I had previously used successfully, and by fuzzing the object by calling methods and seeing their results. I was so frustrated by the way they had chosen to architect their solution that I never bothered to dive into it. Now that I have the solution in hand, though, I can see that slowing down and understanding the library would have paid off more quickly than hacking away at it.

### Takeaways

So what should I learn from this?

### 1. After focus time is done, reflect on it.

The core problem here is with the tight feedback loop: laser focus highlights one thing, and ignores everything else.
So if you have spent a long time in a tight feedback loop, make sure and emerge from it and do some higher-level reflection. Odds are good that you won't remember everything you did in that focus time until you take some time to unpack that work.
Assuming that the goal of the loop was achieved, the most important questions to answer are these:

* What questions does the solution answer?
* Is this solution good or bad? Is it what I actually want in my codebase, or do I want something else?
    * Related: are there any concepts you need to throw away? Did you write any code that relies on it those ideas, and can you get rid of it?
* Did I make or discover anything else worthwhile in this loop?
* Did I make any errors while in this loop? Were there alternative choices that could have closed the loop more quickly? Make sure and have sympathy for yourself: learning requires pursuit. There is no magic oracle that will divert us from blind alleys.
* Is there any other cleanup that needs to happen? Can I improve my process next time to obviate the amount of cleanup I need to do?

### 2. Consider the target of your focus loop before entering it.

Focus loops are powerful and fun, but they are a commitment. You are going to dive in on one end, and come out on the other side with... something. Like Indiana Jones, you might finally get the Ark of the Covenant, but maybe not in the way you wanted.
Life is driven by such quests. But if your life or your project is pulled along by a neverending chain of targets of opportunity, where will you end up? Certainty is not achievable, but participation is.

### 3. Have broader loops to fit the focus loops into.

Every evening I take some time to ask, "How am I feeling? How was my day today? What did I do?" And every morning when I sit down to work, I ask "What do I plan on doing today? What did I do yesterday?"
These are broader loops of reflection. For me, if they're not habits, they don't happen. I have to write the down, too, whether I read them or not.
I also know from experience that if I don't engage in these reflections, the tight loops will eat me alive. I won't eat, I'll neglect my friends and other personal relationships, and so on. And Ill never be able to address #2 effectively, either.

### 4. There is ALWAYS a broader loop.

Even if you are plugged in to a standard corporate feedback cycle and are happy as a clam, remember that the loop you are in serves some other purpose. Someone built that loop to serve that purpose. Therefore, if you do not maintain your own independent loop of reflection, you will be utterly dependent on that purpose.
Oh, and one last thing: for heaven's sake, take it easy on yourself. Let none of this be a cudgel, but rather an inspiration. Take what you like, and leave the rest.
