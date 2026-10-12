# Status

Last updated: 2026-10-12 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x, required MineColonies dependency set.
- Latest fully verified source commit: `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22` (live command authorization GameTest added; all CI jobs green).
- Overall completion estimate: approximately **90%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Automated verification — green

The latest full workflow, [run 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437), passed all four jobs on source commit `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22`.

- English and Dutch localization: 298 literal/dynamic translation keys and formatting placeholders passed.
- Static validators passed for generated tier progression, dependency/release metadata, all seven cross-dimension command variants, and BlockUI ledger wiring: 40 buttons, 38 command handlers and two dedicated actions, with no dangling UI commands.
- `./gradlew --no-daemon test build` passed.
- JAR SHA-256 generation/verification and the self-checking release-candidate ZIP passed.
- Headless `runClient` reached the `Sound engine started` marker.
- Dedicated-server startup validated MineColonies registrations (`jobs=6 buildings=2 guardTypes=3 modules=6`) and loaded all **10/10** Structurize blueprints with valid dimensions and hut anchors.
- Five live Minecraft GameTests passed:
  1. SavedData/economy/realm/law/tax idempotency, work cooldown and treasury/audit round-trip.
  2. Creation/discovery of a real temporary MineColonies colony, colony rename, identity preservation and Imperium SavedData NBT round-trip.
  3. Real Imperial Archive block/building registration, three actual worker job assignments and level 1–5 worker-cap validation.
  4. Real Imperial Guard Tower block/building registration, all three military specialist assignments and level 1–5 shared guard-cap validation.
  5. Real Brigadier command dispatch: the colony owner can submit a tax bill, a non-owner without `MANAGE_HUTS` cannot submit tax or policy bills, and denied commands do not mutate parliamentary proposals.

Latest candidate JAR SHA-256: `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`.

