Here's a fun thing I learned today about `CoroutineScope` and `coroutineContext`. I'm not sure how I managed to avoid picking it up before, but I did!
I learned this the way I learn many things: by doing the wrong thing.

### An incorrect understanding of scopes

Here's a wrong thing I wrote:

        interface TestDispatcherScope : CoroutineScope, DelayController

        fun runBlockingWithTestDispatcher(
            block: suspend TestDispatcherScope.() -> Unit
        ) = runBlocking {
          val testCoroutineDispatcher = TestCoroutineDispatcher()
          object :
            CoroutineScope by (this + testCoroutineDispatcher),
            DelayController by testCoroutineDispatcher,
            TestDispatcherScope {
          }
            .block()
        }

This strange looking code is something I wrote as part of an effort to improve our testing tooling in coroutines. There are some behaviors we don't like in `runBlockingTest` at Cash App, so we thought it would be nice to have a function that gives you a `TestCoroutineDispatcher` like `runBlockingTest` does, but doesn't bring along all that behavior we don't like. So the above implementation lets you write a test like this:

        @Test
        fun test() = runBlockingWithTestDispatcher {
          launch {
            // do some test stuff
          }
        }

Within that block, your `this` is an instance of `TestDispatcherScope`, which allows you to call `CoroutineScope` methods like `launch`, and do `TestDispatcher` things like `runCurrent()`, `advanceTimeBy(...)`, and all that kind of thing.
All that work is done by existing implementations, so all we need to do is delegate the work out to other objects. That's what this odd anonymous object implementation is doing:

        object :
          CoroutineScope by (this + testCoroutineDispatcher),
          DelayController by testCoroutineDispatcher,
          TestDispatcherScope {
        }

This says, "Implement `CoroutineScope` by adding this `TestCoroutineDispatcher` to my existing `CoroutineScope` from `runBlocking`, and implement `DelayController` with that same `TestCoroutineDispatcher`. Oh, and then implement `TestDispatcherScope`, too, since it uses the same methods as those."
Pretty clever, eh? It seems to work great. My test passes:

        @Test
        fun works() = runBlockingWithTestDispatcher {
          val values = mutableListOf<Int>()
          launch(start = UNDISPATCHED) {
            values.add(0)
            delay(1_000)
            values.add(1)
          }
          assertThat(values).containsExactly(0)
          advanceTimeBy(1_000)
          assertThat(values).containsExactly(0, 1).inOrder()
        }

Unfortunately, my implementation is wrong and broken.

### Where Did My Dispatcher Go?

It's really easy to break, too! All you have to do is use the handy `coroutineScope` function.
If you're not familiar with it, `coroutineScope { ... }` runs a block of code, and provides a `CoroutineScope` within that block. The `coroutineScope` function will not exit until all of the launched coroutines in that scope have finished running (gracefully or not). In a structured concurrent style, constructs like this are how all `CoroutineScopes` are built, never by construction.
But it breaks this code. This is a failing test:

        @Test
        fun `nested coroutineScope`() = runBlockingWithTestDispatcher {
          coroutineScope {
            assertThat(coroutineContext[ContinuationInterceptor])
              .isInstanceOf(TestCoroutineDispatcher::class.java)
          }
        }

The `ContinuationInterceptor` is what makes your coroutines go: often, it will be some kind of Dispatcher, which will intercept your continuations and run them on its thread of choice. So this test is simply making sure that, within a `coroutineScope` block, the `ContinuationInterceptor` is actually the `TestCoroutineDispatcher` we need it to be.
But it's not! This test fails hard.
But why? Didn't I plug up the dispatcher to my `TestCoroutineScope` correctly? I mean, this test passes:

        @Test
        fun `nested coroutineScope`() = runBlockingWithTestDispatcher {
          assertThat(coroutineContext[ContinuationInterceptor])
            .isInstanceOf(TestCoroutineDispatcher::class.java)
        }

