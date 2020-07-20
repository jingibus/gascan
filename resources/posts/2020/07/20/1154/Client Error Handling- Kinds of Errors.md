I've got client error handling on the mind. It's a tough thing, too, because it's a classic cross-cutting concern: almost every screen in the app will have to deal with error handling in one way or another. Organizationally, too, it cuts across disciplines: design, server, and client all have to have their say in the solution. None of these parties will have the appetite to spend much time building out the perfect error handling solution, either: they want something that works well enough, and is straightforward to implement.
Let's limit things by strictly saying that this is about errors that are related to communications with the server. What is there to consider? What categories can we divide errors by?

### Communication Errors

As a systems matter, errors in communicating to the server are categorically different from errors that are communicated to the client by the server. If the client is able to open a connection, the server will tell it...something! Perhaps that something should be communicated to the user. Perhaps it shouldn't be! That's a design matter.
But connection related errors are worth calling out as a unique category because they leave the client totally responsible: whether it's a message to be shown or a behavior to be triggered, the server has no say.

### Blame: Client, Server, Network, User

If we have a specific signal about an error that has occurred, that signal will originate from a specific place, and it will tell us something about what caused the error. Maybe not a useful something, but something.
The HTTP portion of the stack is explicit about this: 4xx errors are all categorized as client errors, and 5xx are all server errors. That doesn't always mean that the error provided is informative or useful, but it can at least tell us that we did something wrong on the client.
Sometimes the network isn't working for a variety of reasons. And of course sometimes the system would work perfectly, if only the user would type in the correct password.
It's also possible that the server may send the client something that we don't understand. Whose fault is that?

### Recoverability

Can we recover from the error? If so, can we automatically recover, or do we require user intervention?
Some cases are clear: if the device is on airplane mode, flipping that switch may resolve the issue. Other network issues may be a matter of poor location.
Others are not so clear. The server may be overloaded, and the request will succeed if we try it again in a moment. How many clients are asking this question, though? Could we have a thundering herd problem, where all of our clients retry at the same time?
Recoverability is related to the matter of blame, but there's always interpretation required. For example, in the strict sense the server may blame an error on a malformed payload from the client. In reality, though, it may be that the error can only be recovered by upgrading the client, which is the user's responsibility.

### Recoverability and User Experience

This last case is unlike the other categories above. I can write down communication errors and a few categories of blame, and they'll apply to pretty much any system I work with for the rest of my career.
Recoverability, though, will always be knit hand in hand with the way the application is designed. Recoverability mechanisms aren't handed to us: they are built. The request may be dropped; it may be kicked over to a pending queue that the user can manage themselves; it may be thrown into an automatic retry system that is informed by the error context; it may present a generic message the user and then allow them to proceed through the flow again, and so on and so forth.
What is generally true about whatever design is built is that it will almost always be a system with a high fan-in: it will be built once, and then be used repeatedly throughout the system in similar ways. If not, then we'll never achieve the goal of no spending too much time on this problem that no one wants to think about.
And while I could go on about that topic, I think I've written just about enough, so I will not.
