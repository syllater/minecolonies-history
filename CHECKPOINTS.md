# Checkpoints

## 2026-10-09 — Milestone 2 implementation checkpoint

### Branch

`milestone-2/colony-integration`

### Implemented files

- `src/main/java/com/imperium/realms/colony/ColonyIdentity.java`
- `src/main/java/com/imperium/realms/colony/EmpireState.java`
- `src/main/java/com/imperium/realms/colony/EmpireStateSavedData.java`
- `src/main/java/com/imperium/realms/colony/MineColoniesIntegration.java`
- `src/main/java/com/imperium/realms/colony/MineColoniesLifecycleEvents.java`
- `src/test/java/com/imperium/realms/colony/ColonyIdentityTest.java`
- `src/test/java/com/imperium/realms/colony/EmpireStateTest.java`

### Build configuration

- Added LDTTeam Maven repository.
- Pinned candidate MineColonies/Structurize/BlockUI/Domum Ornamentum/MultiPiston dependencies.
- Added JUnit 5 test dependencies and JUnit Platform configuration.
- Added `.github/workflows/verify.yml` to build/test on pushes, pull requests and manual workflow dispatch; successful builds upload the mod JAR as an artifact.

### API evidence

Inspected the API from MineColonies tag `v1.21.1-1.1.1403`, in particular the public `IColonyManager` lookups and `IColony` identity/name/world methods.

### Test status

- Unit tests are written but have not been executed from this environment.
- Gradle dependency resolution has not yet been verified.
- The mod JAR has not yet been built in this session.
- `./gradlew runClient` has not yet been executed in this session.

### Architectural notes

- Empire records use dimension ID + colony ID.
- Persist custom state in Imperium-owned world SavedData.
- Store records in overworld storage to support cross-dimension identity without record collisions.
- Periodic discovery is idempotent and does not reset an existing empire record.
- No MineColonies private NBT modification or mixins have been introduced.

### Next

Inspect the GitHub Actions run for this branch, fix any real failures, then perform a save/reload verification in a local game or suitable test environment. Do not mark Milestone 2 complete without that evidence.
