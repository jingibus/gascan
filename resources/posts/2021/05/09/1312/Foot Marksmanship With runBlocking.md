There are a lot of ways you can screw up your coroutines code with `runBlocking`. But let's start by talking about `runBlocking`.
`runBlocking` is an essential tool in the coroutines ecosystem. You will certainly use it in test code. And once you start using it, you might run into a scenario that makes you think, "Hmm, I need to call a `suspend fun,` but I'm not already in a `suspend fun` and  I don't have a `CoroutineScope`." Or maybe you're in plain Kotlin code and need to get a `CoroutineScope`, but you know you should prefer not to construct one by hand (which is true --- avoid it if you can!).
And `runBlocking` seems to provide an answer. It does both of those things: It will let you call `suspend funs` _and_ it will give you a `CoroutineScope`. This is useful, and sometimes it's exactly what the doctor ordered.
But usually it's the wrong tool. And I don't just mean there's a more elegant way to do it; I mean that there are significant problems that can be introduced by using `runBlocking` in the wrong place. Like I said: there are a lot of ways to screw things up with `runBlocking`, and they're not all obvious at first glance. Here's the big ones I've run into the hard way --- so far!

### Broken CoroutineContexts

The first one I ran into is that `runBlocking` screwed up my `CoroutineContext`.
What is the `CoroutineContext`? It's a bag of stuff that's used to run your coroutine: things like the name of your coroutine, the dispatcher it's running on, the `DelayController` that you use to advance the clock in a testing context, that kind of thing. Each thing in the bag is uniquely associated with one or more keys, each of which hooks it up to a particular role:

        val delayController = coroutineContext[DelayController]

Some things may or may not be in the `CoroutineContext` --- for example, the `DelayController` above, which is specific to tools like `TestCoroutineDispatcher`. But other things like the `ContinuationInterceptor` absolutely will be in there, because without them the coroutine won't run.
Now, the nifty thing about the `CoroutineContext` is that it operates in a scoped fashion: if you start another coroutine from a `CoroutineScope` in your `CoroutineContext` (as is good custom), it will create its own `CoroutineContext` by starting with your existing `CoroutineContext` and then plugging up the values it needs (e.g. the `Job`).  This is how you can write `withContext(Dispatchers.IO) { ... }` and have everything inside run on the IO dispatcher: unless the code inside shadows that dispatcher with its own dispatcher, it will inherit those values and everything will Just Work.
Unless..... you use `runBlocking`.

### The Chain of CoroutineContext Custody

See, the whole mechanism I outlined above amounts to a chain of custody. At each step, someone passes the `CoroutineContext` on from one owner to the next.
That happens in one of two ways. The first one is between `suspend fun` calls:

        suspend fun suspendOne() {
          println("suspendOne: ${currentCoroutineContext()}")
          reportContext()
        }

        suspend fun reportContext() {
          println("reportContext: ${currentCoroutineContext()}")
        }

