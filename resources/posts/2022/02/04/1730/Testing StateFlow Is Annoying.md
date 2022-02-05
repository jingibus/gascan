Let's get into some background. I've been working on Cash App presenters. A Cash App presenter is, at its heart, a unidirectional dataflow component with (conceptually) the following API:

        fun models(events: Flow<UiEvent>): StateFlow<UiModel>

It's a function that translates a temporal sequence of edge-triggered events into a temporal sequence of unitary states. Both the events and the state are temporal: the events because they represent interactions with the customer (which are unique entities the arrive over time; you can't deduplicate them, and ignoring them will create a bug), and the state because it is responding to the events. Good implementations of Android MVVM architecture do something similar to this, but with a more complicated API (because Android MVVM is worse, don't @ me).
Now, I said that _conceptually_ this is their API. That's because `StateFlow` matches how we think about the `UiModel`: while it changes over time (like a `Flow`), you don't actually need to know the whole sequences of `UiModel` instances emitted to have a usable view of the state represented by the `UiModel`. The `UiModel` is _unitary_: you can understand the whole UI state by looking at just one instance of this object.
So `StateFlow` is a nice API for this! If you need to render the view right now, you can say "Hey `StateFlow`, tell me your state right now." And if you need to render that state over time, then you call `collect` on it and kapow, you're rendering new states as fast as you can get them. Fantastic.
But there's a problem: validation.

### Validation Versus Runtime

Validation requires a different API than runtime. To see why, consider a simple screen:

        data class PaymentViewModel(
          val isLoading: Boolean,
          val isCompleted: Boolean,
          val paymentAmount: Money,
        )
        sealed class PaymentEvent {
          object SubmitTap : PaymentEvent()
        }

This model displays a payment that the customer is attempting to make. When the customer taps the submit button, a payment call should be made to the server. While that call is running, we show a progress spinner, and when it's done we show that the payment is complete.
Here's a Molecule-style implementation that illustrates the business logic:

        @Composable
        fun models(events: Flow<PaymentEvent>) {
          val submitTapped by remember {
            mutableStateOf(false)
          }
          val paymentComplete by remember {
            mutableStateOf(false)
          }
          LaunchedEffect(events) {
            events.collect { event ->
              when (event) {
                is SubmitTap -> submitTapped = true
              }
            }
          }
          if (submitTapped && !paymentComplete) {
            LaunchedEffect(Unit) {
              appService.completePayment(args.payment)
              paymentComplete = true
            }
          }
          return PaymentViewModel(
            isLoading = submitTapped && !paymentComplete,
            isCompleted = paymentComplete,
            paymentAmount = args.payment.amount,
          )
        }

And here's the validation I would write for this behavior:

        @Test
        fun validateFlow() = runBlocking {
          val events = MutableSharedFlow<PaymentEvent>(
            extraBufferCapacity = 50)
          val amount = Money(50)
          val fakeService = FakeAppService()
          val presenter = Presenter(
            Args(payment = Payment(amount)),
            fakeService,
          )

          launchMolecule { presenter.models(events) }.test {
            assertThat(awaitItem()).isEqualTo(
              PaymentViewModel(false, false, amount))

            events.tryEmit(SubmitTap)

            assertThat(awaitItem()).isEqualTo(
              PaymentViewModel(true, false, amount))

            // Responses are modeled with a Channel
            fakeService.completePaymentResponses
              .trySend(CompletePaymentResponse())

            assertThat(awaitItem()).isEqualTo(
              PaymentViewModel(true, true, amount))
          }
        }

(Caveat emptor: the above code is batteries-not-included. We have some code that does what launchMolecule does here, though, so --- it's not completely crazy.)
All looks great, right? Well.... no, not really. Because I can write my test a little differently and observe a _different number of distinct UI models_:

        assertThat(awaitItem()).isEqualTo(
          PaymentViewModel(false, false, amount))
        fakeService.completePaymentResponses
          .trySend(CompletePaymentResponse())

        events.tryEmit(SubmitTap)

        yield()
        assertThat(awaitItem()).isEqualTo(
          PaymentViewModel(true, true, amount))

By populating `fakeService` with the response ahead of time, the presenter has the opportunity to race ahead and emit the final UI model. That will _overwrite_ the second UI model in a `StateFlow`, causing it to never be seen!
This example isn't realistic (it includes a call to `yield()`, which I avoid unless I'm trapped and need to chew my own leg off), but this problem is not imaginary. You can introduce a bug that emits extra UI models that cause the UI to stutter and glitch, and a validation written against a `StateFlow` will never catch it.

### Validate Against Flow, Not StateFlow

Because of this, it's not safe to write validation code against `StateFlow`. It can always sneak in extra values without you knowing about it, values that could still cause issues at runtime. If at all possible, `StateFlow` should be used as an implementation tool and not as the public API that you write validation code against.
This truth drives me nuts when I'm making API decisions, but even so, I'm happier being driven nuts by reality than being driven nuts trying to use a tool to do things it won't do.

### Testing With Concurrency

If you've got this far, you might be thinking, _"hmm."_
Because what makes `StateFlow` so special, right? We have databases: a database can "sneak in" an extra value all it likes. And we write tests against databases and other stateful things all the time, right?
That's true, yes, but every test we ever wrote against a database was imperative. It said, "Do this. Next, do that. Now do this next thing." Even RxJava tests were imperative: plug up a few trampoline schedulers, and every single input to the whole mess of `Observables` waits until the Rx pachinko machine is done running before continuing.
This isn't the case in coroutines. When testing a flow, an independent coroutine has to run that flow concurrently to your own testing logic. That means that it's impossible to tell how much code the flow under test has run. You are not in charge of that; the dispatcher is, and there's no mental machine model you can internalize that will enable you to write clean and clear tests that drive that dispatcher by hand. You will get inscrutable race conditions that you solve by throwing in an arbitrary number of `yield()` or `runCurrent()` calls.
Now, if your test code _is_ setup so that you are [communicating](https://go.dev/blog/codelab-share) with the sharing coroutine before sharing state state and know _exactly_ what it is doing, then you're sitting pretty. And good on you. For that reason, if you must test a `StateFlow` it is better to use its `value` field. That at least makes it clear that you are being unsafe.

### Yet Another Reason To Love Compose Logic

This is another reason why I am in love with logic written in Compose. Because when you get right down to it, the `Flow` and `StateFlow` APIs ask you to represent your implementation in two different ways: `Flow` always forces you to `suspend` for the next value, while `StateFlow` expects to always have it immediately and have some process running producing the next value.
Compose logic lets you write your logic in one form (a function with the signature `@Compose (events: Flow<UiEvent>)->UiModel`), but interpret in either way, depending on your scenario. If you need a `Flow` under test, use `moleculeFlow` or another tool. If you need a `StateFlow`, use `launchMolecule`. And if you're in Jetpack Compose, you don't have to create any kind of `Flow`: just invoke the function and use the value that comes out. Simple.
