# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x, required MineColonies dependency set.
- Candidate source commit: `a22d79f64c4a41601feaf7608f12d86950b75698`
- Overall completion estimate: approximately **86%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Automated verification — green

The RC workflow completed successfully in [GitHub Actions run 38157810821](https://github.com/syllater/minecolonies-history/actions/runs/38157810821) against the branch merged with `main` for CI:

- English and Dutch translation-key/placeholder validation passed.
- Generated blueprint tier-progression validation passed.
- Release metadata/dependency declaration validation passed.
- Cross-dimension command registration and EN/NL help-contract validation passed for all seven new command variants.
- `./gradlew --no-daemon test build` passed, including treasury transfer success/failure cases, NBT round-trip persistence of balances/audit records, and isolation of malformed dimension identifiers.
- The JAR SHA-256 manifest was generated and verified.
- The 0.1.0-rc1 release bundle was assembled; ZIP contents and the embedded JAR checksum were verified.
- Headless `runClient` startup smoke test passed.
- Dedicated-server startup passed and validated the MineColonies registry plus all **10/10** generated Structurize schematics and hut anchors.

Artifacts from that green run (the RC package is retained for 30 days):

- [Self-checking 0.1.0-rc1 release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709157384)
- [Mod JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709232110)
- [Dedicated-server blueprint/registry log](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709785938)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38157810821/artifacts/11709161486)

The release-candidate bundle contains the JAR, checksum manifest, README, changelog, compatibility notes, candidate notes and the mandatory in-world playtest checklist.

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

## Deferred, non-blocking maintenance

- The pinned NeoForge line emits deprecation warnings for `EventBusSubscriber.bus()`; migrate before the next platform upgrade.
- Gradle and some GitHub Actions components emit future-compatibility warnings. They do not currently prevent a successful candidate build or startup.
- These warnings are intentionally deferred in favor of the core release acceptance work.

## Review links

- [Draft release-candidate PR #2](https://github.com/syllater/minecolonies-history/pull/2)
- [Green RC verification workflow](https://github.com/syllater/minecolonies-history/actions/runs/38157810821)
- [End-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md)
- [Release-candidate notes](docs/RELEASE_CANDIDATE_NOTES.md)
