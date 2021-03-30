It seems like my strong opinion on `launchIn` got some traffic. Good! I'm glad it got people thinking. This post will be more rambly and probably get less engagement, but that's fine. There is a larger discussion that it was a part of (and is still continuing as I write this), and I want to write down the big ideas now so that the context is there in the future.
First, for the record: combining `launch` and `collect` into a single method call has the same problems I called out with `launchIn`. So `collectIn` should be avoided, too.
Now for those big ideas. I'll come at them in a roundabout way by responding to the most pointed and well-informed critique I received, which has to do with a bug called "double collect".

### What about infinite loops?

This critique came from Ian Lake, a developer who works on the Android Toolkit. Ian is a brilliant person who thinks about API problems I don't. I'm a fool if I don't take his perspective seriously, and given the difference in my point of view (a senior engineer on a midsize app within a sub-FAANG tech company) and his (a presumably quite senior engineer who is regularly in direct conversation with engineers at a wide variety of organizations who don't care about these debates and only want to get shit done), he could be a fool if he took my opinion as gospel truth.
The topic of discussion was an error that is possible in the style I advocate, but impossible when using `launchIn`. The "double collect bug", which happens something like this:

        scope.launch {
          events.collect { event ->
            updateViewModel(event)
          }
          featureFlags.values(BtcGiveawayFlag).collect { flagValue ->
            toggleBtcGiveaway(flagValue.isEnabled)
          }
        }

