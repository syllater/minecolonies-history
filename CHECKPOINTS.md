# Checkpoints

## 2026-10-09 — Milestone 2 Source and Build Setup

### Repository

- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Implemented files

- `src/main/java/com/imperium/realms/colony/ColonyIdentity.java`
- `src/main/java/com/imperium/realms/colony/EmpireState.java`
- `src/main/java/com/imperium/realms/colony/EmpireStateSavedData.java`
- `src/main/java/com/imperium/realms/colony/MineColoniesIntegration.java`
- `src/main/java/com/imperium/realms/colony/MineColoniesLifecycleEvents.java`
- `src/test/java/com/imperium/realms/colony/ColonyIdentityTest.java`
- `src/test/java/com/imperium/realms/colony/EmpireStateTest.java`

### Integration decisions

- Use the public API from the exact MineColonies tag `v1.21.1-1.1.1403`.
- Look up colonies on the logical server through `IMinecoloniesAPI.getInstance().getColonyManager()`.
- Identify records by dimension ID + colony ID, not by the editable name or center position.
- Store Imperium-owned records in an overworld SavedData registry so all dimensions share a single index.
- Periodically reconcile loaded colonies; missing records are inserted, existing records are observed but not reset.
- Avoid private MineColonies NBT mutation and mixins in this milestone.

### Build setup added

- Added LDTTeam Maven repository.
- Pinned MineColonies and its required runtime dependency baseline.
- Configured JUnit Jupiter.
- Added `.github/workflows/gradle.yml` for `./gradlew --no-daemon clean build` and JAR artifact upload.

### Verification

The GitHub file API has confirmed the writes were accepted, but no Gradle command has been executed by this session. Wait for the actual GitHub Actions result before stating the source compiles.

### Critical correction

A prior investigation referenced MineColonies `version/main`, but that branch is the older Minecraft line. It is not used as evidence for the 1.21.1 implementation. The 1.21.1 signatures were reviewed against the official tag `v1.21.1-1.1.1403`.

### Next

Check Actions run status, fix any actual compile/dependency failures, and only then present Milestone 2 for approval.
