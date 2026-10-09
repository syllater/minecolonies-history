# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Current State

- Repository: https://github.com/syllater/minecolonies-history
- Working branch: `milestone-2/colony-integration`
- A GitHub Actions build/test workflow is configured at `.github/workflows/verify.yml`.
- A CI run on commit `46e2fe20c8a2eb5a87602d4856f514a66e6b0081` completed `./gradlew --no-daemon test build` successfully and uploaded a mod JAR artifact. The artifact was 10,615 bytes. This green run included the corrected `multipiston` artifact ID and Jared's Maven repository.
- A subsequent repository-centralization attempt failed because ModDevGradle project repositories overrode the settings repositories. Project-level repositories were restored; the latest repository configuration is being reverified by CI.
- The connected tool environment can read/write GitHub repository content but cannot directly invoke a local shell.

## Current Milestone

Milestone 2 — MineColonies Integration.

## Implemented in source

- `ColonyIdentity`: namespaced dimension + MineColonies colony ID, with validation and a deterministic storage key.
- `EmpireState`: Imperium-owned per-colony state with first-seen and last-seen game times, safe colony display names and rename observation.
- `EmpireStateSavedData`: versioned world SavedData persisted through the server overworld's data storage.
- `MineColoniesIntegration`: public API adapter for server-side colony lookup by position, enumeration of colonies in a level and idempotent state initialization.
- `MineColoniesLifecycleEvents`: periodic server-level scan for existing/new colonies.
- Unit tests for identity uniqueness/validation and state observation semantics.
- Pinned candidate 1.21.1 dependencies and the LDTTeam Maven repository.
- GitHub Actions workflow to run `./gradlew --no-daemon test build`, upload the mod JAR, and run a headless client-startup smoke test that checks for the `Sound engine started` marker.

## API investigation

Public APIs were inspected from the exact MineColonies source tag `v1.21.1-1.1.1403`, including `IMinecoloniesAPI`, `IColonyManager` and `IColony`. This is source inspection only; compile compatibility remains to be established by CI.

## Not yet verified on the latest commit

- Dependency resolution after restoring project-level repositories.
- The latest commit's unit test/build run.
- The new headless `runClient` smoke test.
- Actual save/reload behavior in a real MineColonies world.
- Dedicated-server startup with MineColonies and all required companions.

## Current blocker

The GitHub-connected environment cannot invoke a local shell. A prior CI revision did pass tests and build, but the latest repository configuration still needs its own green result. The workflow now also attempts a bounded headless client startup. A client startup marker is not a substitute for validating save/reload with an actual MineColonies colony.

## Next actions

1. Confirm the latest `build-and-test` CI job succeeds.
2. Confirm the headless client smoke job reaches its startup marker.
3. Fix any dependency, compile, test or runtime errors found.
4. Verify persistence and first-seen initialization in a real MineColonies world, including save/reload.
5. Request user approval before Milestone 3.
