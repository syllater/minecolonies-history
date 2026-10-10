# Agent Instructions

## Session Start

1. Read `AGENTS.md`, `PROJECT_GOAL.md`, `ROADMAP.md`, `STATUS.md`, `DECISIONS.md`, and `CHECKPOINTS.md`.
2. Inspect current repository state and branch head.
3. Review the latest checkpoint and verify its claims against files and CI evidence.
4. Identify the next incomplete, high-value project task.
5. Make targeted changes without overwriting user work.
6. Run the smallest relevant check, then wider checks when possible.
7. Record only actual command/test results.
8. Update status and checkpoints after each coherent task group.

## Development Rules

- Preserve existing user work.
- Do not use destructive Git commands without explicit approval.
- Use pinned dependency versions for release builds.
- Verify APIs against the exact target dependency version.
- MineColonies is mandatory; never silently replace it with a standalone simulation.
- Keep gameplay server-authoritative.
- Keep client-only code out of server paths.
- Prefer public extension APIs and events over mixins.
- Do not disable tests or add fake implementations to make a build appear successful.
- Never claim a build, test or client launch succeeded unless it was actually executed.
- The project owner explicitly requested on 2026-10-10 that development continue across the remaining milestones without waiting for approval at every milestone boundary; follow that instruction.
- Do not merge or publish a release without the owner's explicit direction.

## Milestone Continuation

Continue through the roadmap toward a usable final mod/JAR, carrying forward non-blocking polish items and prioritizing features that build, test, and work. When a task group is implemented, commit a checkpoint and move directly to the next missing high-value task rather than waiting for another approval.

This authorization does not waive correctness: do not represent a milestone as accepted unless its evidence is real. Keep survival and multiplayer playtests marked pending unless actually performed.

## Environment Limitations

The GitHub-connected interface can edit repository files and read Actions logs but cannot execute a local Gradle shell. Use repository CI for compile/test/client/server checks and record exact current-commit results. Actual in-world survival and multiplayer acceptance testing must remain open unless performed in-game.
