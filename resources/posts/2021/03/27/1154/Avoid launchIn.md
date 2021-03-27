Coming from RxJava or other reactive frameworks like LiveData, `launchIn(CoroutineScope)` is an attractive tool. To see why, take the following example:

        disposables.add(events.filterIsInstance<CloseClicked>()
          .doOnEach { navigator.goTo(Finish()) }
          .subscribe()

In coroutines, `launchIn` allows me to write the same thing in the same way, but better:

        events.filterIsInstance<CloseClicked>()
          .onEach { navigator.goTo(Finish()) }
          .launchIn(coroutineScope)

This code can be written in all the same places as the Rx version, but it is better:

* If you forget to add your subscription to a `CompositeDisposable` in RxJava, you've done it wrong and will leak your job. With coroutines, you have to intentionally create a scope and throw it away to commit that kind of error.
* Putting parens around the whole expression is annoying to type. We fix that at Cash App by having our own `+=` extension, but that's still not as nice a flow under your fingers as the coroutines code.

And yet `launchIn` is bad and you should avoid it.

### launchIn is a conceptual trap

The problem is that coroutines aren't callbacks, like LiveData and RxJava are. They're _better_ than callbacks.
You can also write the above example like this:

        coroutineScope.launch {
          events.first { it is CloseClicked }
          navigator.goTo(Finish())
        }

This is better than either example above.

* There's no iteration. Why _should_ there be iteration? There shouldn't be (we should only navigate away once), but we usually write it that way because reactive code makes it harder _not_ to iterate.
* The code inside the block is refactorable: you can highlight it, extract a `suspend fun` method, and run it anywhere you're in a suspend context as a simple method call, whether you need it to run in a job or not.
* It gives you one of the big structured concurrency wins: The indentation visually shows "Hey, this code is running in a coroutine."

### Write suspend funs, not async callbacks

What about the exact non-launchIn equivalent, though?

        coroutineScope.launch {
          events.filterIsInstance<CloseClicked>()
            .collect { navigator.goTo(Finish()) }
        }

Even here you are better served with this code because the inner block is straightline code written in a `suspend` context.
Prior to coroutines, straightline code was worse than async callback code. "Don't write blocking code," we learned. "Wrap blocking code in something you can run in the background." And by "run in the background," we meant "wrap it in some kind of callback" --- `AsyncTask`, RxJava, LiveData, Futures, Volley, or even our dear old friend `Thread`.
Coroutines flip this relation upside down. It is trivial to run a `suspend fun` call concurrently. You call `launch` in a `CoroutineScope` --- done. It is _more work_ to turn a callback written with `launchIn` into a `suspend fun`.
So don't use `launchIn` --- use `collect { ... }` within a coroutine instead. You will avoid a local maxima, and grander vistas will stretch out before you.
(Avoid parameterless `collect()`, too --- but I'll leave the reasons why as an exercise for the reader.)
