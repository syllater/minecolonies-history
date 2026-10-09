# Agent Instructions

## Session Start

1. Read `AGENTS.md`, `PROJECT_GOAL.md`, `ROADMAP.md`, `STATUS.md`, `DECISIONS.md`, and `CHECKPOINTS.md`.
2. Inspect current repository state and branch.
3. Review the latest checkpoint and verify its claims against current files.
4. Identify the next uncompleted task in the active milestone.
5. Make targeted changes without overwriting user work.
6. Run the smallest relevant check, then wider checks when possible.
7. Record only actual command/test results.
8. Update status and checkpoints after each task group.

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
- Request approval after each milestone.

## Milestone Gate

Active gate: **Milestone 2 — MineColonies Integration**.

Within this milestone, implement the public colony lookup adapter and Imperium-owned saved state; add verification tests; verify dependency resolution and compile in an environment with shell access. Do not start Milestone 3 until the Milestone 2 acceptance criteria are documented and user approval is obtained.

The GitHub-only tool interface can read and write source files but cannot itself execute a local Gradle shell. Where direct execution is unavailable, use repository CI if configured and otherwise record the verification as pending.
