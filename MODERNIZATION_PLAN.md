# Gascan Modernization Plan

## Goal

Modernize Gascan so that:

- hosting is cheap and operationally boring
- publishing is a supported command-line workflow instead of a REPL ritual
- POSSE and syndication become first-class capabilities without making the core site harder to reason about

This plan assumes we want to preserve what is distinctive and valuable in the project:

- the existing content corpus
- the current site identity and HTML rendering style
- the import and normalization knowledge already encoded in the markdown pipeline
- the idea that the canonical source of truth belongs to Bill, not to a third-party platform

## Executive Summary

The best direction is to turn Gascan from a long-running web app into a static site generator plus a publishing/distribution toolchain.

Recommended end state:

- Gascan builds static HTML, RSS, and assets into an output directory
- the site is hosted on static hosting
- imports and publication are driven by a CLI
- the canonical content store remains local and repo-backed
- POSSE is implemented as a separate distribution layer that reads canonical posts and publishes derivative social posts to external platforms

The migration should be incremental, not a rewrite. The existing import and rendering code is reusable.

## Problems To Solve

### 1. Hosting is heavier than the site needs

Today the project runs as a Jetty server on Heroku. That adds:

- always-on hosting
- deployment complexity
- operational cost and fragility
- tighter coupling between rendering and serving

But the site output is almost entirely deterministic from repo contents, which makes a server unnecessary for most cases.

### 2. Publishing requires too much tacit knowledge

The current author workflow depends on:

- a carefully configured Scrivener export setup
- importing content through REPL calls
- running a local Gascan instance
- previewing and mutating state manually

This is powerful for the author who remembers the ritual, but it is not ergonomic or robust.

### 3. Distribution is manual

The site is the canonical home of the work, but syndication is mostly manual. That leaves a lot of value on the table:

- no one-command announcement workflow
- no stored record of where a post was syndicated
- no generation of thread-oriented social formats
- no layered publishing strategy for different platforms

## Proposed Target Architecture

Gascan should become three loosely coupled subsystems.

### A. Canonical Content Core

This is the part that should remain the heart of the project.

Responsibilities:

- import raw authoring artifacts
- normalize markdown and linked resources
- manage canonical post metadata
- render canonical HTML pages and feeds

Likely reused from today:

- `gascan.remote-posts`
- much of `gascan.posts`
- `gascan.intern`
- `gascan.multimarkdown`
- `gascan.ast`
- `gascan.multi-to-intern-markdown`
- most of the rendering logic in the view namespaces

### B. Static Site Builder

This replaces the long-running server as the primary production output path.

Responsibilities:

- enumerate all public routes
- render each route to an HTML file
- render RSS to a static file
- copy static assets into an output directory
- optionally precompute multiple image sizes

Proposed output:

- `dist/index.html`
- `dist/posts/.../index.html`
- `dist/rss.xml`
- `dist/images/...`
- `dist/posts/...` assets

The existing Ring handler can remain for local preview during migration, but it should become optional rather than foundational.

### C. Distribution / POSSE Layer

This is a new subsystem and should be separate from site generation.

Responsibilities:

- generate platform-specific outbound posts from canonical content
- publish to external platforms
- store syndication state and remote IDs/URLs
- support retries and partial failure handling

Supported targets can be added in stages:

- Bluesky
- Mastodon
- Substack

This layer should think in terms of publication jobs and syndication records, not web routes.

## Guiding Principles

### Preserve the repo as the canonical source of truth

The canonical version of a post should remain local and owned:

- markdown
- metadata
- generated site
- syndication records

External platforms are distribution channels, not the source of truth.

### Prefer explicit files over hidden runtime state

A static-generation-oriented system benefits from state that is inspectable and committable. That suggests:

- replacing or reducing mutable in-process caches
- storing syndication metadata in EDN or frontmatter-like structures
- making builds reproducible from the filesystem

### Keep the import/rendering intelligence

The hard part of Gascan is not the server. The hard part is compensating for real-world markdown and Scrivener export weirdness. That knowledge should be retained.

### Separate “build the site” from “distribute the post”

These are different workflows with different failure modes. A failed Mastodon publish should not threaten the build. A static site build should not require external API access.

## Recommended Migration Phases

## Phase 1: Stabilize The Current Core

Goal:
Make the existing code easier to evolve without changing the public result.

Work:

- Introduce a clearer internal boundary between:
  - content loading
  - rendering
  - route-to-response adaptation
