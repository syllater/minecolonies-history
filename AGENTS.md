# Agent Instructions

## Session Start

At the beginning of each work session:

1. Read `AGENTS.md`, `PROJECT_GOAL.md`, `ROADMAP.md`, `STATUS.md`, `DECISIONS.md`, and `CHECKPOINTS.md` if present.
2. Inspect the current branch, latest commits, CI status and test evidence.
3. Inspect repository structure and source code before changing anything.
4. Resume from the most recent checkpoint.
5. Make targeted, testable changes and run the smallest relevant verification, followed by full build/tests and client smoke where possible.
6. Update status and checkpoints with real command results.

## Continuous Execution Authorization

The user explicitly instructed the agent to keep progressing through remaining milestones without pausing for separate milestone approvals, until the best attainable finished project has been built. Do not stop to request approval between milestones. Proceed sequentially, keep changes on feature branches, checkpoint progress, and fix build/runtime failures before expanding scope.

This authorization does not permit inventing test results, bypassing tests, disabling meaningful checks, merging PRs without user permission, overwriting user changes, or claiming unsolved integration tasks are complete. Be honest about environmental limitations and continue with the best available verification path.

## Development Rules

- Preserve existing user work.
- Do not use destructive Git commands or merge changes to `main` without explicit approval.
- Use pinned dependency versions and verify dependency resolution.
- Verify Minecraft/MineColonies APIs against the selected official version before depending on classes, methods, events or mixins.
- MineColonies is mandatory; do not replace it with a standalone simulation.
- Gameplay mutations and transactions must be server-authoritative.
- Keep client-only code out of server paths.
- Persist Imperium-owned data separately from MineColonies-private NBT unless a supported public extension point is verified.
- Do not copy upstream source/assets without checking license obligations.
- Do not disable tests or stub required gameplay to make builds appear complete.
- Keep English source identifiers and English/Dutch translations in sync.
- No runtime feature may depend on an external AI API key.

## Current Work

Current branch: `milestone-3/vertical-slice`.

Milestone 2 has a green CI build and headless `runClient` smoke test on the merge baseline. Milestone 3 adds the first Chancery/Diplomat/economy slice and must pass CI before it is treated as verified. After that, continue through the remaining milestones, preserving checkpoints and recording evidence.
