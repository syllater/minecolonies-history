# Checkpoints

## 2026-10-09 — Milestone 3 first vertical slice (in progress)

### Branch and baseline

- Repository: https://github.com/syllater/minecolonies-history
- Branch: milestone-2/colony-integration
- Last fully verified baseline before Milestone 3 source additions: CI run 37925143022; build/tests and client startup smoke passed on that earlier revision.
- Current milestone-3 source revision is awaiting a new CI verdict.

### Added in this slice

- Treasury and knowledge points persisted in EmpireState.
- Economic policy enum: balanced, mercantile, welfare, austerity.
- Daily tax processing with one turn per overworld day, population-based income, and stability trade-offs.
- Server-authoritative /imperium commands and permission checks.
- English/Dutch command and building translations.
- MineColonies registry entries for Philosopher and Imperial Archive, plus an AI work loop.
- Crafting recipe and placeholder model assets.
- Expanded unit tests for tax turns, policy multipliers, treasury safety, investment conversion and scholarship interval.

### Build feedback and correction

The first new compile attempt failed in EntityAIWorkPhilosopher because idleState(), markIdle() and markWorking() were mistakenly assumed to exist in the parent class, and decide() was incorrectly marked @Override. The latest implementation now defines those helpers locally and removes the invalid override; verify that fix using the next Actions run.

### Current limitations

- No verified Structurize blueprint pack yet.
- No full player-facing BlockUI GUI yet.
- No real-world save/reload or worker-assignment test yet.
- Do not mark Milestone 3 complete before the latest CI and actual schematic/runtime criteria pass.
