# Status

Last updated: 2026-10-12 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x, required MineColonies dependency set.
- Latest fully verified source commit: `9d2cf82ca3a9a7cc909785e1b39b6dc57233b82c` (new GameTest structure/name fixes).
- Overall completion estimate: approximately **87%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Automated verification — green

The latest full workflow passed at [GitHub Actions run 38219495032](https://github.com/syllater/minecolonies-history/actions/runs/38219495032), against source commit `9d2cf82ca3a9a7cc909785e1b39b6dc57233b82c`. All four jobs passed: Gradle/unit/build and RC integrity, headless client startup, dedicated-server startup, and a live Minecraft GameTest.

- English/Dutch translation-key and placeholder validation passed.
- Generated blueprint tier-progression validation passed.
- Release metadata and required-dependency validation passed.
- Cross-dimension command/help-contract validation passed for all seven command variants; duplicate registrar and namespaced-dimension guards also passed.
- `./gradlew --no-daemon test build` passed, including saved realm-ID counter recovery, bounded malformed NBT/history, preserving the newest invitations when excess invitation records are loaded, atomic treasury transactions, tax-remittance invariants, and map/network regression tests.
- JAR SHA-256 generation/verification passed.
- The self-checking `0.1.0-rc1` bundle assembled and its embedded checksum verified.
- Headless `runClient` startup passed: Minecraft reached the `Sound engine started` marker.
- Dedicated-server startup passed; MineColonies registration validated and all **10/10** Structurize schematics/hut anchors loaded.
- The new `runGameTestServer` test passed inside Minecraft: state/NBT round-trip for synthetic colony identities, realm membership, imperial tax/policy law, treasury transfer/audit, and worker cooldown persistence. The test deliberately avoids creating or mutating a real MineColonies colony, so actual in-world colony playtesting remains open.

Artifact links:
- [Self-checking 0.1.0-rc1 release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38219495032/artifacts/11733657660)
- [Mod JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38219495032/artifacts/11733072967)
- [Dedicated-server blueprint/registry log](https://github.com/syllater/minecolonies-history/actions/runs/38219495032/artifacts/11732794716)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38219495032/artifacts/11733414413)

The latest fully verified candidate JAR SHA-256 is `5220c9fc9ce992e258086f2153d848f4dd18731582ad149c8f232194948f6299` (workflow 38219495032). The workflow artifact links are temporary GitHub Actions artifacts, not permanent release assets.

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
- [Latest full green verification workflow](https://github.com/syllater/minecolonies-history/actions/runs/38219495032)
- [End-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md)
- [Release-candidate notes](docs/RELEASE_CANDIDATE_NOTES.md)


## 2026-10-12 — Runtime GameTest and four-job verification checkpoint

- Saved realm IDs remain monotonic after save/load; a stale lower counter is reconciled against active realm IDs.
- Oversized saved membership, invitations, parliament/diplomacy/campaign history and audit lists are bounded.
- For oversized invitation lists, the loader now retains the latest supported invitations rather than the oldest entries; the regression test passed in the latest build.
- Cross-dimension commands use the namespaced resource-location parser, and the obsolete duplicate diplomacy/campaign registrar has been removed.
- [Workflow 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747) passed all three jobs: build/tests/checksum/RC package, headless client startup, and dedicated-server startup with 10/10 schematics.
- Current candidate is still `0.1.0-rc1`. A final release remains blocked on genuine survival-world and two-player acceptance, balance review, and the owner's distribution-license decision.
