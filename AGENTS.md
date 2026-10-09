# Agent Instructions

## Session Start

1. Read `AGENTS.md`, `PROJECT_GOAL.md`, `ROADMAP.md`, `STATUS.md`, `DECISIONS.md`, and `CHECKPOINTS.md`.
2. Inspect the current branch and recent workflow results.
3. Preserve all existing and concurrent repository changes.
4. Choose the next incomplete task that advances the playable mod.
5. Make coherent batches of changes and run relevant checks through CI.
6. Update status and checkpoints with actual evidence after each task group.
7. Continue to subsequent milestones without stopping for separate approval after every milestone; the owner has explicitly authorized continuous progression.

## Development Rules

- Preserve existing user work.
- Do not use destructive Git operations without explicit approval.
- Use pinned dependency versions for release builds.
- Verify APIs against the exact target dependency version.
- MineColonies is mandatory; never replace it with a standalone simulation.
- Keep gameplay and SavedData writes server-authoritative.
- Keep client-only code out of dedicated-server paths.
- Prefer public APIs and events over mixins.
- Do not disable tests or add fake stubs to make a build appear complete.
- Never claim a build, test, JAR or client launch succeeded unless actual workflow evidence supports it.
- Maintain English and Dutch translations for all player-visible strings.
- Do not require external AI API keys.
- Add resume-ready checkpoints so the next session can continue without rediscovering context.

## Active Build Target

Minecraft 1.21.1, NeoForge 21.1.x, Java 21, MineColonies required at runtime.

## Continuous Development Plan

Continue fixing current CI/runtime issues, then build the next playable slice. Prioritize complete systems and a real MineColonies integration over broad but nonfunctional placeholders. The intended order is economy and politics, complete custom professions/buildings, then military, level-based structures, multiplayer/save-load hardening and release verification.

The GitHub connector can edit files and read CI, but it does not provide a local Gradle shell in this environment. Use CI when available and document any remaining verification gap honestly.
