# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Current State

- Repository: https://github.com/syllater/minecolonies-history
- Working branch: `milestone-2/colony-integration`
- A GitHub Actions build/test workflow is configured at `.github/workflows/verify.yml`.
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
- GitHub Actions workflow to run `./gradlew --no-daemon test build` and upload the resulting mod JAR as an artifact.

## API investigation

Public APIs were inspected from the exact MineColonies source tag `v1.21.1-1.1.1403`, including `IMinecoloniesAPI`, `IColonyManager` and `IColony`. This is source inspection only; compile compatibility remains to be established by CI.

## Not yet verified

- Maven artifact resolution for all pinned coordinates.
- Java compilation against the exact dependencies.
- JUnit execution.
- Generated mod JAR contents.
- Actual game runtime behavior.
- `./gradlew runClient`.
- Dedicated-server startup with MineColonies and all required companions.

## Current blocker

The GitHub-connected environment cannot run local Gradle commands. The branch's GitHub Actions workflow should provide a real build/test result once GitHub executes it. If CI fails, fix the concrete failure and rerun CI before considering the milestone ready.

## Next actions

1. Check GitHub Actions for the latest branch run.
2. Fix any dependency, compile, test or resource errors.
3. Verify persistence and first-seen initialization in a real server/world test.
4. Confirm the build JAR is produced.
5. Run `./gradlew runClient` locally or through a suitable runner and inspect logs.
6. Request user approval before Milestone 3.
