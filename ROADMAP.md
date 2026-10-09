# Roadmap

## Milestone 0 — Repository and Compatibility

Status: baseline project exists. The target-version dependency choices are now pinned as candidates in Gradle, but their resolution must be confirmed by CI.

## Milestone 1 — Technical Foundation

Status: minimal NeoForge baseline exists. This milestone was not independently signed off before the user requested continuing work on Milestone 2. Any foundation changes needed for integration are kept narrow.

## Milestone 2 — MineColonies Integration

Implementation present:
- Public API adapter for server-side colony lookup by position.
- Discovery of loaded colonies by level.
- Stable colony identity: dimension + MineColonies colony ID.
- Separate Imperium-owned world SavedData with schema version.
- Idempotent initialization for new and already-existing colonies.
- Name refresh that does not reset first-seen time.
- Focused unit tests for identity/state logic.
- CI workflow for `test` and `build`.

Still required before acceptance:
- Successful resolution of the pinned Maven dependencies.
- Successful compilation against the exact MineColonies 1.21.1 release artifact.
- Successful execution of unit tests.
- Review the CI result and correct any failures.
- Runtime validation in a loaded MineColonies world, including save/reload.
- Explicit check of existing-colony and new-colony discovery.
- Document whether the actual build and client launch succeeded.

## Milestone 3 — First Playable Vertical Slice

Not started. Begins only after Milestone 2 passes its acceptance criteria and the user approves:
- One genuinely buildable imperial building.
- One registered MineColonies profession.
- One policy with a real effect.
- One treasury transaction.
- One GUI route with server-side validation.
- Save/load behaviour.

## Later Milestones

- Milestone 4: Parliament and politics.
- Milestone 5: Economy and citizen professions.
- Milestone 6: Military system.
- Milestone 7: Buildings and visual progression.
- Milestone 8: Empire simulation and strategy.
- Milestone 9: Multiplayer compatibility and hardening.
- Milestone 10: Release.
