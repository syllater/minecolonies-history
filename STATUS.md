# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Current State

- Repository: https://github.com/syllater/minecolonies-history
- Working branch for this effort: `milestone-2/colony-integration`
- The GitHub-connected environment can inspect and commit repository files but cannot run a local shell or Gradle tasks.
- Existing source remains a minimal NeoForge entrypoint.

## Current Milestone

Milestone 2 — MineColonies Integration.

## Completed in this investigation

- Inspected repository files and original Milestone 0 checkpoint.
- Inspected public MineColonies API signatures in the upstream 1.21-era source.
- Confirmed the public API surface includes `IMinecoloniesAPI.getInstance()`, `getColonyManager()`, and colony lookup methods in `IColonyManager`.
- Confirmed `IColony` exposes colony ID, name, center, world and citizen manager.
- Confirmed MineColonies has job and building registry entry builders, but a functional custom job/building requires more than a single registry registration.
- Checked current upstream 1.21.1 release metadata for MineColonies, Structurize and BlockUI.
- Created a dedicated branch: `milestone-2/colony-integration`.

## Candidate versions found in upstream release metadata

- MineColonies: `1.21.1-1.1.1403`
- Structurize: `1.21.1-1.0.835-snapshot`
- BlockUI: `1.21.1-1.0.212-snapshot`

The exact Gradle artifact coordinates and resolution of these candidates have not been verified from this environment.

## Not yet completed

- No direct MineColonies integration code has been added yet.
- No MineColonies compile dependency has been resolved or compiled.
- No SavedData implementation has been compiled.
- No automated tests have been run during this session.
- No `./gradlew build` was run during this session.
- No `./gradlew runClient` was run during this session.

## Known blockers / risks

- The baseline's MineColonies version string predates the currently visible upstream release metadata and is only labelled as a candidate in the initial compatibility document.
- The upstream MineColonies source snippets show some Minecraft/NeoForge-era imports from its build line; exact artifacts need to be used to catch any mismatch.
- The project cannot be verified as a complete Milestone 2 integration until built in a shell-enabled checkout.

## Next actions

1. Use the pinned 1.21.1 official release metadata to choose artifact coordinates and repositories.
2. Add compile dependencies for only the APIs actually imported.
3. Implement a server-side colony lookup adapter and Imperium-owned SavedData with safe serialization.
4. Add unit/integration tests.
5. Run `./gradlew build` and tests in a shell-enabled environment.
6. Update this status with real results and request approval only after acceptance criteria are verifiably met.
