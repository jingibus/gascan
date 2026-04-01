# Phase 1 Implementation Plan

## Purpose

Phase 1 exists to make Gascan easier to evolve without changing the public result.

Concretely, this phase should leave us with:

- a clearer separation between content loading, page rendering, and HTTP adaptation
- a rendering path that can be called without going through Ring
- a small, explicit site-model layer that future static generation can build on
- a test workflow that is actually runnable and useful for auditing changes

This plan is intentionally sliced into independently shippable steps so each change can be reviewed, validated, and, if necessary, reverted without losing the whole effort.

## Phase 1 Exit Criteria

Phase 1 is complete when all of the following are true:

- the current server still works for interactive preview
- there is a reliable command to run the automated tests
- there are tests for the core page-rendering paths and visibility rules
- page rendering functions can be called directly without going through Ring routes
- the codebase has a small site-model API that future build code can depend on
- metadata shape is documented in code and tested, including fields relied on in practice such as `:src-path`

## Auditability Principles

Every step in this phase should satisfy the same audit rules:

- the step should have a narrow goal
- the step should add or improve validation
- the step should leave the app runnable
- the step should be reviewable with a focused diff
- the step should document how to verify it

For this project, “auditable” should mean a reviewer can:

1. read the diff
2. run one or two commands locally
3. see whether the intended behavior changed

## Current Reality

The repository already has some tests under `test/gascan`, but they are not yet enough to serve as a reliable implementation safety net for modernization. They are mostly focused on AST and markdown transformation behaviors, which is valuable, but they do not yet establish a full golden path for:

- post lookup and visibility
- metadata read/write behavior
- route-independent page rendering
- representative full-page output

So the first practical priority is not just “write more tests,” but “make tests runnable and meaningful as a workflow.”

## Step-by-Step Plan

## Step 1: Establish A Real Test Entry Point

### Goal

Make it easy to run the project’s automated tests with one command and know whether the result is trustworthy.

### Work

- Verify and document the intended command for running tests
  - likely `lein test`, unless project constraints say otherwise
- Fix anything that prevents tests from running cleanly as a suite
- Make sure test execution exits nonzero on failures
- Add a short section to developer docs describing:
  - how to run tests
  - what they currently cover
  - what they do not cover

### Why this comes first

Without a stable test entry point, later refactors are harder to audit. This step creates the minimum platform for all the others.

### Deliverable

- one documented command for running tests
- current tests run as a suite

### Validation

- run the documented test command
- confirm it completes successfully
- confirm introducing a deliberate failure causes the command to fail

### Suggested diff scope

- test harness setup only
- minimal documentation updates

## Step 2: Add Golden-Path Rendering Tests

### Goal

Create tests that validate representative rendered output, not just isolated AST utilities.

### Work

- Add tests for representative full-page rendering such as:
  - homepage rendering
  - posts index rendering
  - a published post with plain markdown
  - a post with assets
  - RSS rendering
- Favor assertions on stable features over brittle full-string snapshots
  - title present
  - expected links present
  - expected date present
  - expected audio/image markup present where appropriate
- Keep fixtures based on existing repo content where possible

### Why this matters

Phase 1 will refactor call paths around rendering. These tests tell us whether the public behavior still matches expectations.

### Deliverable

- automated tests that exercise page-producing functions end to end

### Validation

- run the test suite
- verify failures are clear if a rendered page loses required content

### Suggested diff scope

- new tests
- tiny helper functions for test setup if needed

## Step 3: Add Tests For Content Visibility And Lookup

### Goal

Protect the behavioral rules around published and soft-published posts before moving code around.

### Work

- Add tests for:
  - `visible-to-session?`
  - `soft-visible-to-session?`
  - title slug lookup behavior
  - ID lookup behavior
  - criteria filtering behavior
- Prefer table-driven tests using simple synthetic post maps where possible

### Why this matters

Future static generation depends on a clean notion of “which content is public” and “how we resolve posts.” This is core domain logic and should be locked down early.

### Deliverable

- automated coverage for visibility and lookup semantics

### Validation

- run test suite
- confirm public/private behavior is explicitly covered

### Suggested diff scope

- tests only, or tests plus tiny refactors to make logic easier to call

## Step 4: Formalize Metadata Shape

### Goal

Bring the code’s actual metadata model and the declared spec closer together.

### Work

- Audit which post fields are actually used today
- Update the post spec or adjacent validation so runtime-used fields are modeled honestly
- Decide whether `:src-path` should:
  - become an official optional field in interned posts
  - or be removed from runtime expectations
- Add tests for metadata read/write round-tripping
- Add tests that validate loading current `resources/metadata.edn`

### Why this matters

Static generation and CLI workflows both depend on metadata being explicit and trustworthy. Drift between “real data” and “declared data” makes future work riskier.

