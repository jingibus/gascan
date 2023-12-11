Building software is a process of making decisions and writing them down. Decisions like:

* What are we going to build?
* How are we going to build it?
* What should be in the public API for this class?
* Should we have an interface?
* Which classes should represent which things?
* Is this immutable data? Or is it a thing that has business logic?
* Should we prefix our instance variables with `_`, `m`, or nothing?
* Should I use an `if` statement here, or a `when`?

To a first approximation, efficient software development is all about making the correct decisions and executing on them rapidly.

For those of us who strive to excel at our jobs, the first step on this journey is usually to build more faster: to type faster, to problem solve faster, to iterate faster, to get code working faster. It’s an _additive_ process: we’re trying to make more and more and more, as much as we can, as fast as we can.

This approach is foundational, but it has its limits. Namely:

* _Our output is not perfect_. The more of it we make, the more of it we make — both good and bad.
* _We need feedback_. Most importantly, we need feedback on what’s good and what’s not, but we also need feedback purely in terms of alignment. And feedback is harder to give the more we have to process. the huge e-mails and PRs I wrote to earlier in my career never yielded effective feedback.

To move beyond that, it’s not sufficient to just go faster. You also have to stop doing things that are counterproductive. You have to integrate “no” into your process:

* *No huge PRs*
* *No untestable code*
* *No mutable data*
* *No unstructured CoroutineScope usages*
* *No unmonitored production code*
* *No shortcuts*

That’s discipline.

When you first start applying discipline, it sucks. It takes away tools you habitually used, and thus reduces your ability to get things done. But that slowdown is temporary: you’ve simply taken ineffective tools out of your toolbelt. Once you adapt and lean harder on your _good_ tools, you come out the other side a faster engineer, and a less burdensome teammate.
