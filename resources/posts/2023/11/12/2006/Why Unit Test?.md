Nothing worth knowing can go on living if it isn’t repeated. That especially applies for unit testing! This is because unit testing is two things: a practice, and a corpus of code.

The corpus of code is easy to repeat, especially if you’re a large organization with some inertia, like Cash App is at this point. We have a bunch of existing unit tests out there, which any engineer can read and understand and copy. We have the tooling that makes it easy to write your own if you like, and most of our teams have a strong culture of insisting that we have unit tests for any new code.

If you produce that code, you get a few of the benefits of unit testing:

* Hard documentation about what your component actually does
* Protection against regression for the behavior under test
* “Test pain” — by writing the unit test, you are forced to use the API of the subject of your test code. This can give you an early signal that your API shape is bad, or that the behavior is too complicated (and hard to write tests for)
* “Double entry accounting” — by writing code around the expected behavior twice, you get a redundancy check against errors introduced by the author of the code

These benefits are enormous, enough to justify the practice of rejecting a PR if it doesn’t have unit tests.

However, unit testing is also a practice, a way of viewing the development process. This practice cannot be written down in code; it can be written about like this and it can be spread via pair programming, but you can’t reject a PR because they didn’t write their code in the “right” order.

Not that there is a “right” order, necessarily — test driven development would hold that there is, but unit testers are a wider crew than the TDDers. I will say that there is a “wrong” order, though: *if you write your unit tests last, you’re missing out on the benefits of the practice.*

The practice of unit testing isn’t laser focused on the unit test code itself as being a work product. Instead, the unit test gives you a place to write code that isn’t production. It’s a place to write speculative code — what happens if I do this? What if I plug in a different implementation that looks like this? More importantly, it is a tool that allows you to, in a sense, shut the door and have some peace and quiet: rather than consider *everything* going on with your app or service, the unit test allows you to focus your thought exclusively on the piece of functionality you are working on.

I think this practice is even more important than the tests themselves, because you can’t always write good unit tests.

Here's an example: a couple of years ago, I built support for 3DS2 in Cash App. This required integrating with a 3rd party library that, at the time, was moving pretty quickly. Writing unit tests for our integration with that library would’ve been a fool’s errand, because our tests would’ve broken on every release.

That is no excuse for giving up on the principle of having an isolated place to exercise that code, though. So instead of writing unit tests, I built a small app that included a toy server integration. It wasn’t perfect, and didn’t stay working for very long, but while we had it it was invaluable, because it allowed me to think about our integration without thinking about the blockers flow, issues with the server integration, or other Cash-specific details.

If you make a practice of doing this to the best of your ability, wherever you can, your systems will be easier to validate, easier to stand up, easier to share, and easier to maintain. If you don’t, all of those properties will slowly degrade as your code eventually congeals into an indispensable, critical prison cell for your career, inescapable without breakage.
