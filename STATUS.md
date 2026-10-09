# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Current state

- Repository: https://github.com/syllater/minecolonies-history
- Working branch: `milestone-2/colony-integration`
- Changes are committed to that branch using GitHub's repository file APIs.
- This environment cannot execute a local shell or Gradle tasks, so build success must come from CI or a shell-enabled checkout.

## Current milestone

Milestone 2 — MineColonies Integration.

## Implemented in this branch

- `ColonyIdentity`: dimension + MineColonies colony ID as stable key.
- `EmpireState`: custom state for first seen, last-seen heartbeat and colony name.
- `EmpireStateSavedData`: Imperium-owned world save storage under the overworld's DataStorage.
- `MineColoniesIntegration`: read-only public API adapter and idempotent reconciliation of colonies.
- `MineColoniesLifecycleEvents`: periodic server-side colony scanning.
- Unit tests for stable identity and core state behaviour.
- Gradle coordinates for MineColonies and its required dependency baseline.
- English and Dutch starter language files retained.
- GitHub Actions workflow: `.github/workflows/gradle.yml`, intended to run `./gradlew --no-daemon clean build` and upload the mod JAR on success.

## Dependency candidates pinned

- Minecraft 1.21.1.
- NeoForge 21.1.256.
- Java 21.
- ModDevGradle 2.0.148.
- MineColonies 1.1.1403-1.21.1.
- Structurize 1.0.832-1.21.1-snapshot.
- BlockUI 1.0.199-1.21.1-snapshot.
- Domum Ornamentum 1.0.223-snapshot.
- MultiPiston 1.2.51-1.21.1-snapshot.

The versions align with the dependency baseline declared by the official MineColonies 1.21.1 release metadata. Gradle artifact resolution still needs confirmation from the build.

## Verification status

- Public API source reviewed from the pinned MineColonies 1.21.1 release tag.
- No local `./gradlew build` executed by this session.
- No local `./gradlew test` executed by this session.
- No `./gradlew runClient` executed by this session.
- Remote CI result still needs to be retrieved/verified before Milestone 2 can be marked accepted.

## Known limitations

- No new MineColonies building, profession, worker AI, or Structurize schematic has been added yet.
- Runtime persistence and automatic colony discovery are implemented but not verified in an actual Minecraft world here.
- The first version scans colonies once per 200 ticks per server level; this is intentionally low-frequency and idempotent.
- GitHub Actions runner and remote network access are prerequisites for remote build verification.

## Next actions

1. Retrieve the workflow run triggered by the last branch push.
2. If the build fails, inspect logs and fix the actual root cause.
3. Repeat the CI build until it passes or a concrete external blocker remains.
4. Update this file with the actual CI result and artifact details.
5. Submit Milestone 2 for user review; do not start Milestone 3 without approval.
