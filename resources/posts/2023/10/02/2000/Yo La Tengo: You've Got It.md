_I wrote the following post earlier this year and posted it to our internal Cash Mobile discussion forum. It was well received, and several engineers have told me it helped them find their way a bit better._

_Not every company works the same, of course; what applies for our company may not apply for yours. My colleagues and I feel like Cash App's culture is pretty special, though, and that this conveys a bit of why that is so. So I thought I'd share it publicly here._

If you’re new at Cash, you might find our client engineering culture disconcerting. You might be walking around with your hand raised: “I want to do something cool. Pick me, pick me!” And nobody ever picks you. Or you might be bored, wondering when you’ll be assigned something to do: “When will somebody give me a project?” And you get some project work, but not enough to keep you busy all the time.

Meanwhile, others of us who have been around for a while look around and say, “How am I ever going to get to everything I want to do?”

Sometimes people do get picked — that happened with the Arcade project. And every engineer has times when they’re swamped by project work. But for most of us, we fill up that additional time by solving problems that nobody picked us to solve. Nowadays, some of that code does have a single maintainer (and that will probably happen more as we grow), but if you look at the git logs for the tools you use, you will find that they were all built by engineers like you, who were solving a problem that was in front of them.

How do we divide work this way, though? Isn’t it pure chaos?

## Yo La Tengo: “I’ve Got It!”

Baseball has a similar problem: when the person with the baseball stick hits the ball really hard, I’m told that the ball will fly up in the air really high. When that happens, somebody needs to catch the ball on its way down, or it will fall on the ground and something bad will happen. (They will lose the baseball match?)

But who? If a few people try to catch the ball at the same time, they will run into each other and neither will catch the ball. And it’s impossible to decide in advance: you can’t know where the ball will go until it’s hit.

The solution is to “call” the ball: if you can see that you can catch the ball, you yell out “I’ve got it!” (Or if you speak Spanish, “Yo la tengo!”) If you’re first, you get the job of catching the ball.

## If You See It, You Can Solve It

The same thing happens in our codebase all the time: a problem comes up that needs to be solved. Who will solve it?

Usually, if you are experiencing the pain from the problem, you’re probably the best person to build the solution. That’s because:

* You’re motivated, because you feel the pain more closely than anyone at that moment
* You understand how the problem relates to real business problems, because you were probably working on one when you encountered it

And you should probably try and build it _right now_ if you can. Motivation and understanding both have a short half-life: we can only focus on one thing at a time, and our working memory is soon filled with context from other problems.

## How Do I Call It?

When we live up to our mantra of “no cookie licking”, you will find that nobody stands in the way of you solving any problem you understand and are motivated to solve.

We don’t actually literally call “Yo la tengo!” or “I’ve got it,” though. To avoid running into other people working on the same thing (and also to find quality feedback on your approach), you will want to find one or two collaborators. Teammates you know are a good choice, but if you can find someone who has already worked on the code in question that will be even better. Look through a git blame to see who has touched the code recently, and you’ll often find a good starting point.

Once you’ve done that, you will want to get into action. That can look different depending on the kind of problem:

* Issue-driven changes are usually PR driven: a tactical fix that only impacts your code is safer, but following up with a PR that eliminates the issue for everyone is good citizenship that we take pride in. We don’t expect or require broad approval for these kinds of changes: As long as you find one or two thoughtful reviewers, you should be fine.
* Changes to core components with more complexity under the hood will usually have more expertise associated with them. In these cases, reaching out to one of the core maintainers is the best foot to start on.
* For other components with high fan-out, a proposal followed by discussion may be required. For example, changing all of `FakeAppService`’s test points to use `Turbine`: to make that change, I needed at least one or two people who were excited about it, but I also needed to make sure that nobody had any major objections.

## Solve Problems; Get Paid

Let’s face it: we’re smart folks here. We could all have successfully done something else with our lives besides write software.

But we didn’t! Somehow or another, we started writing programs for other people. And now we get paid money to do it.

There’s just one trick: staying motivated. Our worst enemy is learned helplessness. If we don’t see how our day-to-day work is connected with our day-to-day problems, we will stop seeing problems as opportunities and instead see them as meaningless troubles best avoided.

But life ain’t about avoiding problems: it’s about going places. And you can’t go places without solving problems! So if you find yourself inspired to solve a problem, remember that you won’t be able to set that inspiration aside and pick it up again tomorrow. If you have the time, opportunity, and motivation, you’ve got everything you need.