Artifacts from the same fully green run:
- [Self-checking 0.1.0-rc1 bundle](https://github.com/syllater/minecolonies-history/https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763166421)
- [Mod JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762818347)
- [Live GameTest log](https://github.com/syllater/minecolonies-history/https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763480400)
- [Dedicated-server schematic/registry log](https://github.com/syllater/minecolonies-history/https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762294179)
- [Client startup log](https://github.com/syllater/minecolonies-history/https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762678784)

Automated coverage now includes real temporary MineColonies colonies, custom hut/job registration and BlockUI command wiring. It still does **not** replace a survival-world playtest of full Structurize build/upgrade placement, visible tier differences, sustained worker behavior, saved-world reload, GUI click-through or a genuine two-player dedicated-server session.


## Implemented systems

- Real MineColonies colony lookup and Imperium-owned, versioned SavedData, including safe defaults, schema migration and malformed-record isolation.
- Imperial Archive and Imperial Guard Tower, each with five tiers and ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat jobs; Siege Engineer, Imperial Field Medic and Imperial Cavalier specialist roles.
- Tier-scaled worker/guard capacity, worker production and guard training, persisted per-citizen work cooldowns.
- Treasury, taxes, economy policies, knowledge investment and provincial focus/development; provincial-to-imperial deposits and withdrawals now run as atomic server-side transactions with preflight checks, rollback guards and success-only audit entries.
- Parliament proposals, faction voting, Emperor assent/veto, legitimacy, stability, unrest, strikes, revolts and separatist petitions.
- Diplomacy, cross-dimension multi-colony realms, invitations, shared treasury/laws, audit history, governors and regional events.
- Military campaigns, postures, supply routes, defensive orders, strategic-priority actions and allied-province targeting protection.
- BlockUI Imperial Ledger and server-authoritative Imperial Theatre with province selection, pan/zoom/recenter and a validated dimension selector.
- Cross-dimension strategy commands for diplomacy, campaigns, governor appointment/dismissal, supply routes, defensive orders and separatist petitions, all resolved by server-side MineColonies identity.
- Bounded/rate-limited map payloads, English/Dutch help and commands, automated regression tests and CI artifact checksum verification.

## Remaining final-release gates

These require a person to interact with a real Minecraft world; headless CI cannot honestly satisfy them:

1. Build both huts in a survival world, hire the relevant workers/guards, upgrade levels 1–5, and confirm physical placement plus save/reload.
2. Observe real worker behavior, tax/economy outputs and specialist training at each tier.
3. Open the BlockUI ledger and interact with the graphical Imperial Theatre for a realm with multiple provinces, including a dimension containing an unloaded province.
4. Run a two-player dedicated-server session covering realm invitations, permissions, cross-dimension commands, concurrent treasury operations, reconnect and save/reload.
5. Review balance/usability, finalize release notes and decide the distribution license before publishing a final package.

## Build efficiency

- Updated `.github/workflows/verify.yml` so feature-branch commits are verified by the pull-request workflow instead of triggering a duplicate push workflow; pushes to `main` and manual workflow dispatch remain supported.

## Deferred, non-blocking maintenance

- The pinned NeoForge line emits deprecation warnings for `EventBusSubscriber.bus()`; migrate before the next platform upgrade.
- Gradle and some GitHub Actions components emit future-compatibility warnings. They do not currently prevent a successful candidate build or startup.
- These warnings are intentionally deferred in favor of the core release acceptance work.

## Review links

- [Draft release-candidate PR #2](https://github.com/syllater/minecolonies-history/pull/2)
- [Latest full green verification workflow](https://github.com/syllater/minecolonies-history/actions/runs/38227463363)
- [End-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md)
- [Release-candidate notes](docs/RELEASE_CANDIDATE_NOTES.md)


## 2026-10-12 — Real hut and worker integration verified

- Added a supported NeoForge `BlockEntityTypeAddBlocksEvent` integration so both Imperium hut blocks are accepted by MineColonies' shared `minecolonies:colonybuilding` entity type. This fixes the actual invalid-block-entity failure observed when placing the custom Archive hut.
- Added a live GameTest that creates a real temporary MineColonies colony and places the Imperial Archive anchor. It validates the registered building entry, assigns Philosopher, Tax Collector and Diplomat jobs, and checks the level-one and level-five capacity rules.
- Added a live GameTest for the real Imperial Guard Tower. It validates registered building data, assigns Siege Engineer, Imperial Field Medic and Cavalier jobs, and verifies the shared one-guard/five-guard capacity at levels 1 and 5.
- Real-colony discovery, rename preservation, identity handling and Imperium SavedData NBT round-trip are tested in a separate live GameTest.
- [Workflow 38226482725](https://github.com/syllater/minecolonies-history/actions/runs/38226482725) passed all four CI jobs, including `./gradlew --no-daemon test build`, JAR checksum/RC packaging, headless client startup, dedicated server/10-of-10 blueprint validation and all four GameTests.
- The resulting `0.1.0-rc1` JAR SHA-256 is `31c771467f7787fe8fa75ff4489aa8d87874dd5057b35f3f3d1c1f71fd514b12`.
- The final-release blockers are now the genuinely manual product-acceptance tasks: build/upgrade both schematics in survival, observe sustained worker behavior and reload a saved world, click through the GUI, test two-player permissions/concurrency/reconnect, review balance and select a distribution license.


## 2026-10-12 — BlockUI ledger command wiring verified

- Added `tools/validate_ledger_controls.py` to validate that each XML button has exactly one handler, each handler maps to an XML button, and every fixed command path refers to registered command literals.
- The validator passed in CI: 40 buttons, 38 command handlers and two dedicated actions; no missing or unused handler and no dangling command path.
- [Full green four-job workflow 38227463363](https://github.com/syllater/minecolonies-history/actions/runs/38227463363) includes this validator, Gradle build/tests, checksum/RC packaging, client startup, dedicated-server/10-blueprint validation and all four live GameTests.
- Candidate JAR SHA-256: `0c734afd29c2aed195f58169f2ba3e00988472ea17d39d88447d55224fd4b379`.


## 2026-10-12 — Reproducible release candidate verified

- Source commit: `61d7f9c9cb66fca8d9f96f9773878b6b196c43be`.
- Full workflow [38228182714](https://github.com/syllater/minecolonies-history/actions/runs/38228182714) passed all four jobs: static/build checks, client smoke, live GameTests, and dedicated-server/blueprint smoke.
- JAR build, SHA-256 manifest check and self-checking release-candidate package all passed.
- Current candidate JAR SHA-256: `4d44a38535747159a93d22ca46744852931d1929e3dfefc02c37747333685bdb`.
- Current artifacts: [release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737131416), [JAR and checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737226311), [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737920417), [dedicated-server log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737576125), [client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38228182714/artifacts/11737760664).
- Final public release remains blocked only by the uncompleted hands-on survival GUI/world-save and two-player acceptance checklist, balance review, and explicit distribution-license decision. These have not been falsely marked complete.


## 2026-10-12 — Command authorization regression test verified

- Source commit: `e3e4e7c541ffc0de78daf1a3d8b3483db28d2b22`.
- Full verification [workflow 38285019437](https://github.com/syllater/minecolonies-history/actions/runs/38285019437) passed all four jobs.
- A fifth live GameTest executes commands through the real Brigadier dispatcher: the colony owner can submit a tax bill; a non-owner in the same colony is denied tax and policy changes; denied commands leave the proposal list unchanged.
- Static validators, `./gradlew test build`, JAR checksum validation, RC packaging, headless client startup and dedicated-server/10-blueprint checks all passed.
- Current JAR SHA-256: `4c4720cadf82f82e944f67134bab8d11ac8d0f74b1056653e274a1868482fbd0`.
- Current artifacts: [RC bundle](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763166421), [JAR + checksum](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762818347), [GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11763480400), [server log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762294179), [client log](https://github.com/syllater/minecolonies-history/actions/runs/38285019437/artifacts/11762678784).
- Overall completion remains approximately 90%; hands-on survival, GUI, genuine multiplayer, balance and licensing gates remain open.
