# Checkpoints

## 2026-10-11 — Treasury conservation and membership guards (fully verified)

### Change
- Added a deterministic 100-step alternating transfer test (province ⇄ imperial reserve) that asserts the combined total never changes, balances stay within limits, only successful transfers append audit entries, and the post-transfer SavedData round trip preserves both balances and audit history.
- Added direct coverage that zero/negative amounts, negative day indices and provinces outside a realm are rejected without moving crowns or writing audit entries.
- Existing transfer tests continue to cover insufficient source funds, full destination treasuries and success-only audit history.

### Verification
- Source commit: `fe9284d0faa098b27cb02b1cf904ab0d38af3f7d`
- Full green Actions run: https://github.com/syllater/minecolonies-history/actions/runs/38158411131
- English/Dutch translations, generated tier progression, release/dependency metadata and cross-dimension command contracts passed.
- `./gradlew --no-daemon test build`, JAR checksum verification and self-checking RC packaging passed.
- Headless client startup passed.
- Dedicated-server startup and all 10/10 Structurize blueprint/anchor validations passed.

Artifacts:
- [Release candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38158411131/artifacts/11709477514)
- [JAR + SHA-256 manifest](https://github.com/syllater/minecolonies-history/actions/runs/38158411131/artifacts/11709796642)
- [Dedicated-server validation log](https://github.com/syllater/minecolonies-history/actions/runs/38158411131/artifacts/11709226976)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38158411131/artifacts/11708867734)

### Release gates still open
- Physical hut placement/upgrades and worker behavior in a real survival world.
- Interactive GUI click-through in a real client.
- Two-player permission/concurrency/reconnect/save-reload testing.
- Final balance/package review and an explicit distribution-license decision.

## 2026-10-11 — Atomic treasury transfers and stricter identity validation (fully verified)

### Change
- Added server-side transfer operations to `EmpireStateSavedData` for moving crowns from a provincial treasury to the imperial reserve and back.
- Preflight checks validate positive amount, province/realm membership, source balance and destination capacity before mutating either balance.
- Defensive rollback guards preserve money if the second balance mutation unexpectedly fails.
- Audit entries are added only after successful transfers; failed transactions leave balances and audit history untouched.
- Command handlers now route through the transaction service instead of implementing debit/credit/rollback separately.
- Added EN/NL fallback text for the unexpected transaction-failure path.
- Extended unit tests for success, insufficient local/imperial funds, full reserve/province caps, audit behavior and treasury/audit NBT round trip.
- Strengthened dimension identity parsing; malformed SavedData records with empty paths, invalid characters or non-canonical IDs are isolated without discarding neighboring valid records.

### Verification
- Source commit: `a22d79f64c4a41601feaf7608f12d86950b75698`
- Full GitHub Actions run: https://github.com/syllater/minecolonies-history/actions/runs/38157810821
- Translation, blueprint, release metadata and cross-dimension command validators passed.
- `./gradlew --no-daemon test build` passed.
- JAR checksum and self-checking RC bundle validation passed.
- Headless client startup passed.
- Dedicated-server startup/blueprint validation passed, including all 10/10 Structurize schematics.

Artifacts:
- [Release candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709157384)
- [Mod JAR + SHA-256 manifest](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709232110)
- [Dedicated-server validation log](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709785938)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709161486)

### Still pending
- Real survival-world hut building/hiring/upgrades and in-world save/reload.
- Interactive GUI click-through with a real realm.
- Real two-player permissions, concurrent treasury actions, reconnect and multiplayer save/reload.
- Distribution-license decision and final release approval.

## 2026-10-10 — Five-tier worker capacity and blueprint progression validation

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Latest fully verified commit
Commit `6ceed484e4ca0d567e9b81555351eae1abadd422` passed the complete GitHub Actions workflow:
- 265 literal/dynamic EN/NL localization keys validated, including placeholder indices/types.
- Gradle compile, tests and JAR upload passed.
- Headless client startup smoke test passed.
- Dedicated server startup and all 10 Structurize schematic checks passed.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075771050

### New verified functionality
- `ImperialBuildingProgression.workerLimitForLevel`: level 0/negative -> 0; level 1–5 -> one through five; over-cap input -> five.
- All six Archive/Guard Tower worker modules use that shared rule.
- Unit tests cover valid tiers, unbuilt/negative levels and values above five.
- Existing tests cover NBT persistence, strategic map geometry, route priority and defensive orders.

### Added in this commit, pending CI
- `tools/validate_blueprint_progression.py` generates test payloads from the original generator and checks five unique tiers per building family, non-shrinking dimensions with at least one dimension increasing per tier, and valid hut anchor offsets.
- `.github/workflows/verify.yml` runs the new validation before the Gradle build.
- The new validator still needs its own green workflow result.

### Still pending
- Actual in-world building placement, worker hiring, level 1–5 upgrades and save/reload.
- Actual two-player permissions/economy tests.
- The map remains a chat grid rather than a full-screen interactive theatre map.


## 2026-10-11 — Pan/Zoom Strategic Map Viewport (CI pending at edit time)

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Change
- Added `StrategicMapGrid.renderViewport` for a stable world-coordinate grid where X increases east and lower Z is north.
- Added `isVisibleInViewport` so command output lists only visible map markers and reports off-screen same-dimension provinces with their coordinates.
- Extended `/imperium empire map` with optional `<centerX> <centerZ> <blocksPerCell>` arguments.
- Updated English and Dutch translations and README/acceptance instructions.
- Added unit tests for direction, scale, panning, off-screen filtering and invalid viewport dimensions/scale.

### How to use
- `/imperium empire map`: auto-fitted overview.
- `/imperium empire map 0 0 128`: stable viewport around world coordinate X=0, Z=0, where one cell represents 128 blocks.
- Change the center X/Z to pan. Smaller scale values zoom in; larger values zoom out.

### Verification state
- Last previously fully verified commit: `bce773194c56d70cc3a659287dd7d83e438bd3af`, including localization, blueprint progression validation, Gradle tests/build, client startup and server blueprint checks.
- This viewport implementation is not considered verified until its own GitHub Actions run passes.
- This feature remains a text/chat map, not a graphical screen. Real survival-world and two-player acceptance remain pending.


## 2026-10-11 — Interactive Imperial Theatre GUI (verified by CI)

### Implementation
- Added bounded custom network payloads for a strategic map request and server-produced realm snapshot.
- Server resolves the requesting player to their actual MineColonies colony, then resolves that colony's realm; the client does not provide an empire or province ID.
- Added `ImperialMapScreen`, opened from the BlockUI ledger's Empire Map button.
- GUI supports arrow-key panning, mouse-wheel and button zoom, recentering with R, province selection and display of province stability, unrest and loyalty.
- Map payloads cap the number of provinces at 64 and normalize field lengths and metric values.
- Isolated the client receiver through a client-only callback so dedicated servers do not link the new screen class.

### Verification
- Commit: `bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119451312
- Localization check, blueprint progression validation, Gradle build/tests, client-start smoke and dedicated-server schematic validation all passed.
- Artifact uploaded: `imperium-realms-bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73`.

### Still pending
- A human must open the BlockUI ledger and interact with the map in a real survival world.
- Save/reload and two-player realm-permission tests remain required before release.


## 2026-10-11 — Five-tier architectural detail progression (verified by CI)

### Implemented
- Extended the deterministic blueprint palette with oriented vanilla stair states, slabs, lanterns, bars, fences and chiseled masonry.
- Tier 2+ adds oriented roof eaves.
- Archive levels 3–5 add a ridge and progressively more shelving; levels 4–5 add lantern details; level 5 adds raised rooftop ornaments.
- Guard Tower levels 3–5 add defensive barred window courses, structural corner piers, battlements/fence trim and tier-5 lanterns.
- Higher levels also add extra window courses and materials rather than relying solely on exterior dimensions.
- The generator now raises an error if expected tier-specific visual details are absent; the progression validator exercises all 10 tiers.

### Verification
- Commit: `139cfd5b70eda962c6c2e830d6e18049b77ef833`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119918288
- English/Dutch keys, visual blueprint progression, Gradle tests/build/JAR, headless client startup and dedicated-server validation of all 10 schematics passed.

### Still pending
- Inspect the actual rendered schematics placed in Minecraft to tune proportions/material choices.
- Verify in-world construction, upgrade placement and save/reload; automated NBT/schema validation cannot replace that playtest.


## 2026-10-11 — Tier-scaled Archive economy and Guard Tower training (verified by CI)

### Implementation
- Philosopher production uses the actual Archive level: tier 1–2 grants 1 knowledge point, tier 3–4 grants 2, and tier 5 grants 3 per successful 1,200-tick work interval.
- Tax Collector efficiency uses the same 1/2/3 tier brackets, still capped at 25 percent and gated by the existing work interval.
- Diplomat influence uses the same 1/2/3 tier brackets, with the existing influence cap and work interval.
- Guard level-ups grant tier-based military training: tiers 1–2 add 1, tiers 3–4 add 2 and tier 5 adds 3 points to the corresponding discipline; Military province focus doubles the gain.
- Siege Engineer, Field Medic and Cavalier pass the current Guard Tower level to the server-side training recorder.
- The old no-tier method signatures remain as tier-1 compatibility overloads. Unbuilt huts return no work yield; discipline-specific and economy caps still apply.
- Unit tests cover tiers 1, 3 and 5, level-zero rejection and Military-focus multiplication.

### Verification
- Commit: `0030bd873678a812ee821601002535f58c61878c`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38120356669
- Localization validation, blueprint progression validation, Gradle build/tests and JAR upload passed.
- Headless client startup passed.
- Dedicated-server startup and all ten Structurize schematic checks passed.
- Artifact uploaded: `imperium-realms-0030bd873678a812ee821601002535f58c61878c`.

### Still pending
- Human in-world test of worker behavior, actual tier upgrades, save/reload and multiplayer permissions; CI startup is not a substitute.


## 2026-10-11 — Tier-scaled economy and military persistence test (verified)

### Regression coverage
- Added an `EmpireStateSavedDataTest` NBT round-trip that records tier-5 Philosopher, Tax Collector and Diplomat output plus tier-5 Field Medic training under Military province focus.
- Confirms knowledge points, collection efficiency, diplomatic influence, discipline points, province focus and work interval timestamps survive serialization/deserialization.
- Confirms loading does not permit the same game tick to generate duplicate work rewards.
- Updated the test and project status to reflect the verified tier-scaled behavior.

### Verification
- Commit: `a647f1a9876aa83c386a023b5a2da078c06785be`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38120649287
- Translation checks, blueprint progression validation, Gradle build/unit tests, JAR upload, client startup and dedicated-server validation of all ten Structurize schematics passed.
- Artifact: `imperium-realms-a647f1a9876aa83c386a023b5a2da078c06785be`.

### Still pending
- Human survival-world testing and two-player permission/concurrency/reconnect acceptance; automated startup and unit tests do not exercise a real multiplayer world.


## 2026-10-11 — Per-citizen Archive production (verified before cooldown-bound test)

### Change
- Philosopher, Tax Collector and Diplomat work now uses the worker UUID as a cooldown key.
- Different workers can contribute within the same colony work interval; repeated work callbacks from the same citizen are throttled.
- Per-citizen cooldown timestamps are serialized in three SavedData lists and restored independently on world load.
- Existing saves without the new lists use their old colony-wide timestamps as a one-time migration safety guard.
- Expired entries are pruned and stored cooldown history is bounded at 64 worker records per profession.
- Incremented the SavedData schema version to 22.
- Added unit coverage for per-worker output, same-worker duplicate blocking, a worker becoming due again after the interval and the bounded cooldown map.

### Previously green verification
- Commit: `eb0b9a9d4011e5abb63de424d76655f0395e8694`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38121078239
- Translation validation, blueprint validation, Gradle build/tests/JAR, client start and dedicated-server validation all passed.
- The schema bump and cooldown-cap test in the current commit are pending their own CI run.


## 2026-10-11 — Verified Build and Release Gate Update

- Verified code commit: `117da4d4b97693902d90dd2b490d5a18cd4c8b01`.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38121255613.
- Translation validation, blueprint tier validation, Gradle tests/build and JAR upload passed.
- Headless client startup and dedicated-server startup with all ten generated Structurize schematics passed.
- Latest code includes bounded persisted per-citizen Archive cooldown history and save-schema version 22.
- Estimated overall completion: ~75% as a planning estimate, not a measured metric.
- Still pending: survival-world construction/hiring/upgrades/save-reload, real graphical map playtest, dedicated-server two-player permission/concurrency/reconnect tests, balance review, release notes/package review and distribution-license decision.
- JAR artifact name: `imperium-realms-117da4d4b97693902d90dd2b490d5a18cd4c8b01` (temporary CI artifact; regenerate when expired).


## 2026-10-11 — Cross-dimension management and Imperial Theatre view

### Changes

- Added cross-dimension commands for supply routes, defensive orders and governor appointment/dismissal.
- Added a map dimension selector that cycles through dimensions represented by provinces in the player's current realm.
- Changed the map request payload to carry only a bounded display-dimension preference.
- Server-side dimension selection checks the realm's own province list; invalid/unrelated dimensions default to the capital dimension.
- Added unit tests for dimension request bounds and server-side map-dimension allowlisting.
- Updated English and Dutch translations and the end-to-end playtest checklist.

### Verification evidence

The complete CI workflow passed on code commit `12a748d92b8cc648ce3b954a0d3b801d107c9e48`:
- [Build, tests, client smoke and dedicated-server blueprint smoke](https://github.com/syllater/minecolonies-history/actions/runs/38132956614)
- [Built mod JAR artifact](https://github.com/syllater/minecolonies-history/actions/runs/38132956614/artifacts/11700370329)

This verifies compilation, unit tests, startup and loading of the ten generated schematics. It does not verify in-world placement/upgrades or two-player multiplayer interaction.

### Remaining acceptance gates

Follow `docs/PLAYTEST_RELEASE_CHECKLIST.md` for the real-world hut upgrade/save-load, interactive Theatre map, two-player permission/concurrency, balance, final packaging and license checks.


## 2026-10-11 — Map snapshot rate limiting and elapsed-time cleanup (verified)

### Change

- Added server-side per-player map snapshot cooldown of 10 overworld ticks (half a second at normal game speed).
- Uses the authenticated server player UUID and the shared overworld clock, so switching dimensions does not bypass the cooldown.
- Stale entries expire after 24,000 ticks. Housekeeping now runs when the elapsed cleanup deadline passes rather than only when a client sends a request at an exact multiple of 200 ticks.
- A backwards-moving world clock clears stale session entries, covering an integrated-server restart in the same development JVM.
- Added unit coverage for spam blocking, player isolation, stale entry pruning when the request tick is not an exact interval boundary, and clock reset handling.
- Updated README and changelog with the current map and cross-dimension administration behavior.

### Verification

The complete GitHub Actions workflow passed on code commit `329c6ccde562bcfd8fb9b18c2cb372373e0d0c43`:
- [Build, tests, client startup and dedicated-server blueprint smoke test](https://github.com/syllater/minecolonies-history/actions/runs/38133512797)
- [Built mod JAR artifact](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700226575)
- [Dedicated-server schematic log](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700405519)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700361099)

All workflow jobs passed, including translation/placeholder validation, visual blueprint progression, Gradle unit tests/build, headless client start and dedicated-server validation of 10/10 schematics.

### Still pending

This does not substitute for the required manual survival-world, GUI click-through or two-player dedicated-server tests listed in `docs/PLAYTEST_RELEASE_CHECKLIST.md`.


## 2026-10-11 — Verified checksum-gated JAR packaging (full CI green)

### Build pipeline

- Added a `Generate JAR SHA-256 manifest` step after the Gradle build.
- Added a `Verify generated JAR checksum manifest` step that runs `sha256sum --check SHA256SUMS.txt`.
- The JAR bundle is uploaded only if manifest generation and verification pass; the artifact contains both the JAR and `SHA256SUMS.txt`.
- Updated README and changelog instructions accordingly.

### Verification

- Branch head: `eb6b790ebf5f58ef6ea52a36ae7ba5ca33a8f4ab`
- [Full CI workflow](https://github.com/syllater/minecolonies-history/actions/runs/38133948650)
- [JAR + checksum manifest bundle](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700525650)
- Artifact ZIP SHA-256: `0eb6d4c0393ffa072ad10ae711d4860161fd9565b2bca7e8cfa5f9eccb327fd3`
- [Dedicated-server schematic validation log](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700520830)
- [Client startup smoke log](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700421184)

The run passed translation validation, blueprint progression validation, Gradle build/tests, JAR checksum verification, client startup and dedicated-server startup with 10/10 generated schematics.

### Known non-blocking warnings

The compiler currently reports the pinned NeoForge line's deprecation warnings for `EventBusSubscriber.bus()`; Gradle/GitHub Actions also report future-compatibility warnings. They do not affect this successful build and should be handled before the next platform/toolchain upgrade.

### Remaining acceptance

Actual survival-world building/hiring/upgrade/save-reload testing, real interactive GUI usage, two-player multiplayer permissions/concurrency/reconnect tests, balance review and the distribution-license decision remain open.


## 2026-10-11 — 0.1.0-rc1 package assembled and CI green

### Branch and candidate

- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Candidate source head: `e9a58c1a071cc54178a806c855726ed60c533ce2`
- Review PR: [#2 — WIP: Imperium European Realms 0.1.0-rc1](https://github.com/syllater/minecolonies-history/pull/2)

### Verification evidence

The full workflow passed at [run 38154793458](https://github.com/syllater/minecolonies-history/actions/runs/38154793458), including:
- EN/NL translation coverage validation.
- Generated blueprint progression validation.
- Release/dependency metadata validation.
- `./gradlew --no-daemon test build`.
- JAR checksum manifest generation and verification.
- Deterministic RC bundle packaging and embedded JAR checksum verification.
- Headless client startup smoke.
- Dedicated server registry/blueprint smoke: 10/10 Structurize schematics with valid dimensions/hut anchors.

Artifacts:
- [RC bundle](https://github.com/syllater/minecolonies-history/actions/runs/38154793458/artifacts/11708051777)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38154793458/artifacts/11708256448)
- [Server validation log](https://github.com/syllater/minecolonies-history/actions/runs/38154793458/artifacts/11708471103)
- [Client smoke log](https://github.com/syllater/minecolonies-history/actions/runs/38154793458/artifacts/11707259036)

### New RC packaging

- Added `docs/RELEASE_CANDIDATE_NOTES.md`.
- Added `tools/package_release_candidate.py`, which validates the RC version, required documents, JAR filename, manifest digest and complete package contents. It creates a deterministic ZIP, checks ZIP integrity and verifies that the bundled JAR matches its bundled checksum.
- Added CI steps to assemble the candidate bundle after checksum verification and upload it as a separate 30-day artifact.
- Added a release metadata validator for dependency declarations and stale milestone versions.
- Added regression tests for legacy SavedData with missing fields and malformed colony records.

### Still not certified by automation

- Actual survival-world placement/upgrades and worker hiring/work/save-reload.
- In-client interaction with the Imperial Theatre.
- Real two-player realm permissions, concurrent treasury operations, reconnect and multiplayer save/reload.
- Explicit distribution-license choice and final release package/legal review.

Do not promote `0.1.0-rc1` to a final release until these gates are completed.


## 2026-10-11 — Cross-dimension strategy commands and RC1 workflow green

### Implementation
- Added dimension-aware variants while preserving dimension-local syntax:
  - `/imperium diplomacy improve-at <dimension> <colonyId>`
  - `/imperium campaign launch-at <type> <dimension> <colonyId>`
  - `/imperium governor appoint-at <dimension> <colonyId> <player>`
  - `/imperium governor dismiss-at <dimension> <colonyId>`
  - `/imperium empire route build-at <dimension> <colonyId>`
  - `/imperium empire defense-at <dimension> <colonyId>`
  - `/imperium empire resolve-at <dimension> <colonyId> <reassure|crackdown>`
- Shared target resolver validates the namespaced dimension, looks up a real colony through MineColonies' public API, and verifies dimension + colony ID identity.
- Existing permission, realm membership, capital-protection and resource-cost checks remain in the common handler paths.
- Updated English/Dutch help messages and added `tools/validate_cross_dimension_commands.py` so command registrations and help text cannot silently drift apart.

### Verification evidence
The full workflow passed at [run 38155865389](https://github.com/syllater/minecolonies-history/actions/runs/38155865389):
- EN/NL translation/placeholder validation.
- Blueprint tier progression validation.
- Dependency/release metadata validation.
- Cross-dimension command and localized help contract validation.
- `./gradlew --no-daemon test build`.
- JAR SHA-256 and self-checking RC bundle validation.
- Headless client startup and dedicated-server startup.
- Dedicated-server validation of MineColonies registration and all 10/10 Structurize blueprints.

Artifacts:
- [RC bundle](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11707704528)
- [JAR + checksum manifest](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708735039)
- [Server validation log](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708552679)
- [Client smoke log](https://github.com/syllater/minecolonies-history/actions/runs/38155865389/artifacts/11708263500)

### Still not certified by automation
- Actual in-world hut placement/upgrades and worker hiring/work/save-reload.
- In-client click-through of the Imperial Theatre and new cross-dimension command variants.
- Real two-player permissions, treasury concurrency, reconnect and multiplayer save/reload.
- Explicit distribution-license choice and final package/legal review.

Do not promote `0.1.0-rc1` to a final release until these gates are completed.
