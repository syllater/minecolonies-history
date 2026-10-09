# Roadmap

## Milestone 0 — Repository and Compatibility

Status: baseline created and built in the original local environment, according to the existing checkpoint. Exact API integration verification then remained pending.

## Milestone 1 — Technical Foundation

Status: not separately implemented as a milestone. The existing baseline contains the minimal mod entrypoint, Gradle configuration, metadata and starter translations. Any additional foundation required by Milestone 2 is implemented as narrowly as possible.

## Milestone 2 — MineColonies Integration

Scope:
- Inspect selected MineColonies APIs.
- Provide a safe server-side colony lookup.
- Introduce empire state keyed to colony identity.
- Ensure existing and new colonies are initialized idempotently.
- Add tests for identity, data defaults and migration/serialization behaviour.
- Record direct limitations of the upstream extension APIs.

Progress:
- Public upstream signatures for `IMinecoloniesAPI`, `IColonyManager`, `IColony`, `ICitizenData`, `IJob`, `JobEntry.Builder` and `BuildingEntry.Builder` have been inspected.
- A separate Imperium-owned world SavedData design is documented.
- Actual direct integration code and local compile verification are still required before this milestone can be considered complete.

Acceptance:
- The selected dependencies resolve in Gradle.
- Integration code compiles against the exact selected artifacts.
- Server-side colony lookup by position works.
- Existing and new colony data are handled idempotently.
- Imperium data survives save/load and is not placed in private MineColonies NBT.
- Tests cover colony identity and saved data behaviour.
- The build succeeds.

## Milestone 3 — First Playable Vertical Slice

After Milestone 2 is approved:
- One truly buildable imperial building.
- One registered profession.
- One policy.
- One treasury transaction.
- One GUI path with server-side validation.
- Save/load for the slice.

## Later Milestones

- Milestone 4: Parliament and politics.
- Milestone 5: Economy and citizen professions.
- Milestone 6: Military system.
- Milestone 7: Buildings and visual progression.
- Milestone 8: Empire simulation and strategy.
- Milestone 9: Multiplayer compatibility and hardening.
- Milestone 10: Release.
