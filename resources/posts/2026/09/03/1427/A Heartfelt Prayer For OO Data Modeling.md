I’ve been working on something compiler-y in Kotlin, and I’ve run up against a super annoying data modeling problem around identifiers.

In our initial parse, we want to be forgiving. So we allow you to get away with a lot on your identifiers: `My-Identifier` is not valid, but we let that slide so that we can bubble up as many issues as possible.

Later on in the process, though, we can’t allow that — it could break our code generation. So at some point we have to stop compilation if we know any malformed identifiers are present. We also want some assurance from that point forth in our type system that we’ve only got the good ones, not the bad ones.

We can model this for consumers in Kotlin’s type system with interfaces like this:

```
/**
 * A raw identifier read from source
 */
interface IoIdentifier {
  val value: String
}

/**
 * A well-formed identifier that matches the spec exactly
 */
interface Identifier : IoIdentifier
```

Then we can define all the methods used in code generation against `Identifier`, not `IoIdentifier`, and everything else against the broader `IoIdentifer` interface. (These could also be value classes, but… one rant at a time)

Now, `Identifier` is also part of some other key identifier types that appear all of the place as building blocks. So we need to repeat the process there:

```
interface IoPackageName {
  val namespaces: List<IoIdentifier>
  val names: List<IoIdentifier>
}

interface PackageName : IoPackageName {
  override val namespaces: List<Identifier>
  override val names: List<Identifier>
}

interface IoServiceName {
  val packageName: IoPackageName
  val name: IoIdentifier
}

interface ServiceName {
  override val packageName: PackageName
  override val name: Identifier
}
```

If you ask me, so far this is all well and good. I might add some generics and cut some repetition, but that’d just be saving me some typing: this is the surface I want, the specification I want, the different kinds of things I need and will want to write code against.

Where things get icky is when it comes time to implement these interfaces. Because Kotlin, as an OO language, is built around the idea that instances of a particular class always satisfy the same interfaces.

This means that if I want to have some instances only satisfy `IoIdentifier` and other instances satisfy both `IoIdentifier` and `Identifier`, I actually need to have distinct implementations:

```
data class GoodIdentifier(override val name: String) : Identifier
data class BadIdentifier(override val name: String) : IoIdentifier
```

This is kind of unfortunate. These two things are the same class of thing in almost every way: they have identical equality semantics, comparability, I can lowercase them all the same way, etc. Some of that I can encode into the interface, but not all of it. So this distinction causes me more problems than it solves.

More importantly, as containers of data, these two data classes truly are identical. They just hold a string. I’m using distinct Kotlin classes because that’s the only way I can get the type system to do the work I need done; one could imagine a world where I could use `String` as the type and layer on the type information I need in some other way.

This problem gets repeated at every level we package things up in the type system, too. `ServiceName` and `PackageName` have to do the same thing:

```
data class BadPackageName(
  override val namespaces: List<IoIdentifier>,
  override val names: List<IoIdentifier>,
) : IoPackageName

data class GoodPackageName (
  override val namespaces: List<Identifier>,
  override val names: List<Identifier>,
) : PackageName

data class BadServiceName(
  override val packageName: IoPackageName,
  override val name: IoIdentifier,
) : IoServiceName

data class GoodServiceName(
  override val packageName: PackageName,
  override val name: Identifier,
) : ServiceName
```

Now instead of three container classes, we have six. And all because we wanted to distinguish between two types of strings! Thank god it wasn’t three, right?

And it’s really the same problem repeating itself at this layer, too. Let’s say we performed the following runtime check on an instance of `BadPackageName`:

```
if (with(badPackageName) { 
  namespaces.any { it !is Identifier } ||
  names.any { it !is Identifier }
}) {
  error(“I’m bad!”)
}
```

That would mean that it would be perfectly safe to use `badPackageName` as an instance of `PackageName`, right? Well, yeah, but the only way you can actually get Kotlin to know this is by constructing a new instance - of `GoodPackageName`.

I don’t really mean to malign Kotlin with this observation — the amount of pain I have experienced in my entire career in mobile engineering from this is so, so tiny. Many problem domains just don’t do much heavy lifting with data. And I also don’t want to pretend this is an original observation — Clojureland has considered and solved for this exact kind of problem.

But where pure data problems lurk, this ends up being kind of a big deal. And you don’t have to look too far from the client engineering boat to find some pretty big data fish: problems like wire protocols and persistent data. In these spaces, versioning gremlins guarantee that the pile of data we receive cannot be absolutely guaranteed to satisfy the strict type constraints we are in the habit of statically defining on our data containers. If we could magically layer on further constraints onto the data, that would be incredibly handy!

And I think that’s all I have to say about that.
