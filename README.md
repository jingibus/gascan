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
$ ./bin/gascan serve
```

You can also run the server directly through Leiningen:

```
$ lein run
```

## Running tests

Run the current automated test suite with:

```
$ lein test
```

## Importing new content

To import and publish new content, export it from Scrivener and point Gascan at the generated Markdown file:

```
$ ./bin/gascan publish "/path/to/Focus Loops In Perspective.md"
```

You can assign filter criteria during import:

```
$ ./bin/gascan publish --filter technical --filter music "/Users/bphillips/Documents/Focus Loops In Perspective.md"
```

You can also start the local server after import:

```
$ ./bin/gascan publish --serve 5000 "/Users/bphillips/Documents/Focus Loops In Perspective.md"
```

That command reads the source Markdown, copies the rendered post and linked resources into `resources/`, appends the post to `resources/metadata.edn`, and prints a short summary with the post URL.

You can do the same thing directly through Leiningen:

```
$ lein run publish "/path/to/Focus Loops In Perspective.md"
```

The older REPL functions are still available for manual maintenance and refresh workflows:

```
user> (gascan.posts/refresh-post! {:title "focus-loops-in-perspective"})
```

The `gascan.posts` module has more tools for updating posts. Usually you will want to update the filter keys to pick one or more sections the post will appear in.

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

Also, I really don't like the markdown dependency I'm using.

## License

Copyright © 2020 Bridget Phillips

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