When you run this code, the second loop never runs because the events Flow is endless. It never terminates, and so the feature flags code never runs.
It doesn't surprise me to hear that this is a common bug. Ian and I disagree on whether programmers should be guarded from it. Here's [his critique](https://twitter.com/ianhlake/status/1376012383005466628):

> Did you often run into function calls that never ended previously? I would imagine those would be extremely rare compared to the proliferation of endless flows. In fact, it seems like they're on polar opposite ends of the expected spectrum (very rare <> very common)?

And here's a followup where he puts it a bit more pointedly:

> So: a double collect shouldn't have a lint check because that's not a scary thing to do, despite it not working (only at runtime) on the endless flows that proliferate in the common collecting for the UI cases?

I had a big argument about this, which you can go read on Twitter if you like. I won't recapitulate it here, but the core of the disagreement comes down to two ways of programming in Kotlin coroutines.

### Royal Rumble: Reactive Versus Structured Concurrency

In short, `launchIn` is a tool for writing purely reactive code with the coroutines libraries. If you understand how to write reactive code or if you already have a bunch of reactive code, you can switch to using Flow, use `launchIn`, put "Kotlin coroutines" on your resume, and call it a day.
By saying, "Avoid `launchIn`," I'm drawing a line in the sand that says "Don't write purely reactive code with coroutines."
I'm doing that because I'm interested foremost in our own codebase, which is already built on top of a powerful general purpose reactive programming toolkit, RxJava. And we're still on RxJava 2, not 3.
Why? Because moving between toolkits is a huge technical effort. It's not worth it if it doesn't give us some big wins.
If we were built on a restricted reactive framework like LiveData, then moving to Coroutines Flow would give us that big win. We'd switch over in a heartbeat.
But we aren't. So why should we bother?
We should, but not to migrate to Coroutines Flow. We should migrate to _structured concurrency_.
If you haven't already read the foundational text on structured concurrency, [here it is](https://vorpus.org/blog/notes-on-structured-concurrency-or-go-statement-considered-harmful/). It's great. In this less fantastic post, I'll be writing more about the consequences.

### Let Me Tell You The Good News About Structured Concurrency

It is no exaggeration to say that structured concurrency is a paradigm shift that deletes (or at least strongly threatens) 30+ year old foundational rules for building snappy, stable client applications within UI frameworks. Rules like these:

* Never write infinite loops (unless you're a framework developer)
* Use objects to manage threads or other concurrent resources
* Never block, always use callbacks and return quickly

Within a `suspend` context passing `CoroutineScope` down the stack, it can make sense to violate all three of these rules.
That's pretty wild. The scope of the consequences of this change in the rules is so large that it will take time for people to wrap their brains around it.
Here's one consequence: APIs that assume a structured concurrent style will not look like current callback-centric APIs. Jetpack Compose is one of those APIs, the first API that many Android developers will be exposed to that allows them to safely violate these rules.
Here's a completely useless but easily understood example that violates two, maybe three of them:

        @Composable
        fun MyScreen(
            state: UiState<List<Movie>>,
            scaffoldState: ScaffoldState = rememberScaffoldState()
        ) {
            // If the UI state contains an error, log some trash to Logcat
            // once a second
            if (state.hasError) {
              LaunchedEffect("annoyance") {
                while (true) {
                  Log.i("MyScreen", "Oh no, we have an error")
                  delay(1000L)
                }
              }
            }

            Scaffold(scaffoldState = scaffoldState) {
                /* ... */
            }
        }

I've written an infinite loop, I've blocked my caller, and I've implicitly defined a long-running process without saving off a handle to it anywhere.
And yet it works! And it's safe!
It's not crazy, either. The real example from the documentation also makes a function call that blocks for a while:

        @Composable
        fun MyScreen(
            state: UiState<List<Movie>>,
            scaffoldState: ScaffoldState = rememberScaffoldState()
        ) {
            // If the UI state contains an error, log some trash to Logcat
            // once a second
            if (state.hasError) {
              LaunchedEffect(scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar(
                  message = "Error message",
                  actionLabel = "Retry message"
                )
              }
            }

            Scaffold(scaffoldState = scaffoldState) {
                /* ... */
            }
        }

`showSnackbar` blocks for _seconds_. It doesn't return until the user sees the snackbar and interacts with it.  
And this is a great API. With this API, the function that shows the snackbar can return the result, allowing you to handle it right there:

        LaunchedEffect(scaffoldState.snackbarHostState) {
          val result = scaffoldState.snackbarHostState.showSnackbar(
            message = "Error message",
            actionLabel = "Retry message"
          )
          when (result) {
            ActionPerformed -> {
              /* action has been performed */
            }
            Dismissed -> {
              /* dismissed, no action needed */
            }
          }
        }

No callbacks or threading required.

### Back to launchIn

So that's structured concurrency in a nutshell. What does that have to do with double collect?
Let's look at the bug again:

        scope.launch {
          events.collect { event ->
            updateViewModel(event)
          }
          featureFlags.values(BtcGiveawayFlag).collect { flagValue ->
            toggleBtcGiveaway(flagValue.isEnabled)
          }
        }

In a reactive programming world, infinite loops and long running function calls are dangerous. They are _never_ correct. And Flows that terminate aren't super useful, because the constructs for running code after termination are awkward.
In a structured concurrent world, infinite loops and long running function calls are valid, useful constructs. They allow programmers to write meaningful logic. If a programmer understands that model, and you write a lint that says, "Hey, don't write any code after `collect`. It's never going to run, because all Flows are infinite," you hamstring what they can do.
But people will write this bug, and they'll be thoroughly confused by it. They will write it not because the code is unclear, but because _they believe that collect doesn't block_.
Now if they're writing it, that's dangerous and probably does call for a lint check. But if you are an engineer, you should internalize that `collect` suspend blocks. If you have influence over other engineers, you should nudge them towards understanding that `collect` suspend blocks.

### Nudge Towards Structured Concurrency!

There are two factions today in Android: the ViewModel faction, and the Compose faction. The ViewModel faction is writing materials in the all-reactive Flow style that never confront the big ideas above; the Compose faction is in the bag for structured concurrency, and is melting through everyone's preconceptions about how to build a client app.
Heaven help them, they seem to believe that they are both working to move Android developers to coroutines. But they're working at cross purposes.
Throw me in with the Compose faction. I'm nudging towards structured concurrency.