### Deliverable

- metadata model documented in code
- metadata read/write behavior under test

### Validation

- run tests against current metadata
- verify round-trip behavior for representative post records

### Suggested diff scope

- `post_spec`
- metadata-related tests
- small supporting refactors only

## Step 5: Introduce A Site Model Namespace

### Goal

Create a small API layer that represents the content the site is built from, independent of HTTP.

### Work

- Add a new namespace, likely something like `gascan.site` or `gascan.site-model`
- Move or wrap logic for:
  - loading all posts
  - computing visible posts for a session
  - finding posts by locator
  - filtering posts by criteria
  - obtaining public-post collections for route enumeration later
- Keep this layer data-oriented and side-effect-light

### Why this matters

Right now content concerns are spread across `posts`, view namespaces, and routing. A site-model layer gives Phase 2 a clean dependency point.

### Deliverable

- one namespace that exposes the canonical read API for site content

### Validation

- existing tests still pass
- new unit tests cover the site-model API directly
- existing routes can read through this layer without behavior changes

### Suggested diff scope

- new namespace
- mechanical call-site updates where appropriate
- tests

## Step 6: Extract Pure Page Renderers

### Goal

Separate “produce page content” from “adapt it to Ring.”

### Work

- Identify rendering functions that are already nearly pure
- Normalize them into a clearer pattern, for example:
  - page renderer returns HTML string or nil
  - route layer wraps that in status and headers
- Introduce explicit renderers for:
  - homepage
  - posts index
  - post detail
  - RSS
  - what-it-is
- Keep resource-serving concerns separate from HTML generation

### Why this matters

This is the key prerequisite for static generation. Once renderers are independent of Ring, Phase 2 can write their output to files instead of serving them over HTTP.

### Deliverable

- rendering functions callable directly from code without hitting routes

### Validation

- golden-path rendering tests pass
- local server behavior remains unchanged
- a simple REPL or test call can render each page kind directly

### Suggested diff scope

- view namespaces
- light route handler updates
- tests

## Step 7: Thin The Ring Adapter Layer

### Goal

Make `server.clj` mostly about HTTP transport and resource wiring, not business logic.

### Work

- Update route handlers to call the new site-model and rendering APIs
- Keep responsibilities in `server.clj` limited to:
  - routing
  - HTTP status selection
  - headers/content type
  - middleware composition
- Avoid changing public URLs in this phase

### Why this matters

It creates a clear architectural seam:

- core site logic below
- HTTP adapter above

That seam is exactly what static generation needs.

### Deliverable

- slimmer route handlers with less embedded domain logic

### Validation

- test suite passes
- manual smoke test of representative routes still works

### Suggested diff scope

- `server.clj`
- minor related route tests if added

## Step 8: Add Manual Smoke-Test Documentation

### Goal

Support human auditing alongside automated tests.

### Work

- Add a short checklist to a markdown doc, likely this plan or `README.md`, covering:
  - how to run tests
  - how to run the local server
  - which pages to open for a quick smoke test
  - what expected behavior to confirm
- Include a compact list such as:
  - homepage loads
  - posts index loads
  - one published post renders
  - one post with assets renders
  - RSS endpoint responds

### Why this matters

Auditability is not just unit tests. A lightweight manual verification path makes it much easier to review incremental refactors confidently.

### Deliverable

- a repeatable human validation checklist

### Validation

- follow the checklist on a local run and confirm it is sufficient to catch obvious regressions

### Suggested diff scope

- docs only

## Recommended Sequencing

The safest order is:

1. Establish the test entry point
2. Add golden-path rendering tests
3. Add visibility and lookup tests
4. Formalize metadata shape
5. Introduce the site model namespace
6. Extract pure page renderers
7. Thin the Ring adapter layer
8. Add smoke-test documentation

This order front-loads validation so the refactors happen after the safety net starts to exist.

## Suggested Commit/Review Boundaries

If we implement this incrementally, the review slices should look roughly like this:

1. “Make tests runnable”
2. “Add rendering golden-path tests”
3. “Add visibility and metadata tests”
4. “Introduce site model API”
5. “Extract page renderers from routes”
6. “Simplify server adapter layer”
7. “Document audit checklist”

Each slice should be reviewable on its own and should leave the codebase in a working state.

## What Success Looks Like After Phase 1

At the end of Phase 1, the project should still feel like Gascan, but it should be much easier to inspect and trust:

- you can run tests and get a meaningful answer
- you can audit rendering behavior without mentally executing Ring routes
- you can see a clear separation between site logic and server logic
- Phase 2 can start from “write rendered pages to files” rather than “untangle the app first”

That is the right kind of progress: structural, testable, and incremental.
