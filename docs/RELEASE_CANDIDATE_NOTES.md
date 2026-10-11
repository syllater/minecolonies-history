# Imperium: European Realms — 0.1.0-rc1

This is a release candidate for evaluation, not a final public release.

## Contents

The release-candidate bundle contains the built Imperium mod JAR, `SHA256SUMS.txt`, project README/changelog/compatibility notes, this file and the in-game playtest/release checklist.

## Target environment

- Minecraft Java Edition 1.21.1
- Java 21
- NeoForge 21.1.x
- MineColonies `1.1.1403-1.21.1`
- Structurize `1.0.832-1.21.1-snapshot` or a compatible newer 1.21.1 build
- BlockUI `1.0.199-1.21.1-snapshot` or compatible newer build
- Domum Ornamentum `1.0.223-snapshot` or compatible newer build
- MultiPiston `1.2.51-1.21.1-snapshot` or compatible newer build

MineColonies and all listed required dependencies must be installed as normal mods; they are not shaded into the Imperium JAR.

## Installation for evaluation

1. Back up any world you intend to test.
2. Install the matching Minecraft/NeoForge versions and MineColonies plus all of its required dependencies.
3. Place the Imperium JAR in the instance's `mods` directory. For multiplayer, install compatible mod versions on the server and connecting clients as required by NeoForge.
4. Launch once, check the log for errors, then use a disposable world for testing.
5. Follow `PLAYTEST_RELEASE_CHECKLIST.md` for huts, hiring, worker behavior, level upgrades, save/reload, permissions, and multiplayer checks.

## Integrity

The accompanying `SHA256SUMS.txt` records the JAR SHA-256. From the directory containing the JAR and the checksum file, run:

```bash
sha256sum --check SHA256SUMS.txt
```

The CI build verifies this manifest before packaging and verifies the checksum again inside the release-candidate bundle.

## Cross-dimension command syntax

Use these commands to target real MineColonies colonies in another server dimension; existing permission, realm-membership and cost checks still apply:

- Diplomacy: `/imperium diplomacy improve-at <dimension> <colonyId>`
- Campaign: `/imperium campaign launch-at <type> <dimension> <colonyId>`
- Governor: `/imperium governor appoint-at <dimension> <colonyId> <player>` and `/imperium governor dismiss-at <dimension> <colonyId>`
- Supply route: `/imperium empire route build-at <dimension> <colonyId>`
- Defensive order: `/imperium empire defense-at <dimension> <colonyId>`
- Separatist petition: `/imperium empire resolve-at <dimension> <colonyId> <reassure|crackdown>`

Use namespaced dimension IDs such as `minecraft:the_nether`. The server resolves targets through MineColonies' public API and verifies the stored dimension + colony ID.

## Automated checks

The GitHub Actions workflow runs EN/NL translation/placeholder validation, blueprint progression, dependency metadata validation, cross-dimension command/help validation, `./gradlew --no-daemon test build`, JAR checksum verification, a headless `runClient` startup check and dedicated-server validation of MineColonies registries/all ten Structurize blueprints. The latest full green workflow is [run 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747) on commit `d5dc286d2e3e1e1b4ac30b5c7d6af48810fd0952`. It includes the command-registration cleanup, namespaced dimension parsing, monotonic realm-ID recovery, bounded SavedData history, and newest-invitation retention regression test.

Additional integrity checks cover atomic provincial ↔ imperial treasury transfers: successful transfers update both balances and the audit trail; insufficient-funds or destination-cap failures leave balances unchanged; a save/load cycle preserves the resulting balances and audit record. A 100-step alternating transfer test checks conservation of crowns, while invalid amounts/day indexes and non-member province transfers are rejected without changing balances or audit history. Daily imperial tax remittance tests cover the 10% central transfer of existing receipts, audit/persistence, rejection without an enacted realm tax law or valid day index, and rollback for insufficient provincial funds or a full central reserve.

These automated checks are not a substitute for a hands-on survival-world or two-player dedicated-server test.

## Outstanding release gates

- Confirm both huts build and upgrade at levels 1–5 in a real survival world.
- Verify worker hiring, production, specialist training, and save/reload in-game.
- Click through the Imperial Theatre and the ledger in a real client.
- Perform a two-player test of permissions, cross-dimension command variants, treasury concurrency, reconnect and save/reload.
- Decide the project's distribution license and review the final package/license obligations.

The project currently uses `All Rights Reserved` metadata for its own code and has no selected redistribution license. Do not treat this candidate as a license-cleared final release or redistribute it as a finished release before those decisions and tests are complete.
