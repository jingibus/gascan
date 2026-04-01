# Gascan Architecture Report

## Overview

Gascan is a small Clojure publishing system for a personal website. It has two main responsibilities:

1. Import content exported from Scrivener as MultiMarkdown into the repository's `resources/` tree.
2. Serve that interned content as HTML, RSS, images, and static assets through a Ring/Jetty web app.

The system is intentionally simple. There is no database, admin UI, background job system, or separate build pipeline. Repository files under `resources/` are the source of truth for published content, and the application keeps an in-memory view of that content while it runs.

## High-Level Shape

The codebase splits into four major areas:

- Runtime and HTTP serving
  - `src/gascan/core.clj`
  - `src/gascan/server.clj`
  - `src/gascan/routing.clj`
  - `src/gascan/session.clj`
- Content storage and import
  - `src/gascan/posts.clj`
  - `src/gascan/remote_posts.clj`
  - `src/gascan/intern.clj`
  - `src/gascan/post_spec.clj`
- Markdown parsing and AST transformation
  - `src/gascan/flexmark.clj`
  - `src/gascan/multimarkdown.clj`
  - `src/gascan/intern_markdown.clj`
  - `src/gascan/ast.clj`
  - `src/gascan/multi_to_intern_markdown.clj`
- Presentation
  - `src/gascan/index_view.clj`
  - `src/gascan/posts_view.clj`
  - `src/gascan/post_view.clj`
  - `src/gascan/what_it_is_view.clj`
  - `src/gascan/template.clj`
  - `src/gascan/images.clj`

There are also developer-facing helpers:

- `src/gascan/browser.clj` for opening pages in Chrome through Etaoin
- `src/gascan/debug.clj` for tracing and verbose instrumentation
- `samples/` and `test/` for example content and focused transformation tests

## Runtime Architecture

### Process Model

The entrypoint is `gascan.core/-main`, which resolves a port from CLI args or environment and starts Jetty with a public session.

`gascan.server/run` builds a Ring handler stack around Compojure routes:

- Route dispatch happens in `all-routes`
- Middleware wraps resource serving, content typing, image resizing, and conditional `304 Not Modified` support for public sessions
- The app serves dynamic HTML and RSS alongside static resources from `resources/`

The deployment story is minimal:

- `Procfile` runs the produced uberjar and invokes `gascan.core`
- `project.clj` defines a Leiningen app with Ring, Compojure, Hiccup, Flexmark, Etaoin, and image-resizer dependencies

### Sessions and Visibility

The session model is deliberately tiny. A session is just `{:public boolean}`.

This flag controls which posts are visible:

- Public sessions show only `:published` posts in indexes
- Individual post rendering allows `:soft-published` posts in public mode
- Private sessions effectively expose all posts for author use

That makes "preview vs public" a runtime concern rather than a separate environment or content store.

## Content Model and Storage

### Source of Truth

Published content lives under `resources/`:

- `resources/metadata.edn` contains the post catalog
- `resources/posts/...` contains interned markdown files and copied assets
- `resources/images/...` and other bundled assets support general site content

There is no database. `metadata.edn` is the canonical index.

### Post Record Shape

`gascan.post-spec` defines two important shapes:

- Remote post
  - content outside the repo, usually a Scrivener export
- Interned post
  - content copied into `resources/` and tracked by metadata

Interned posts include:

- `:id`
- `:title`
- `:timestamp`
- `:markdown-rel-path`
- `:extra-resources-rel`
- `:filter`
- `:status`
- optional `:parsed-markdown`

The metadata file currently also carries `:src-path` for refresh workflows, even though that field is not part of the declared spec. In practice, the code depends on it for re-importing existing posts.

### In-Memory State

`gascan.posts` lazily loads `metadata.edn` into a global `posts-lazy` var. Reads go through `posts/posts`, and writes happen by rewriting `resources/metadata.edn` and then resetting the lazy cache.

This is simple and effective for a personal site, but it means:

- application state is process-local
- writes are whole-file rewrites
- there is no concurrency control
- fresh imports or metadata edits require running through these helpers to keep memory in sync

## Import Pipeline

The import flow is the most domain-specific part of the system.

### Input Assumptions

`README.md` and `remote_posts.clj` show the expected authoring model:

- Content is written in Scrivener
- Scrivener exports MultiMarkdown
- A post may be either:
  - a single `.md` file
  - a directory containing a `.md` file plus linked resources

### Import Steps

The main workflow is:

1. `gascan.remote-posts/read-remote-post`
   - reads a file or directory export
   - parses it with Flexmark in MultiMarkdown emulation mode
   - extracts title and resource list
2. `gascan.posts/import-post!`
   - transforms parsed markdown into an interned form
   - rewrites file links to local relative resource names
   - strips the title block from the rendered body
   - copies markdown and extra files into `resources/posts/YYYY/MM/DD/HHmm/...`
   - creates an interned metadata record with a new UUID and `:published` status
3. `gascan.posts/import-and-add-post!`
   - appends the new post to `metadata.edn`

### Resource Interning

`gascan.intern` handles file and EDN persistence:

- file copy destination is computed from a relative destination plus source path depth
- interned files are always written under `resources/`
- metadata is pretty-printed back to EDN

This turns external authoring artifacts into repository-managed assets that the site can serve directly.

