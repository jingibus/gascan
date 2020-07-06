I've been mulling over a few things lately. I don't know that I have something concrete to mull them towards, but I think it's time to put pen to paper.
Let's start with data polymorphism. The following concepts are all stolen from or inspired by Clojure, but have pachinkoed around my own brain for enough months that I'm sure I've made my own mistakes with them. Clojure's `spec` library implements a lot of what I describe below as a "field-oriented view" in a more generic and powerful way.

### The humble record

Let's consider a record of data. A record has fields. Each field has a name, and a value. So we could make a record `person1` with fields and values `name: "Fred Willard"`, `birthday: September 18, 1933`, and `favorite color: Green`
Do the values have types? Yep, they definitely do. I've written out the birthday and color as a string, but they would be much more useful represented as a typed value that we can use to perform other operations.

### Types of records: an object view

Does the record have a type? Are all records fine to pass in to all places?
Say I want to write a function that colors the calendar cell for a person's birthday with their favorite color:

        def colorBirthday(cells: CalendarCells, record: Record): 
          ...

If I define the type as a generic record, I'll have to manually check the value within this code. As a caller, I don't have any idea what sort of value I need to pass in to satisfy this function, either. Not good.
How could we define the type? One way would be an object-y way of doing it: define a type of record called `Person`, and use `person1` as our model: a `Person` has a `name` field that is a string, a `birthday` field that is a date, and a `favorite color` field that is a color. All of these can be implemented in a POJO fashion, with getters and setters.
Now I can declare my function like so:

        `def colorBirthday(cells: CalendarCells, person: Person):` 
          `...` 

...and I can be sure I'll get what I need to paint this person's birthday cell their favorite color.
Unfortunately, this overspecifies our type. `colorBirthday` doesn't actually need the `name` field from `Person`. Due to the way our type works, though, the name has to come along for the ride.

### Types of records: a field-oriented view

Could we do better?
Instead of defining the type as an explicit set of required fields with names, we could instead assign typing to the fields. So we would define the name `name` typed as a string, `birthday` typed as a date, and `favorite color` typed as a color. (And we'll namespace all these names, for sanity's sake.)
That means that the concrete record value is a collection of fields that each have their own types. The record itself doesn't have a type; it is merely a collection of fields that do.
Now we can define the original `Person` type above as an aggregation. `Person = {name, birthday, favorite color}`. It is no longer tied to any specific record, but we can look at any record and say whether it is a `Person` or not by examining what fields it has. And if we want to use that to validate information coming from a database or the network or who knows where, or to validate our code, we have it.
But for `colorBirthday`, we could define a separate type referring to the same field names and it would interoperate cleanly: `BirthdayColoring = {birthday, favorite color`}.
And what's even better than that, is that the name no longer has any meaning. Since the field names are namespaced and fully disambiguated, the aggregation `{birthday, favorite color}` is enough to fully specify the type of the data. The name `BirthdayColoring` is only a convenience for the programmer.

### Nullability on field types

Let's go back to our original record and its fields and values. What about nullability? Are the types of the field values nullable, or not?
For our original record, nullability isn't helpful or descriptive. Does it have a nullable phone number field? Sure! It has a nullability social security field, a nullable geotag field, a nullable religion field, and a nullable atmospheric waystation locator id field. Name a field this record doesn't (or does!) have, and that's a nullable field.
Nullability doesn't seem that useful for fields when considered on their own, then. It doesn't add anything to our specification for `birthday` to say that it's nullable, because you can always omit the birthday field, whether it's nullable or not.

### Nullability on object-oriented record types

When discussing the type of the record, nullability does start to have a meaning.
In our object type system, we could write "A `Person` is guaranteed to have a name, birthday, and favorite color, but it may or may not have a phone number." The phone number is nullable.
This is how all object-oriented record types that I have used work. Why do they work that way?
There's an argument that it's useful in a couple of ways. For one, the nullable field value helps programmers preserve polymorphism between values that differ in small ways, but belong in the same slots. Without a nullable phone number field, I'd have to define a separate `PersonWithPhoneNumber` type.
For another, the nullable field serves as documentation. Even if it doesn't guarantee presence of a value, it tells the reader what values to look for.
There's another reason that's a bit stronger, though: it has to work this way. A concrete object type representing a record in the typical POJO fashion cannot expose any data outside its declared interface. So if the record has a phone number, there are two choices: either have a nullable phone number field, or drop it on the floor.
And that's unfortunate, because nullable fields are imperfect tools from a type perspective. My code may require that the `Person` has a non-null phone number field. The only remedy for that is to create another type.

### Nullability on the field-oriented view

What about the field-oriented view of a `Person`?
Unlike the object representation, our aggregate `{name, birthday, favorite color}` type only makes positive claims about data in the record. Any record anywhere might hold a `phone number`, and since that field description is typed we know what it will contain if it is there. So as a matter of compile-time verification, something like `{name, birthday, favorite color, phone number?}` wins us nothing.
What about the two examples of the utility of nullability in the object context?  One of them falls right out: to preserve polymorphism, we don't need nullability at all. A `Person` is polymorphic with a `{name, birthday, favorite color, phone number}` for any site that doesn't need the phone number, and no nullability notion is required.
What about documentation? That we still need: a method might not strictly require a phone number, but it nevertheless may be part of the API. This is similar to how we used the name `Person` above: technically it doesn't add anything to the validation scheme, but it would be invaluable for communication purposes to be able to say `Person{name, birthday, favorite color, phone number?}`.

### What's the point of all this?

I had thought this would be a waypoint towards some other topics on my mind: representations of data on the wire or in memory vs. the data APIs we use in code, the relationship between wire protocol languages and the data languages we use in code, and persistent data representation on the client. It seems clear that I have some more work to do before I get to any of those topics, though, because this is quite enough already.
I do want to add one string to pull regarding what the point is, though, which is what this tells us about how we represent our data.
Most serde libraries I have used abide strongly by the object representation model. This is beneficial because the object representation gives me two things: an API against the data I can use in code, and containers to stuff the data into and out of. For a tool like protobufs, that container is sufficiently specified that I even get an explicit field order to pack it into and out of.
A serde tool wins a lot by specifying a clear and clean object representation to pack values into. But this implicitly ties the data language closely to a concrete specification of the data, including specific holes for the data to fit into.
But what happens if this concrete representation starts to get crufty? What if it accumulates layers of legacy? Does it continue to make sense to stuff our data into these containers if they no longer match what our application needs? And if our application needs something different, what would that data description look like, and how would we map it onto the concrete data descriptions given to us by a JSON or protobuf format?
There's a lot of handwaving in the above, and no doubt I've gone astray somewhere, but I think that hews close enough to the line of my idle speculations for me to call an end to this post.
