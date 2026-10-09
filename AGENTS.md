# Agent Instructions

## Session Start

At the beginning of each work session:

1. Read `AGENTS.md`, `PROJECT_GOAL.md`, `ROADMAP.md`, `STATUS.md`, `DECISIONS.md`, and `CHECKPOINTS.md` if present.
2. Inspect Git status.
3. Inspect the repository structure and current source code.
4. Check the most recent checkpoint and test evidence.
5. Identify the next incomplete milestone task.
6. Make targeted changes.
7. Run the smallest relevant verification, then broader checks when appropriate.
8. Update status and checkpoints with real command results.

## Development Rules

- Preserve existing user work.
- Do not use destructive Git commands without explicit approval.
- Use pinned dependency versions for release builds.
- Verify APIs before depending on class names, methods, events, or mixins.
- MineColonies is mandatory; do not replace it with a standalone simulation.
- Keep gameplay server-authoritative.
- Keep client-only code out of server paths.
- Document known uncertainty instead of pretending it is resolved.
- Do not disable tests or stub required gameplay to make builds appear complete.
- Ask for user approval after each milestone.

## Current Milestone Gate

Current gate: **Milestone 0 - Repository and Compatibility**.

Do not begin Milestone 1 implementation until the user approves the Milestone 0 findings and dependency choices.