- Refactor route handlers so page rendering functions can be called without going through Ring
- Create a small “site model” API for:
  - all posts
  - visible posts
  - post lookup
  - route enumeration inputs
- Audit metadata shape and formally account for fields like `:src-path`
- Add more tests around:
  - post lookup and visibility
  - metadata reads/writes
  - full page rendering for representative posts

Why first:

The current code is already close to static generation, but the rendering logic is routed through the server layer in places. We want a stable base before adding new output modes.

Deliverable:

- existing server still works
- rendering functions are callable from a build pipeline without HTTP

## Phase 2: Add Static Site Generation

Goal:
Produce a complete deployable site directory from the repo contents.

Work:

- Add a build namespace, for example `gascan.build`
- Enumerate all public pages:
  - homepage
  - post index pages
  - post detail pages
  - what-it-is pages
  - RSS
- Render each page to a file path under `dist/`
- Copy required resources from `resources/`
- Decide how image handling should work:
  - simplest path: ship original images and preserve width query parameters only for preview mode
  - better path: generate sized derivatives during build and rewrite image URLs to point at them
- Add CLI command:
  - `gascan build`

Key design choice:

Do not try to preserve the current server middleware model in the output build. Instead, render final files directly.

Deliverable:

- `dist/` can be served by any static file server
- local validation can happen by opening built files or serving `dist/` with a tiny static server

## Phase 3: Replace REPL Publishing With A CLI Workflow

Goal:
Make importing and publishing a supported user workflow.

Work:

- Add commands such as:
  - `gascan import <path>`
  - `gascan publish --title <title>`
  - `gascan refresh --title <title>`
  - `gascan build`
  - `gascan preview`
- Make commands print clear summaries of what changed:
  - imported post title
  - copied assets
  - resulting metadata changes
  - output paths
- Add optional prompts or flags for:
  - status
  - filters
  - timestamp override
- Keep preview lightweight:
  - either use the existing Ring server
  - or serve the generated `dist/` directory locally

Potential extension:

Add a single command like `gascan publish-flow <path>` that imports, lets you set metadata, builds, and previews.

Deliverable:

- publishing no longer requires Emacs, a REPL, or remembered function names

## Phase 4: Reduce Scrivener Lock-In

Goal:
Keep Scrivener support, but stop depending on it as the only realistic authoring route.

Work:

- Define a first-class native input format for Gascan, likely:
  - plain Markdown
  - optional frontmatter or sidecar EDN metadata
  - folder-based assets where needed
- Update import code so it can ingest:
  - Scrivener/MultiMarkdown exports
  - plain Markdown files
  - Markdown directories with assets
- Document the “recommended” path as the native format, while preserving Scrivener compatibility for older content or preferred projects

Why this matters:

It removes the single biggest authoring bottleneck without forcing migration of the existing corpus all at once.

Deliverable:

- new posts can be authored without Scrivener-specific setup

## Phase 5: Move Production Hosting To Static Infrastructure

Goal:
Retire Heroku and deploy built output to static hosting.

Good candidate hosts:

- Netlify
- Cloudflare Pages
- GitHub Pages
- S3 + CDN

Selection criteria:

- ease of deployment
- custom domain support
- redirects and headers
- cost
- feed/static asset handling

Suggested approach:

- keep the existing server as a fallback until parity is confirmed
- deploy the generated site in parallel to a preview domain
- compare route coverage and output quality
- cut over once stable

Deliverable:

- no always-on app server required for production

## Phase 6: Add POSSE As A Separate Distribution Workflow

Goal:
Make canonical site publication and external syndication part of the supported workflow.

Work:

- Define a syndication record model, for example per post:
  - target platform
  - publication status
  - remote post/thread IDs
  - remote URL
  - timestamps
  - last error
- Store these records locally, either:
  - in `metadata.edn`
  - in a separate `resources/syndication.edn`
  - or per-post sidecar metadata
- Add a distribution command layer:
  - `gascan syndicate --title <title> --to bsky`
  - `gascan syndicate --title <title> --to mastodon`
  - `gascan syndicate --title <title> --to substack`

Recommendation:

Start by generating platform-specific draft text locally before attempting direct publish APIs.

That staged rollout should be:

1. Generate suggested post copy and thread segments
2. Support manual review/edit
3. Add authenticated publish
4. Add status tracking and retry logic

Deliverable:

- a post can be published to the site and syndicated through supported commands

## POSSE Design Recommendation

Treat POSSE content as derived artifacts from the canonical post.

For each post, derive one or more outbound forms:

