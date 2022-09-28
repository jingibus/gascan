A while back posed the following question to a group of Compose obsessives I participate in:

> Can anyone tell me how to get a value from a previous composition?

We had some fun with this little code golfing problem. It turns out that it is, in fact, possible. I liked my solution the best, and so that's the one I've remembered. Here it is:

        @Composable
        fun <R, T : R> previous(current: T, initial: R): R {
          val lastValue = remember { mutableStateOf(initial) }
          return remember(current) {
            val previous = lastValue.value
            lastValue.value = current
            previous
          }
        }

I like to bring this composable up every now and again, partly because it irritates the Jetpack Compose team (I hope Jim Sproch will forgive my delight when he replied "This will likely end very very poorly for you"). Probably for that reason I've settled on calling it The Unholy Composable.

## Why Is The Unholy Composable Unholy?

Good question. And, while I have an answer for it, I'd like to prove that the Unholy Composable is unholy by using it to open up a portal of evil.
First, what if we don't want to stop at the previous value? What about looking back 3, or even 4 or 5 values? 50 values?
No problem:

          @Composable
          fun <T : Any> nthPrevious(n: Int, current: T): T? = 
            nthPreviousInternal(n, current)

          @Composable
          tailrec fun <T : Any> nthPreviousInternal(
              n: Int, 
              current: T?,
          ): T? {
            return when (n) {
              0 -> current
              1 -> previous(current, null)
              else -> nthPreviousInternal(
                n - 1, 
                previous(current, null),
              )
            }
          }

What if I want a list of the last 4 values? The last 50 values?
Easily done:

        @Composable
        fun <T : Any> takeLast(n: Int, current: T): List<T> =
          (0 until n).map { i ->
            nthPrevious(i, current)
          }.takeWhile { it != null }
            .filterNotNull()

## Limits To The Unholy Composable

The Unholy Composable is not all-powerful. It can't magically look into the past: it has to `remember` previous compositions. So e.g. if you conditionally invoke `previous`, it won't always work:

          @Test
          fun previousIsntMagic(): Unit = runBlocking {
            val values = Channel<Pair<Int, Int?>>(1)
            var input by mutableStateOf(1)

            val job = launch {
              moleculeFlow(RecompositionClock.Immediate) {
                if (input > 1) input to previous(input, null)
                else input to null
              }.distinctUntilChanged().collect { values.send(it) }
            }
            assertEquals(1 to null, values.awaitValue())
            input = 2
            assertEquals(2 to null, values.awaitValue())
            job.cancel()
          }

For the same reason, you can't use the approach in `takeLast` to build a function that gives you the entire history of a value off of `previous` or `nthPrevious`:

          @Composable
          fun <T : Any> historyInternal(
              index: Int, 
              current: T,
          ): List<T> {
            val oldValue = nthPrevious(index, current)
            return when (oldValue) {
              null -> emptyList()
              else -> listOf(oldValue) + 
                historyInternal(index + 1, current)
            }
          }

          @Composable
          fun <T : Any> history(current: T): List<T> = 
            historyInternal(0, current)

This will fail to "record" new values the first time they are seen, because storage for those past values hasn't yet been allocated.
Of course, you can get around that...

        @Composable
        fun <T> history(current: T): List<T> {
          val history = remember { mutableListOf<T>() }
          return remember(current) {
            history.add(0, current)
            history.toList()
          }
        }

## A Difficult Badness

The reason I find the Unholy Composable so interesting is that, while it's immediate obvious to insiders that it is problematic, it's challenging to explain clearly why this is the case. For example, [here's how Jim Sproch tried to explain its badness](https://twitter.com/JimSproch/status/1573149118607888385):

> It does indeed move a value to a future composition, and that is by design, but it is intended/required that if a recomposition were to spontaneously occur at any given time, that the app's behavior would remain unchanged. Any code which fails this behavior code is violating API.

This is indeed an issue with `previous` (if I recompose fewer times, I will get different results), but it's an issue with lots of valid Composable code, too: recomposing fewer times may result in failure to run a `LaunchedEffect` promptly, or at all, which would also change the app's behavior. So I don't think that's the reason the unholy composable is so bad.
And yet he's right somehow: if you read these examples like `nthPrevious` and `history`, you can sense that something is going increasingly haywire. Their inefficiency is reason alone not to use them; but even if they were efficient, they would still be wrong.

## It's Bad Because It's Complex

Here's the truth as I see it:
There's nothing undefined or illegal about the unholy composable and its foul offspring. It does what it says it does, and it can be relied to work upon the way it works as long as Jetpack Compose is implemented correctly.
What it is is _pointlessly complicated_:

* It adds no additional power to Compose. Compose has a whole toolset for dealing with state over time: the snapshot system. You can even use `snapshotFlow` to turn snapshot state into a sequence of values! Why not just use that?
* It forces you to think about things that Compose is designed help you ignore.

Composables are pure functions of their inputs and snapshot state. As an outsider I can see as a general idea that this is the secret to the power of the whole system: by teasing state and the composition tree apart from one another, I can think much more clearly about _now_.
_Now_ is what the screen must show to the customer; now is what a Molecule-based presenter specifies when it emits a value.
_Now_ is a core requirement of interactive computer systems. When a person uses their phone, they need to see what it is right now, not what it was. Being able to clearly specify what happens now without thinking about the future or the past is why building UIs with Jetpack Compose is so fast.
This is also why the unholy composable is unholy: it opens the door again to the bad old days of the View system. Android Views are full of previouslies: every single variable in an Android View is a look at the past, until it's updated. The whole idea of recomposition was to make that impossible.

## Discipline Is Power

In programming, discipline is power. Discipline means being _capable_ of doing something, but _not_ doing it.
In this post, I have intentionally not explained how any of the code works. I've got my good reasons for that, but buried between them is a less good reason: I take a bit of pride in having figured this code out. It shows that I'm capable of building something that does something its creators did not intend.
But if I were to use this code in practice, I would add a completely unnecessary complexity. Rich Hickey calls this "complecting" - adding one more concept to our program.
Over time, repeated complections creates complexity, which saps us. Great programmers do have the ability to hold huge systems in their heads, but even they are limited at the end of the day by the complexity they create. Once it's too complex to understand and modify, they have to move on to something else to keep building.
Good disciplines remove options. They cut connections, they untangle webs. And if they're good disciplines, they do not reduce your capabilities one bit. So if you find a good discipline, it's worth holding firm to.

## One Recomposition At A Time

And that's the final takeaway here: all you really need to do is think about the current recomposition.
What happened last composition? Doesn't matter. Don't put it in state; you don't need to remember it. One recomposition at a time is enough.
And I think that's all I have to say about that.
