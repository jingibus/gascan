I've always felt guilty about about the fact that I don't have much stomach for a lot of technical writing. I read reference material, sure, and I'll read a deep dive or a good tutorial if I'm sufficiently motivated, but that's about it. Particularly when it comes to material about architecture --- MVVM, MVC, MVI, what have you ---  I can't summon the patience.
But I like Views! And I have properties that I like in a view. So maybe that's a safe place to put down some thoughts.

### Standalone

I like views that stand on their own. I would like to be able to fire up a small application, add the module with my view in it as a dependency, and throw it into an Activity with almost no ceremony:

        class MyActivity : AppCompatActivity() {
          override fun onCreate(savedInstanceState: Bundle) {
            super.onCreate(savedInstanceState)
            setContentView(ThermosiphonView())
          }
        }

My `ThermosiphonView` displays a thermosiphon, but it doesn't require me to implement anything --- not even a callback API. All I have to do is throw it onto an `Activity`, and I can see it.
The reason I like a view to be standalone is that it focuses my efforts on a giant piece of the system: rendering information to the screen. So much effort has been spent by so many talented developers over the years on making simple APIs like Android XML layout files that can be easy to forget that this is challenging work that will make or break the success of your app. Being able to focus on it and provide coding space in this problem area will pay off.
So this is a good baseline to have: create a component that can draw something useful to the screen. If your goal is to make things move around, it should do that work, too. By itself, that is enough to make a useful component.

### Unitary External State

I like views that display what I tell them to display. But I'm going to be honest: I don't like having to remember what I told them in the past. That's why I'm a big fan of unitary state for my views.
By unitary state, I simply mean that all the state is together in one place, and that every time the view renders itself it looks at *all* of the state. This makes the view less complicated to interact with, because it reduces the amount of state lying around: the state in the widgets is entirely dependent on the unitary state, and thus will not accumulate state detritus and can be ignored.
It is possible to do this by using ivars: store the information about the state of the view in internal ivars, and then write a single method (I used to call it `updateUI`) that updates the widgets to reflect their current state.
That doesn't go quite far enough, though, because it's still a hassle to remember what I told the view: I have a whole set of invocations to make to get the view's ivars just so. It is far simpler to take all that state and move it onto an order ticket: a sheet of paper that I can hand to the view and say, "Hey, display this!"
Now my memory is even simpler: if I want to know what the view should display right now, I look at one (and exactly one!) order ticket.
Unitary external state makes the view more straightforward to tinker with because it makes it easier to put it through its paces. The inputs and outputs are clarified: the input is the order ticket, and the output is the view displayed on the screen. Using a modern type system for the order ticket defines the space of valid order tickets, too, and ensures that all required items are filled in.
At Square, we call those order tickets View Models. They're generally data classes. We call the method that accepts an order ticket `render`.

### Unitary External State Tensions

Any fixed idea about how to build software is probably wrong. Where does this idea start to break?
What makes this idea work well is that the relationship between the ticket and the view is straightforward: the ticket tells you what's on the view in a usable language. It stops working well when the view's job doesn't cooperate with that simplification.
One way that can happen is when the view itself is changing over time. Say that the view animates to a selected item when it receives a new view model. Should it repeat the animation if you send it a second view model with the same selection? Well, maybe it should, and maybe it shouldn't. The issue can be fixed by adding more modeling (say, by adding a timestamp to the selection), but that is annoying and makes it more annoying to put the view through its paces.
Even more extreme is an example I recently dealt with: Android's WebView. WebView by its nature has a lot of internal state: you tell it to load an URL and it will go make a web request, maybe show a progress bar, or show an error screen. When I applied the idea of "the view displays what I write on the ticket," WebView just laughed at me.
So what did I do? I never achieved a happy purity. I drove it with objects I called View Models, but they were not unitary state like I describe above. I could have invented a novel new perfect name that only applied in exactly that situation, but I decided that the better part of valor was to abandon perfection. It was and is reasonably easy to tinker with, though, so I suppose I'm happy with that.

### Standalone Tensions

The same goes for "standalone-ness". Here are two edges for that idea I've encountered recently:
The first is composite views. By that, I mean views that are composed of other views. Under normal circumstances, this is no big deal, but if the other view that you're composing into your view has business logic associated with it, you can be forced to duplicate code that you'd rather not duplicate. Or even composite business logic from different screens together, which sounds annoying.
The solution I went with was to provide these views as dependencies through a factory. Then in my "standalone" solution I can provide a subview with an unexciting look, and in the real app I can wire up the subview to its business code. It requires a bit of dependency creation, but what can you do? You're compositing things together now, so that's the world you're living in.
The other is tools that are just extremely view-y, and have absolutely nothing to do with any kind of business logic at all. Image loaders are the tool I ran into that fit this description: Picasso in our case, but any one will do.
The problem is that if you leave the image loader dependency out entirely, it leaves your view useless. It thwarts the goal of providing utility: why bother going to all the trouble of separating all this stuff into a "clean" design if I end up with a view that can't easily display remote content? That defies tinkerability, too: it should be easy to fiddle with my view and try to break it with different images I have lying around. Adding a compile loop just to display a different image is no good.
So that's another one that I think I've convinced myself to break this rule on: Android views that display remote images just aren't useful without an image loader, even when all the business logic is stripped out. So I'm leaving the image loader in.
