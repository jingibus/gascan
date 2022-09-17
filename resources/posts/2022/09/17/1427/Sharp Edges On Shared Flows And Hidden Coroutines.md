I shared a code lament on Twitter the other day that got me thinking "Hey, I should probably write down some thoughts about this."
Here's the code in question:

        val eventsChannel = Channel<UiEvent>(BUFFERED)
        lateinit var events : Flow<UiEvent>
        val job = launch { 
          events = eventsChannel.consumeAsFlow()
            .shareIn(this, SharingStarted.Lazily) 
        }

It's actually a pretty darned useful snippet! But the reason it's pretty darned useful is that it illustrates at least three tricky coroutines ideas into four short lines of code. Some of them I unpacked at length [here](https://code.cash.app/rx-to-coroutines-shared-flows), but I'm going to try to use this little example to unpack them all more quickly in a shorter post. Should be fun!
Here's the first takeaway:

## Every Shared Flow Has A Driver

The first time I encountered `shareIn` I found it frustrating. Why can't I do something like this?

        val sharedFlow = flow.share()

I can't do that, because `shareIn` takes in a `CoroutineScope`. Okay, fine. How do I get a `CoroutineScope?`
Well, the quickiest and dirtiest way is to construct one:

        val scope = CoroutineScope()
        val sharedFlow = flow.shareIn(scope, SharingStarted.Lazily)

But if I do that, I abandon structured concurrency and open myself to the risk of leaking coroutines. No good! `CoroutineScope` should only be used to bridge with non-coroutines code, so let's avoid it.
I could use the `coroutineScope` block scoped function, which is structured concurrent. But if I try that:

        coroutineScope {
          val sharedFlow = flow.shareIn(scope)
        }

...the `coroutineScope` block never returns.
This is *kind* of a big deal if your function signature looks something like this: `suspend fun myFunction()`. In a structured concurrent world that signature says, "All of my concurrent work is cleaned up by the time I am done running." That's why `coroutineScope` works the way it works, and that's why `coroutineScope` is the right tool for the job for functions like `myFunction`.
But why does `shareIn` have to launch a coroutine in the first place?? Can we possibly worm our way around this requirement and get working code?
The only way to make sense of this weird trap is to know a fundamental truth about all shared flows:
_Every shared flow has a driver outside of the collecting coroutine._
What do I mean by that? Well, if you invoke `collect` on a `SharedFlow`, the data you receive always (always!) comes from a sequence of operations that lives _outside the current coroutine._ (Unless it's coming from inside the `collect`, in which case you're probably having a clever day for yourself)
That sequence of operations could be running in an OS thread like the Android View system's main thread. It could be coming from a coroutine; it could be coming from _multiple_ coroutines. It could even be coming from Jetpack Compose, but it isn't coming from the coroutine that you invoked `collect` on.
That's the underlying reason why we ended up iterating towards this code in the first place:

        coroutineScope {
          val sharedFlow = flow.shareIn(scope, SharingStarted.Lazily)
        }

We have to have a `CoroutineScope` because _something_ has to drive our resulting `SharedFlow`, and since we're using coroutines that something must be a new coroutine. `shareIn` launches that new coroutine, calls `collect` on `flow`, and then emit each item into the shared flow as it arrives.
But where's that coroutine? The answer is, uhh --- it's somewhere in the `CoroutineScope`. But we don't actually have a `Job` that points at. Without that `Job` we can't cancel it, and until we cancel it our `coroutineScope` can't return.
That brings us to the next takeaway:

## Killing Hidden Coroutines Is Annoying

Some coroutines are hidden. Take a look at `shareIn`'s signature:

        fun <T> Flow<T>.shareIn(
            scope: CoroutineScope, 
            started: SharingStarted, 
            replay: Int = 0
        ): SharedFlow<T>

`shareIn` takes in a `CoroutineScope` as a parameter. It does this so that it can launch a coroutine. But the coroutine that `shareIn` launches is hidden! And unfortunately, _there is no graceful way to get a handle on hidden coroutines_.
That doesn't mean it's impossible, though. It's probably possible to fish that `Job` out by walking the job hierarchy, but I use a much easier workaround for this problem. Here it is by itself:

        coroutineScope {
          lateinit var sharedFlow: Flow<T>
          val job = launch(start = UNDISPATCHED) {
            sharedFlow = flow.shareIn(scope, SharingStarted.Lazily)
          }
          // do some stuff with sharedFlow
          job.cancel()
        }

The idea here is that instead of finding that hidden job (jobs? there could be several!), we create a new job to be its parent. Then we can cancel the parent to kill the hidden job.
But we still need the return value from `shareIn`. If we define an output variable, it has to be defined outside of the `launch` block if we want to access it outside of that block. And to ensure that the value is assigned promptly, we have to pass in `start = UNDISPATCHED`. This ensures that the launched coroutine is initially run immediately on the current coroutine, without waiting to run on its target dispatcher.
Oh, and it might be `null`, too! So we need a little box to put it in.
It's an easy dance to screw up, which is why I find myself writing this helper function sometimes:

        fun <T> CoroutineScope.launchWithJob(
          block: CoroutineScope.()->T
        ) -> Pair<T, Job> {
          val output = mutableListOf<T>()
          val job = launch(start = UNDISPATCHED) {
            output += block()
          }
          return output[0] to job
        }

I have yet to find a good place for it to live, though. I'm not sure I love the name, either. So until then, I usually end up writing this workaround by hand each time.
Okay, last takeaway. Number three:

## Wiring Up 0-Replay SharedFlows Is Racey

You might wonder why we need to use `shareIn` in the first place in the example above. After all, hooking up events _should_ be straightforward. For example, you might hook up a presenter to a view with code that looks like this:

        val events = MutableSharedFlow<UiEvent>(BUFFERED)
        val models = MutableStateFlow<UiModel>(UiModel.Initial)

        launch {
          displayView(models, onEvent: { events.tryEmit(it) })
        }
        launch {
          runPresenter(events, onNewModel: { models.value = it })
        }

The `models` hookup works great here, because it's stateful: conceptually it represents the current state of the `UiModel`. If `displayView` is racing ahead of the presenter, that's no big deal: it will take a bit before a new model is generated and displayed, that's all. If it's lagging behind the presenter, that's also no big deal: when the view finally catches up, it will display whatever the current model is. Older models are skipped, and that's fine.
The `events` hookup could be broken, though, depending on which runs first: view or presenter. Here's how:
When `displayView` calls `onEvent`, it will try to emit an item into the `events` `MutableSharedFlow`. But if the presenter is not collecting at the time `tryEmit` is called, that event will never be received. By anyone! It will just get dropped on the floor.
Now, in production, this is probably not a big deal: views shouldn't emit events the moment they fire up, anyway. But in test, it's an annoying problem, because we don't know exactly what hula hoops the presenter will jump through as it's hooking up its event collector. We can "fix" the problem by adding a non-zero `replay` value to `shareIn`, but that's not right, either, because it doesn't match the semantics of an event: if the presenter decides to fire up a second collector later on, it will receive additional copies of the event.
The solution is to pre-buffer: create a buffered `Channel`, and send the events into that `Channel`. To share those events with multiple listeners, use `shareIn` to lazily share the contents of the `Channel` as soon as someone subscribes.

## All Together Now

Of course, `shareIn` will have to start a coroutine (because of takeaway #1). And we'll need to clean things up when we're done, so we'll need to do that weird do-si-do to get a handle on that coroutine's `Job`  (because of takeaway #2).
Which brings us all the way back to the beginning of the post:

        val eventsChannel = Channel<UiEvent>(BUFFERED)
        lateinit var events : Flow<UiEvent>
        val job = launch { 
          events = eventsChannel.consumeAsFlow()
            .shareIn(this, SharingStarted.Lazily) 
        }

All three takeaways are in this one little snippet: `shareIn` creates a hidden coroutine to drive the shared flow of events, the `lateinit` + `launch` combo captures the hidden coroutine so that we can cancel it at cleanup time, and the buffered `eventsChannel` guards us against races on `MutableSharedFlow.tryEmit`.
Whew!
