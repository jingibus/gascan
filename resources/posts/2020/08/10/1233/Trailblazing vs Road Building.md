I'm not usually a lurker. I like to find a stance that I think is God's own truth, and argue about it.
Experience has shown me that just because I like doing something doesn't mean that my friends and I will be happier for my having done it. And so it is that I find myself acting against type and lurking in our internal discussions about Contour.
What's Contour, you ask? It's a layout tool some of my colleagues are working on. You can find the project [here](https://github.com/cashapp/contour) on GitHub. It's not quite to a 1.0 release, but it is used by many of us internally. As I've become familiar with building layouts in code, Contour has been second only to Kotlin in making that a pleasant experience.
And while I didn't do much user interface work at my previous job, my background from prior to that left me with a few strong opinions. And, as much as I want to share them, I find myself holding my tongue.
Why? Because I want to listen!

### Trailblazer: Showing A Way

See, many of those opinions I have are as a consumer of the library. I want certain things to be easy; I want to write my code as I have always written it. I've got battle scars from using awful third party libraries that, had they been more restrictive, would have prevented me from writing the hacky workarounds that allowed me to ship.
And as a writer about Android APIs from back in the old days, I learned to take a certain approach to those APIs: take them as a given, try to build something clean and clear with them, and remove or refine API usages that aren't working. Rinse and repeat as needed until one clear recommended path presents itself.
This had some advantages: if you were making apps on the same scale as our experience, we could tell you with perfect clarity how to do it our way. If you weren't, though, you would come to the limits of our recommendations. At that point, you would need to explore the APIs for yourselves and find their hard edges.
Here's the short version of that perspective:

1. I want the path I explain to be clear and free of incidental complexity.
2. I want it to accomplish a clear objective for the person following me.
3. I want it to lead the follower to new vistas, not to unproductive blind alleys.

Let's call this the trailblazer's job. It will be done imperfectly, but it's worth doing. You make a pilot exploration, and show someone else how to do it your way. Hopefully, your way doesn't suck.

### Road Builder: Defining Safe Space

I've been rapt watching the discussion around Contour unfold because their job --- the library writer's job, the API designer's job, the wire protocol author's job --- is completely different from this. I've never done that job successfully, in spite of giving it a couple of tries, because every time I do it I go, "Okay, here is a clear path for how to get from point A to point B, and points C, D, and E through Z are all achievable. All done!"
Hah hah. No, not all done. What happens when the implementer doesn't follow the clear path? Who are they going to go to for help? You! You built the thing! Let's call your job the road builder.
So where the trailblazer only points where to go, the road builder constructs a system for general use. The road builder will have some concrete examples in mind like the trailblazer does, but they will not be so laser focused on them.
Instead, they will be thinking far more about support costs. After all, if it's not clear how to use the thing correctly, they'll come to someone for answers. As a user or as a trailblazer, I may be horrified and offended that my tools will prevent me from getting from point A to B in a way that I think is the clearest. But as a road builder, this is civilization itself: if you could get off the highway anywhere you like, you wouldn't have a highway.
And that's what's key about the perspective: when building roads, one is really defining a whole playing field. If one can use the tool you're building and fail to stay within the playing field you're defining, that's a bad thing. That's still your playing field, in spite of the fact that you didn't design it, because people built on it using your tool. If you change how it works, you impact them, and you end up being on the hook for supporting it.

### Situations For Each

Every shop will have circumstances that calls for each of these perspectives.
Trailblazing is excellent for learning new things rapidly. Projects are all about building new things, and so trailblazing can be a great tool for expediting projects.
I finished up a project this weekend the would have benefited from some trailblazing work: the collaboration on recording my arrangement of [How Can I Keep From Singing?](https://www.billjings.com/posts/title/how-can-i-keep-from-singing/) This was a small project, but we had a well defined protocol (sing along with the guide track in DropBox; drop work products in a "Tracks" folder). The project got bogged down because, in spite of the clear protocol, the whole space was an unknown for my collaborators. Providing them with a clear guide on how to get from point A to B would have been invaluable.
Road building I have done has been about shared infrastructure, about building a toolset that will be used in common. This work, often set aside in favor of project work, is critical because without good infrastructure to elevate it above technical details, cruft will grow up like thorns around your useful code. Pressure has to be lifted and space has to be made, because unlike trailblazing work I can say from experience that piloting out a working solution will not get you there.
And I think I have to end my thoughts there. I've got some battle scars in building tools for others, and those certainly count for something, but for now I think I'll continue to lurk.