## Markdown and AST Transformation Pipeline

The rendering pipeline is more sophisticated than the overall app shape because it compensates for Scrivener/MultiMarkdown quirks.

### Parser Stack

There are effectively two markdown dialects in play:

- `gascan.multimarkdown`
  - Flexmark configured for `MULTI_MARKDOWN`
  - used for imported author content
- `gascan.intern-markdown`
  - Flexmark configured closer to CommonMark plus attributes extension
  - used after Gascan normalizes content into a render-friendly form

### AST as an Editable Intermediate Representation

`gascan.ast` converts Flexmark nodes into a nested "scaffold AST" so the code can use Clojure zipper operations to mutate documents safely enough and then "restitch" them back into Flexmark nodes.

That AST layer supports the key transformation strategy in the app:

- parse Flexmark document
- convert to scaffold AST
- edit tree structure and individual nodes
- reconstruct Flexmark document
- render to HTML or markdown

### Notable Transformations

Important transformations include:

- `posts/strip-title-section`
  - removes Scrivener-style title metadata from the rendered article body
- `posts/translate-links!`
  - turns `file:///...` links into local resource references
- `multi_to_intern_markdown/multimarkdown->internmarkdown`
  - rewrites MultiMarkdown image-reference syntax with inline dimension metadata into a form the CommonMark-plus-attributes parser can understand
- `ast/split-line-breaks`
  - converts line breaks into paragraph boundaries outside block quotes, while preserving block quote behavior with `<br>`
- `post_view/extract-audio-links` and `apply-audio-links`
  - detects links to `.mp3` and `.wav` assets and injects HTML audio controls
- `post_view/apply-simple-text-replacements`
  - normalizes `--` and `---` into en/em dashes during rendering

Overall, Gascan treats markdown less as plain text and more as a source format that needs structural normalization before final HTML output.

## Request/Response Flow

### Main Routes

`gascan.server/all-routes` exposes the whole application:

- `/`, `/index.html`, `/index.htm`
  - index page with featured latest post per filter
- `/posts`
  - all posts index
- `/posts/criteria/<criteria>`
  - filtered posts index
- `/posts/title/<slug>/`
  - post by title slug
- `/posts/id/<uuid>/`
  - post by ID
- resource routes under both title and ID paths
  - serve interned extra assets for a post
- `/rss`
  - RSS feed for visible posts
- `/what-it-is`
  - static/about-style content
- `/favicon.ico`
  - bundled favicon

Unknown routes return a custom 404 page.

### Page Rendering

The rendering flow for a post is:

1. Find the post from the in-memory catalog
2. Check session visibility
3. Read the interned markdown file from `resources/`
4. Parse and transform the AST
5. Render HTML
6. Wrap it in a site template with date and up-link navigation

Indexes and RSS are generated dynamically from the same in-memory metadata list.

## Presentation Layer

The UI is intentionally handcrafted and server-rendered:

- HTML is generated with Hiccup
- layout is produced by `template/enframe`
- styling is inline or embedded in the page
- Google Fonts are loaded directly from the template

There is no client-side application framework. The only meaningful client-side script is the randomized redirect behavior in `what_it_is_view.clj`.

The image path layer has a special capability:

- `images/wrap-image-scale` intercepts image responses
- a `?width=` query parameter triggers server-side resizing
- `post_view` and `what_it_is_view` both rely on this to keep image payloads manageable

## Supporting Tooling and Tests

### Tests

The test suite is focused more on content-transformation correctness than on end-to-end HTTP behavior. Current tests cover:

- title extraction from MultiMarkdown
- AST scaffold/restitch round-tripping
- title-section stripping
- line-break transformation behavior
- audio-link enhancement behavior

This aligns with where the architectural complexity actually is: AST manipulation and markdown normalization.

### Manual Author Tooling

`gascan.browser` provides a lightweight preview helper by launching Chrome and navigating to the locally running server. This appears intended for REPL-driven author workflows.

The codebase contains many `comment` blocks with REPL recipes, which function as living operator notes for importing, refreshing, publishing, and previewing posts.

## Architectural Characteristics

### Strengths

- Very small deployment surface area
- No external persistence dependency
- Author workflow is tailored to the actual publishing process
- Content and assets are fully interned into the repo for durable hosting
- Rendering behavior is explicit and inspectable in code

### Constraints and Risks

- Global mutable state via vars and lazy caches makes the runtime simple but not robust for concurrent writers
- Metadata writes are full rewrites with no locking
- The post spec and actual metadata shape have drifted (`:src-path` is used but not modeled)
- Much of the markdown pipeline depends on AST surgery against Flexmark internals, which is powerful but brittle
- Rendering logic mixes structural transformation with HTML generation, making some behaviors hard to change independently
- There is little separation between "library code" and "REPL operator workflow"
- Tests do not cover route-level integration, metadata mutation scenarios, or failure cases around file IO

## Practical Mental Model

The simplest accurate way to think about Gascan is:

- a repository-backed CMS without an admin UI
- a markdown normalization engine specialized for Scrivener/MultiMarkdown exports
- a small Ring app that serves dynamically rendered content from interned files

The heart of the design is not HTTP routing. It is the conversion of external writing artifacts into a stable local content store and then the transformation of those artifacts into final HTML through a custom AST-editing pipeline.
