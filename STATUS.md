# Status

Last updated: 2026-10-12 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x, required MineColonies dependency set.
- Latest fully verified source commit: `eb002edc10253472f235f367d226b42d12d1d709`.
- Overall completion estimate: approximately **90%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Latest automated verification — all green

[GitHub Actions run 38288316185](https://github.com/syllater/minecolonies-history/actions/runs/38288316185) passed **all four jobs** on source commit `eb002edc10253472f235f367d226b42d12d1d709`.

- English and Dutch localization: 298 translation keys/placeholders passed.
- Static validators passed for tier progression, dependency/release metadata, all seven cross-dimension command variants, and BlockUI ledger wiring: 40 buttons, 38 command handlers and two dedicated actions.
- `./gradlew --no-daemon test build` passed.
- JAR SHA-256 generation/verification and the self-checking release-candidate ZIP passed.
- Headless `runClient` reached the `Sound engine started` marker.
- Dedicated-server startup validated MineColonies registrations (`jobs=6 buildings=2 guardTypes=3 modules=6`) and loaded all **10/10** Structurize blueprints.
- Five live Minecraft GameTests passed:
  1. SavedData/economy/realm/tax idempotency, worker cooldown and treasury/audit round-trip.
  2. Discovery and serialization of a real temporary MineColonies colony with rename preservation.
  3. Real Imperial Archive registration, worker-job assignment and tier 1–5 capacity.
  4. Real Imperial Guard Tower registration, military specialists and tier 1–5 capacity.
  5. Real command dispatch: the owner can submit tax and perform treasury deposits/withdrawals; non-owner tax/policy/deposit/withdraw attempts are denied; underfunded authorized transfers are rejected without changing balances or audit history. Denied commands leave Parliament proposal state unchanged, and the test accounts for the province's actual opening balance.

Latest candidate JAR SHA-256: `a7dd3f42bf49b7703d15f94f7903b3e625d189d86b26919bfcf19b3e505f1940`.

Artifacts from the verified run:
- [Self-checking 0.1.0-rc1 bundle](https://github.com/syllater/minecolonies-history/actions/runs/38288316185/artifacts/11764163159)
- [Mod JAR and SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38288316185/artifacts/11764183051)
- [Live GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38288316185/artifacts/11763763931)
- [Dedicated-server schematic/registry log](https://github.com/syllater/minecolonies-history/actions/runs/38288316185/artifacts/11764487140)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38288316185/artifacts/11763913074)

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



## Latest checkpoint — treasury authorization verified

All automated gates are green on the candidate above. Manual acceptance remains open for survival construction/upgrades, sustained worker behavior and world save/reload, graphical UI click-through, a genuine two-player session (including concurrency/reconnect), balance review and a distribution-license decision. No final public release has been published.