What is `coroutineScope` doing to get rid of my perfectly good `coroutineContext` with its perfectly cromulent `TestCoroutineDispatcher` wired up to the `ContinuationInterceptor`?

### There Are Two CoroutineContexts!

As it turns out, the explanation is not so baffling: I was thinking about things wrong, and my code was wrong. But my thinking was based on some misunderstandings that the documentation doesn't exactly knock you over the head with.
To wit, my fond friends: there are _two_ `coroutineContexts`!
What do I mean? Well, consider the following code:

        suspend fun whevs() {
          println("My context: $coroutineContext")
        }

This property, `coroutineContext`, represents the context within which the current coroutine is running. If you're in a "suspend context" (that is, within a block of code marked `suspend`), you'll have one because you can't run a suspend block without a `coroutineContext`.
Now, you'd think this would be the same thing:

        fun whevsBlocking() = runBlocking {
          println("My context: $coroutineContext")
        }

The block is also a suspend block, so there must be a coroutine context within which you're running. Naturally, one expects that context to be the value pointed to by the `coroutineContext` property.
But it's not necessarily. See, within `runBlocking` the `this` receiver is a `CoroutineScope`. And `CoroutineScope` has a property named `coroutineContext`. That's what `coroutineContext` is pointing to here, not the `coroutineContext` pointed to in the simple suspend context we used earlier. If you want to get the coroutine context being used to run the current suspend context, you need to call `currentCoroutineContext()` instead.
Like I said: There are two coroutine contexts. There's one that you're running in, and one for the `CoroutineScope` to launch its coroutines into.

### The Fix

So what's the fix?
Well, I can start by pointing at the smell I should've noticed in the original code: constructing my own `CoroutineScope`. I try to avoid constructing `CoroutineScope` by hand whenever possible. It's a little hidden, but that's effectively what I did by writing `CoroutineScope by (this + testDispatcher)` in the above implementation. That was a bad move.
So instead of trying to implement it the right way myself, I use an existing tool that I know will do it the right way:

        fun runBlockingWithTestDispatcher(
            block: suspend TestDispatcherScope.() -> Unit
        ) {
          val testCoroutineDispatcher = TestCoroutineDispatcher()
          runBlocking(testCoroutineDispatcher) {
            object :
              CoroutineScope by this,
              DelayController by testCoroutineDispatcher,
              TestDispatcherScope {
            }
              .block()
          }
        }

Since `runBlocking` already creates a `CoroutineScope` for me, I can just pass in the `TestCoroutineDispatcher` as the context and it will do the right thing for me. `withContext` can do something similar if you're already in a suspend context.

### Why/When Should You Care?

Honestly, most of the time you shouldn't. Unless you're dealing with tools written by rude people, your code is going to run in a context where `currentCoroutineContext()` points to the same value as `coroutineContext`. But sometimes it's worth caring:

* If you're writing those tools --- that is, code that calls a `CoroutineScope.() -> Unit` or something similar --- then it's important to avoid being rude! Pass the correct coroutine context into `withContext` or `runBlocking` to switch your coroutine over to the same coroutine context and create the `CoroutineScope` at the same time. And if you need to work at a lower level than that, maintain that invariant: always keep `currentCoroutineContext()` and `coroutineContext` pointing to the same value.
* If you're debugging, you absolutely want to know the ground truth of how these tools work. In a structured concurrent style, coroutine contexts tend to get passed down the call stack. I've already run into a few bugs that were caused by breaking that chain of custody. Looking at `currentCoroutineContext()[ContinuationInterceptor]` can be a valuable way of debugging those issues, but if you just look at `coroutineContext` it's easy to forget that it can be a moving target.
* Speaking of coroutine context chain of custody: avoid using `runBlocking` unless it's required, because it will break that chain of custody.
* Maybe you're a nerd and you just want to know how these things work.

And maybe there are other reasons, too, but now I'm done writing, so we'll never get to them.
