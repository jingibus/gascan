# gascan

Gas Can is a publishing tool I built to host my own work. It ingests MultiMarkdown emitted from Scrivener 3, and serves up static HTML. Everything hosted in Gas Can lives in resources, indexed in an edn file.

Gas Can serves two purposes:

* To publish writing and music and whatever else using my preferred workflow, presented in the way that I want it to be presented.
* To play around with tools I find interesting.

You can find more writing about the motivations on the site itself. At least, that's the plan.

## Installation

Clone this repo. 

## Running the server

To serve content, try this:

```
$ lein run run
```

## Importing new content

To import new content, you will first need to create some in Scrivener. See below for strictures on Scrivener composition and export; Scrivener is finicky, and so am I. 

Having exported to, say, `/Users/bphillips/Documents/Focus Loops In Perspective.md`, content is managed at the REPL. First convert it to a remote post:

```
user> (gascan.remote-posts/read-remote-post 
 "/Users/bphillips/Documents/Focus Loops In Perspective.md")
{:markdown-abs-path
 "/Users/bphillips/Documents/Focus Loops In Perspective.md",
 :title "Focus Loops In Perspective",
 :timestamp 1586541922953,
 :extra-resources [],
 :dir-depth 0,
 :parsed-markdown
 #object[com.vladsch.flexmark.util.ast.Document 0x4e5efc6d "Document{}"],
 :src-path "/Users/bphillips/Documents/Focus Loops In Perspective.md"}
```

Then intern it as a draft and add the interned post to the post store:

```
user> (gascan.posts/import-and-add-post! 
 (gascan.remote-posts/read-remote-post 
  "/Users/bphillips/Documents/Focus Loops In Perspective.md"))
...etc...
```

You can verify that it is there by using `gascan.browser` to run the server and automate a Chrome session to observe the post: (`chromedriver` is required):

```
user> (gascan.browser/look-at 
 (post->title-path (posts/find-post {:title "Focus Loops In Perspective"})))
```

The `gascan.posts` module has more tools for updating posts. Usually you will want to update the filter keys to pick one or more sections the post will appear in, and then update `:status` to `:published` to serve the post publicly.

## Writing and exporting in Scrivener

When writing in Scrivener:

* To link to a file from the local file system, create a local file link. This will emit a `file:///...` link in the exported Markdown.
* Change the title of the exported Markdown to whatever you want the title of the post to be.
* Use the "Emphasis" style for italics. (Remap Cmd-i to this menu item in Mac's Keyboard settings.)
* Create a new "Markdown Bold" character style from some bold formatted text. (Remap Cmd-b to this menu item in Mac's Keyboard settings.)
* Create a new export format for your project:
    1. Export project
    2. Check the box marked "Convert links to MultiMarkdown"
    3. Compile for "MultiMarkdown"
    4. Select "Basic MultiMarkdown" under "Scrivener Formats"
    5. Right click, "Duplicate & Edit Format"
    6. Editing the new format, make the following changes:
        * Under "Styles," 
            * Select "Code Block". Add a carriage return prefix by typing `Opt-Return` in the prefix/suffix area. (Nothing will appear, but it will work anyway.)
            * Select "Block Quote". Add a carriage return *suffix* by typing `Opt-Return` in the suffix area. (Same as above.)
            * Add "Emphasis". Add an underline for the prefix and suffix.
            * Add "Markdown Bold". Add an asterisk for the prefix and suffix.
            * Add "Heading 2". Add a prefix of carriage return, then "## ", and a suffix of a carriage return.

### Bugs

It's ugly, and the workflow with Scrivener is finicky.

## License

Copyright © 2020 Bill Phillips

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
