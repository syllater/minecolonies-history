# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x, required MineColonies dependency set.
- Candidate source commit: `d5dc286d2e3e1e1b4ac30b5c7d6af48810fd0952`
- Overall completion estimate: approximately **86%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Automated verification — green

The latest full workflow passed at [GitHub Actions run 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747), against source commit `d5dc286d2e3e1e1b4ac30b5c7d6af48810fd0952`.

- English/Dutch translation-key and placeholder validation passed.
- Generated blueprint tier-progression validation passed.
- Release metadata and required-dependency validation passed.
- Cross-dimension command/help-contract validation passed for all seven command variants; duplicate registrar and namespaced-dimension guards also passed.
- `./gradlew --no-daemon test build` passed, including saved realm-ID counter recovery, bounded malformed NBT/history, preserving the newest invitations when excess invitation records are loaded, atomic treasury transactions, tax-remittance invariants, and map/network regression tests.
- JAR SHA-256 generation/verification passed.
- The self-checking `0.1.0-rc1` bundle assembled and its embedded checksum verified.
- Headless `runClient` startup passed: Minecraft reached the `Sound engine started` marker.
- Dedicated-server startup passed; MineColonies registration validated and all **10/10** Structurize schematics/hut anchors loaded.

Artifact links:
- [Self-checking 0.1.0-rc1 release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38162651747/artifacts/11710624257)
- [Mod JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38162651747/artifacts/11710779160)
- [Dedicated-server blueprint/registry log](https://github.com/syllater/minecolonies-history/actions/runs/38162651747/artifacts/11710389689)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38162651747/artifacts/11710722865)

The built JAR SHA-256 is `67ae860f656926b27a2fb30b2bf65d6e880a322629e44b0ce005060ca0e8fd26`. The workflow artifact links are temporary GitHub Actions artifacts, not permanent release assets.

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
- [Latest full green verification workflow](https://github.com/syllater/minecolonies-history/actions/runs/38162651747)
- [End-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md)
- [Release-candidate notes](docs/RELEASE_CANDIDATE_NOTES.md)


## 2026-10-11 — Latest full verification checkpoint

- Saved realm IDs remain monotonic after save/load; a stale lower counter is reconciled against active realm IDs.
- Oversized saved membership, invitations, parliament/diplomacy/campaign history and audit lists are bounded.
- For oversized invitation lists, the loader now retains the latest supported invitations rather than the oldest entries; the regression test passed in the latest build.
- Cross-dimension commands use the namespaced resource-location parser, and the obsolete duplicate diplomacy/campaign registrar has been removed.
- [Workflow 38162651747](https://github.com/syllater/minecolonies-history/actions/runs/38162651747) passed all three jobs: build/tests/checksum/RC package, headless client startup, and dedicated-server startup with 10/10 schematics.
- Current candidate is still `0.1.0-rc1`. A final release remains blocked on genuine survival-world and two-player acceptance, balance review, and the owner's distribution-license decision.
