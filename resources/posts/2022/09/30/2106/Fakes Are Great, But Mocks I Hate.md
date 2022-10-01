I learned how to write tests using mocking frameworks. All the tests I wrote at Instagram were written with heavy use of Mockito.
Since I've started at Cash App, though, I've learned the Cash App way, which is to avoid using mocking frameworks at all. Instead, if we're testing something that needs a `BiometricsStore`, for example, we create a new class and write a `FakeBiometricStore` implementation by hand.
This is called "faking".
I strongly prefer faking over mocking now.
Here's why.

## A Labor Saving Tool For An Easy Job

Mocks feel like they make writing tests easier, because they save me the labor of manually building a fake implementation. In practice, though, they make writing tests harder, because I have to write that labor-saving code for every single test I write.
That work just isn't worth saving! Writing the fake implementation in Kotlin is usually just as easy as writing the equivalent mocking code. So why bother?
Of course, writing code over and over isn't necessarily a bad thing in tests. But it is when that code has nothing to do with what you're testing. And that's the case here: I decide on an API for `BiometricsStore`, and then every time I test against it I have to simulate its behavior with the mocking framework. Once you start writing fakes instead, that work disappears.

## Easy To Mock Isn't Necessarily Good

Mocks also create "drunk under the lamppost" sorts of problems: situations where you write code in a particular way not because it's the right way, but because it's easy to mock.
Thin abstractions become particularly appealing. If you mock, `BiometricsStore` can't do anything interesting, because you have to program its mocked behavior from scratch every time you test against it. This encourages creating tissue thin abstractions that match your mocking framework's API, resulting in a proliferation of not-very-meaningful classes.

## Maintenance Drag

Having all this mocking code written over and over is a drag on maintenance, too. Changing the API for `BiometricsStore` means changing the mocking setup in every single test. This is a drag.
Another example we've run into on Cash Android is migrating over to coroutines; part of this work has required distributing good coroutines testing practices across the codebase.
We're in a good spot with this today. But if we weren't using fakes, I don't know how we would've gotten there. Mocking frameworks didn't (and still don't) support the approaches we found best.
Even if they did, the way that mocks repeatedly reimplement testing code would have left us without a foothold for pushing the whole codebase forward. We would have had to police testing approaches team by team, engineer by engineer, PR by PR, line by line. And that is a direct result of the practices mocking encourages.

## In Conclusion

* Burn mocks to the ground
* Run away
* Put on shades
* Get in car
* Drive away

