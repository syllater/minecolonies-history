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

The GitHub Actions workflow runs EN/NL translation/placeholder validation, blueprint progression (including the generated GameTest fixture), dependency metadata validation, cross-dimension command/help validation, `./gradlew --no-daemon test build`, JAR checksum verification, a headless `runClient` startup check, dedicated-server validation of MineColonies registries/all ten Structurize blueprints, and a live `runGameTestServer` persistence test. The latest full green workflow is [run 38220155005](https://github.com/syllater/minecolonies-history/actions/runs/38220155005) on commit `0e96ba3ba809ad7e09735891593322b2514383fd`. All four jobs passed.

Additional integrity checks cover atomic provincial ↔ imperial treasury transfers: successful transfers update both balances and the audit trail; insufficient-funds or destination-cap failures leave balances unchanged; a save/load cycle preserves the resulting balances and audit record. A 100-step alternating transfer test checks conservation of crowns, while invalid amounts/day indexes and non-member province transfers are rejected without changing balances or audit history. Daily imperial tax remittance tests cover the 10% central transfer of existing receipts, audit/persistence, rejection without an enacted realm tax law or valid day index, and rollback for insufficient provincial funds or a full central reserve.

The live GameTests cover synthetic-state economy/realm persistence and also create real temporary MineColonies colonies. They test public API discovery, colony-state NBT round-trip, real Imperial Archive and Guard Tower block/building registration, actual custom job assignment and tier-scaled worker/guard capacity. They still do not replace a survival-world playtest of full Structurize construction/upgrades, sustained work, in-world save/reload, GUI interaction or genuine two-player permissions/concurrency/reconnect.

## Outstanding release gates

- Confirm both huts build and upgrade at levels 1–5 in a real survival world.
- Verify worker hiring, production, specialist training, and save/reload in-game.
- Click through the Imperial Theatre and the ledger in a real client.
- Perform a two-player test of permissions, cross-dimension command variants, treasury concurrency, reconnect and save/reload.
- Decide the project's distribution license and review the final package/license obligations.

The project currently uses `All Rights Reserved` metadata for its own code and has no selected redistribution license. Do not treat this candidate as a license-cleared final release or redistribute it as a finished release before those decisions and tests are complete.


## Latest verified candidate (2026-10-12)

All four jobs passed in [workflow 38227463363](https://github.com/syllater/minecolonies-history/actions/runs/38227463363) on source commit `fa50cc2d67ee890b061fea30cb27598c808a3ac9`:
- EN/NL localization validator: 298 literal/dynamic keys with matching format placeholders.
- Blueprint progression, release/dependency metadata, cross-dimension commands, and BlockUI ledger wiring validators passed. The ledger validator checked all 40 XML buttons, 38 command handlers and two dedicated actions, and found no dangling command paths.
- `./gradlew --no-daemon test build`, JAR SHA-256 verification and self-checking RC ZIP packaging passed.
- Headless client reached `Sound engine started`.
- Dedicated server validated `jobs=6 buildings=2 guardTypes=3 modules=6` and all `10/10` Structurize blueprints loaded.
- Four live Minecraft GameTests passed: daily tax/economy/realm persistence; discovery/serialization of a real MineColonies colony; Archive building registration, the three citizen jobs and tier-scaled hiring; Guard Tower registration, three military specialists and level-scaled shared capacity.

Current JAR SHA-256: `0c734afd29c2aed195f58169f2ba3e00988472ea17d39d88447d55224fd4b379`.

Artifacts:
- [Self-checking RC package](https://github.com/syllater/minecolonies-history/actions/runs/38227463363/artifacts/11736869886)
- [JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38227463363/artifacts/11737190134)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38227463363/artifacts/11737395115)
- [Dedicated-server validation log](https://github.com/syllater/minecolonies-history/actions/runs/38227463363/artifacts/11737195332)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38227463363/artifacts/11737150342)

This remains a release candidate, not a license-cleared final release. Real survival-world full blueprint construction/upgrades, sustained worker/guard behavior, in-world save/reload, actual GUI click-through, two-player permissions/concurrency/reconnect, balance review and the distribution-license decision remain open.