When you call a `suspend fun` from another `suspend fun`, you implicitly pass along your `coroutineContext`, too. Within a `suspend fun` you might choose to change up that `coroutineContext`:

        suspend fun changeContext() {
          println("changeContext: ${currentCoroutineContext()}")
          withContext(newSingleThreadContext("My Thread") {
            reportContext()
          }
        }

...and you can count on reportContext`()` running in the new context, because it's implicitly been passed along. Apart from the keys plugged in by `newSingleThreadContext`, it will have the same keys as the original context.
The other way this chain of custody happens is through the `CoroutineScope`:

        suspend fun launchCoroutine() {
          println("launchCoroutine: ${currentCoroutineContext()}")
          coroutineScope {
            launchAndReport()
          }
        }

        fun CoroutineScope.launchAndReport() {
          // No coroutineContext! Nothing to report here.
          this.launch {
            reportContext()
          }
        }

All new coroutines are launched from a `CoroutineScope` --- the `this` is unnecessary, but I include it to emphasize that it's an extension method, not a plain old `fun`.
But unlike the previous example, `launchAndReport` is not a `suspend fun`. It doesn't have a `coroutineContext`. The context chain is instead passed through the `CoroutineScope`, which carries it along and uses it to launch the new coroutines. This keeps the chain of custody going, and also wires up those coroutines to the `CoroutineScope`'s `Job` --- which makes it possible to cancel those coroutines.
The chain of custody is very neat and useful. Some of the most important abstractions in coroutines land are built on top of it. For example:

        suspend fun cancelAJob() {
          coroutineScope {
            val job = launch {
              longRunningProcess()
            }
            delay(1000)
            job.cancel()
          }
        }

`longRunningProcess()` can do literally anything in coroutines land --- create its own `coroutineScope`, a whole tree of child coroutines, suspend and wait forever, etc --- and `job.cancel()` will be able to cancel that job and all of its children, and everything will safely be cleaned up. And that all happens because of the chain of `CoroutineContext` custody.

### Breaking the Chain

At least, that's how it should happen. But it doesn't have to. `I`nstead of using one of the above two mechanisms to pass along the context, we might choose to implement `longRunningProcess()` like this:

        fun longRunningProcess() = runBlocking {
          launch {
            runJobA()
          }
          launch {
            runJobB()
          }
          ...
        }

It _looks_ fine. It gives us everything we need: a `suspend` context, a `CoroutineScope`, and a sealed abstraction: a function that runs until some concurrent work is complete, then quits.
But we break the chain. And we break the important guarantees that chain gives us:

* Were we running on a background dispatcher? Well, too bad --- you're back on default.
* Will that cancel work? Nope! `runBlocking` starts a completely new chain of jobs.
* If we're running with `runBlockingTest`, our test dispatcher won't work either.

### It Acts Like Thread.sleep

There's another reason it won't cancel besides breaking the job chain, though, which is that `runBlocking` behaves just like `Thread.sleep`. That is, when you call it it will hard block the thread --- the thread will sit there and wait for `runBlocking` to return.
I used to not think that sounded so bad. `Thread.sleep` is a basic tool, after all --- it puts the thread to sleep. What could be less complicated? Apart from being a code smell, this never caused me any huge problems in the world of threading. Why should it cause me any huge problems in the world of coroutines?

### Breaking the Coroutine Abstraction

Unfortunately it --- and `runBlocking` and anything else that hard blocks the thread --- does cause huge problems. These calls break the machinery that all the coroutine abstractions are built on.
Here's a real-world example to show what I mean. This is one of my go-to examples of a neat structured concurrent API from the Jetpack Compose docs:

        LaunchedEffect(scaffoldState.snackbarHostState) {
          scaffoldState.snackbarHostState.showSnackbar(
            message = "Error message",
            actionLabel = "Retry message"
          )
        }

`LaunchedEffect`'s body is a coroutine, same as you'd create with `launch`. And `showSnackbar` --- well, it shows a snackbar. When the user taps on the snackbar, this `suspend fun` will return.
It doesn't necessarily have to wait that long, though. You could add a timeout, so that if the user fails to dismiss it after a few seconds, it goes away on its own:

        LaunchedEffect(scaffoldState.snackbarHostState) {
          withTimeout (5000) {
            scaffoldState.snackbarHostState.showSnackbar(
              message = "Error message",
              actionLabel = "Retry message"
            )
          }
        }

Pretty nifty!
This works through a couple of mechanisms. One is the hierarchical cancellation described above: when `withTimeout`'s trigger goes off, it cancels the current coroutine's `Job,` which then cancels its whole tree of jobs.
But what sets off `withTimeout`'s trigger? Essentially, a message loop, like Android's main thread.
And you can _break_ Android's main thread by hogging it with long running work. Can you break the message loop `withTimeout` relies on?
Absolutely:

        LaunchedEffect(scaffoldState.snackbarHostState) {
          coroutineScope {
            launch {
              Thread.sleep(Long.MAX_VALUE)
            }
            withTimeout (5000) {
              scaffoldState.snackbarHostState.showSnackbar(
                message = "Error message",
                actionLabel = "Retry message"
              )
            }
          }
        }

Here we `launch` a coroutine that goes to sleep forever. It won't start running immediately, so if we step through in a debugger we'll see `withTimeout` and `showSnackbar` run.
But at some point, execution will switch to that sleepy coroutine. When it does, the message loop will sit there on `Thread.sleep` until the end of time.
This is horrible. It's horrible that everything shuts down, of course, but put yourself in the shoes of someone trying to debug this: it _looks_ like `withTimeout` is the piece that is broken, because the timeout is failing to trigger. But the bug is actually over in the coroutine that called `Thread.sleep`.
This small example makes the problem obvious. But the person debugging this in the real world will have no clue where that `Thread.sleep` is. It could be almost anywhere.

### Blocking Is So, So Bad

And that's what makes `runBlocking` so amazingly bad: its behavior is identical to `Thread.sleep`. Any kind of long blocking non-suspend fun is just as problematic, causing non-local hangs of your coroutines code.
There are tools to get around this. `runInterruptible` will patch over this for you:

        LaunchedEffect(scaffoldState.snackbarHostState) {
          coroutineScope {
            launch {
              runInterruptible {
                Thread.sleep(Long.MAX_VALUE)
              }
            }
            withTimeout (5000) {
              scaffoldState.snackbarHostState.showSnackbar(
                message = "Error message",
                actionLabel = "Retry message"
              )
            }
          }
        }

This takes something that is interruptible in the Java threading ecosystem (`Thread.sleep`, and anything else in the JVM ecosystem), and makes it an interruptible `suspend fun` in the coroutines ecosystem. So there is an escape hatch! But unfortunately, nonlocal code that fails to use that escape hatch can really wreck your day.

### Avoid Blocking!

The takeaway here is that you should avoid a hard block at all costs. `suspend funs` can wait as long as they like, but any plain old method that takes a while should be treated with extreme caution, as it can have severe nonlocal performance impact, or even mysterious nonlocal bugs.
And `runBlocking`? Its use should be restricted to invocations that are unavoidably embedded in a non-coroutines scope, and (if at all possible) are not nested in a coroutines call stack. In day-to-day Android development, that means that with the exception of methods annotated with `@Test` you should use it as often you would call `Looper.prepare(); Looper.loop()`. (That is, approximately never.) If you see it in code review, red flag it immediately as something that requires a strong and compelling need to be there.

Thanks to Adam Powell and Ken Yee for technical feedback on this post, and to Fatih Giris for discussion on the topic.
