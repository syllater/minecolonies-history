# Imperium: European Realms — 0.1.0-rc1

This is a release candidate for evaluation, not a final public release.

## Target environment
- Minecraft Java Edition 1.21.1
- Java 21
- NeoForge 21.1.x
- MineColonies `1.1.1403-1.21.1`
- Structurize `1.0.832-1.21.1-snapshot` or a compatible newer 1.21.1 build
- BlockUI `1.0.199-1.21.1-snapshot` or a compatible newer 1.21.1 build
- Domum Ornamentum `1.0.223-snapshot` or a compatible newer 1.21.1 build
- MultiPiston `1.2.51-1.21.1-snapshot` or a compatible newer 1.21.1 build

MineColonies and its required dependencies must be installed as normal mods; they are not shaded into the Imperium JAR.

## Install for evaluation
1. Back up any world you intend to test.
2. Install the matching Minecraft/NeoForge versions and MineColonies plus its required dependencies.
3. Place the Imperium JAR in the instance's `mods` directory. For multiplayer, use matching server/client mod versions as required by NeoForge.
4. Launch once, inspect the log, then use a disposable world for testing.
5. Follow [the playtest/release checklist](PLAYTEST_RELEASE_CHECKLIST.md) for hut construction/upgrades, hiring, worker behavior, save/reload, permissions and multiplayer.

## Integrity
The bundle contains the JAR and `SHA256SUMS.txt`. From the folder containing them, run:

```bash
sha256sum --check SHA256SUMS.txt
```

CI generates and checks the manifest before packaging, then validates the self-checking release-candidate ZIP.

## Cross-dimension commands
All target real MineColonies colonies by namespaced dimension and colony ID. Existing permission, realm-membership and cost checks remain server-side:
- Diplomacy: `/imperium diplomacy improve-at <dimension> <colonyId>`
- Campaign: `/imperium campaign launch-at <type> <dimension> <colonyId>`
- Governor: `/imperium governor appoint-at <dimension> <colonyId> <player>` and `/imperium governor dismiss-at <dimension> <colonyId>`
- Supply route: `/imperium empire route build-at <dimension> <colonyId>`
- Defensive order: `/imperium empire defense-at <dimension> <colonyId>`
- Separatist petition: `/imperium empire resolve-at <dimension> <colonyId> <reassure|crackdown>`

## Latest verified candidate — 2026-10-12

All four jobs passed in [workflow 38296167316](https://github.com/syllater/minecolonies-history/actions/runs/38296167316) on source commit `a2bf703628c25379dd1c4f4acb1a8aa1e3dc457a`:
- EN/NL localization (298 keys/placeholders), blueprint progression, dependency/release metadata, seven cross-dimension command variants and BlockUI ledger wiring (40 buttons, 38 handlers, two dedicated actions).
- `./gradlew --no-daemon test build`, JAR SHA-256 verification and reproducible self-checking RC package.
- Headless `runClient` reached `Sound engine started`.
- Dedicated server verified `jobs=6 buildings=2 guardTypes=3 modules=6` and all `10/10` Structurize schematics.
- Six live Minecraft GameTests passed: economy/tax/profession persistence; real colony discovery; Archive registration/jobs/capacity; Guard Tower specialists/capacity; owner/non-owner treasury and parliamentary authorization with rollback; and cross-dimension invitation/join/persistence.

JAR SHA-256:
```text
351eeafb48e8e6bea538a701c55ae24f10b806df2a3ed15c8979c7701d13bebe
```

Artifacts:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768472386)
- [Mod JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768327658)
- [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11767998216)
- [Dedicated-server/blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768902441)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768358033)

## Final-release gate — still open

A green CI run is not a substitute for:
- Real survival construction/upgrades at levels 1–5, sustained worker/guard behavior and actual world save/reload.
- In-client click-through of the BlockUI Ledger and Imperial Theatre.
- Genuine two-player dedicated-server test for permissions, cross-dimension commands, treasury concurrency, reconnect and persistence.
- Gameplay balance/usability review and final release-note review.
- Explicit choice of a distribution license. The repository's own metadata is currently `All Rights Reserved`; redistribution clearance has not been decided. Do not treat this RC as license-cleared or final.