- short announcement
- medium announcement
- thread plan
- quote/excerpt set

Then let each target choose from those forms.

Example target behavior:

- Bluesky
  - single skeet for most posts
  - optional thread mode for longer ideas
- Mastodon
  - similar to Bluesky, but tuned for character limits and link presentation
- Substack
  - likely better for notes, cross-post essays, or announcement digests than for every post automatically

### Megathreads

Megathread support should be intentional, not bolted on.

Recommended model:

- build a thread generator that works from a canonical post plus rules
- split on headings, sections, or selected excerpts
- generate a linked sequence of posts
- allow manual editing before publication
- record the resulting thread as a list of remote post IDs

This should be a specialized output mode, not the default for every post.

## Data Model Recommendations

### Post metadata

The post metadata model should likely be formalized to include fields already used in practice:

- `:id`
- `:title`
- `:timestamp`
- `:status`
- `:filter`
- `:markdown-rel-path`
- `:extra-resources-rel`
- `:src-path`
- possibly `:slug`
- possibly `:description` or `:summary`

Adding `:slug` explicitly would be useful if title-derived URLs ever need to stop changing with title edits.

### Syndication metadata

Keep syndication metadata separate enough that the core site model remains easy to understand.

One reasonable shape:

```clojure
{:post-id #uuid "..."
 :targets
 {:bsky {:status :published
         :url "https://..."
         :remote-id "..."
         :published-at 1710000000000}
  :mastodon {:status :draft-generated}
  :substack {:status :failed
             :last-error "401 unauthorized"}}}
```

## CLI Recommendation

The CLI is the key usability win. A good first cut would support:

- `gascan import <path>`
- `gascan list drafts`
- `gascan set-meta --title <title> --status published --filter technical,wphillips-weekly`
- `gascan build`
- `gascan preview`
- `gascan publish-site`
- `gascan syndicate --title <title> --to bsky`

Nice-to-have later:

- `gascan new`
- `gascan doctor`
- `gascan deploy`
- `gascan thread-plan --title <title> --to bsky`

## Suggested Internal Refactors

These are not goals in themselves, but they will make the roadmap much easier.

### 1. Introduce pure page renderers

Prefer functions shaped like:

- `render-index-page site-model -> html`
- `render-post-page site-model post -> html`
- `render-rss site-model -> xml`

Then let Ring or the static builder adapt those outputs to transport.

### 2. Separate route description from route serving

Have one place that knows:

- canonical route path
- page kind
- file output path for static builds

That will reduce duplication between server routes and build output enumeration.

### 3. Isolate side effects

Make it clearer which functions:

- read the filesystem
- write interned files
- mutate metadata
- publish externally

That will make testing and CLI behavior much more predictable.

### 4. Add golden-path integration tests

Especially for:

- importing a sample post
- building a static output directory
- rendering RSS
- rendering a post with assets
- generating a social thread plan

## Risks And Tradeoffs

### Risk: static generation exposes assumptions hidden by Ring

The current server middleware does some useful work, especially for images and content types. A build pipeline must make those outputs explicit rather than relying on runtime behavior.

### Risk: POSSE can sprawl quickly

Publishing APIs, auth handling, retries, formatting, rate limits, and partial failures can take over the project if introduced too early. That is why POSSE should come after static generation and CLI improvements.

### Risk: migration fatigue

The easiest way for this effort to stall is trying to solve hosting, authoring, native markdown, and cross-posting all at once. The phases should stay narrow.

## Recommended Order

If only a few things happen, they should happen in this order:

1. Add a static build pipeline
2. Add a CLI for import/build/publish
3. Move hosting to static infrastructure
4. Add native non-Scrivener authoring support
5. Add POSSE draft generation
6. Add direct POSSE publishing

This order produces value early:

- hosting gets simpler
- daily publishing gets easier
- distribution can grow without destabilizing the core site

## Concrete Near-Term Plan

The most pragmatic next implementation slice is:

1. Refactor rendering so pages can be produced without Ring
2. Add `gascan.build` to emit `dist/`
3. Add a minimal CLI with `import`, `build`, and `preview`
4. Validate the generated site against the current live/server-rendered output

Once that works, the project will already feel much lighter and more maintainable.

## Recommendation

Do not rewrite Gascan as a new app.

Instead:

- keep the existing parsing and rendering intelligence
- treat the current server as a preview adapter
- add a static build path
- add a real CLI
- add POSSE as a later, separate distribution layer

That path addresses the biggest pain points directly while preserving the parts of the project that are unique and already working.
