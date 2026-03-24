## Source control

* Use **Jujutsu (`jj`)** for all source control operations.
* **Never** use `jj edit`. Always use `jj new`.
* **Never** squash unless explicitly instructed. The human must review all changes before squashing.
* When viewing diffs, use ``jj diff --git` for git-style diffs or `jj diff --stat` for summary diffs. Plain `jj diff` is misleading without color.
