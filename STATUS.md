# Status

Last updated: 2026-10-12 Europe/Amsterdam.

## Current release candidate

- Project: **Imperium: European Realms**
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Candidate version: `0.1.0-rc1`
- Target: Minecraft Java 1.21.1, Java 21, NeoForge 21.1.x; MineColonies is required.
- Latest fully verified source commit: `a2bf703628c25379dd1c4f4acb1a8aa1e3dc457a`.
- Overall completion estimate: approximately **91%**. This is a planning estimate, not a measured metric.
- Release status: **release candidate only**. No final public release has been published.

## Latest automated verification — all green

[GitHub Actions run 38296167316](https://github.com/syllater/minecolonies-history/actions/runs/38296167316) passed all four jobs on source commit `a2bf703628c25379dd1c4f4acb1a8aa1e3dc457a`.

- English and Dutch localization: 298 keys/placeholders verified.
- Static checks for generated tier progression, dependency/release metadata, all seven cross-dimension command variants and BlockUI ledger wiring passed.
- GUI wiring check: 40 buttons, 38 fixed command handlers and two dedicated actions, all accounted for.
- `./gradlew --no-daemon test build` passed.
- JAR SHA-256 manifest generation/checking and the self-checking release-candidate bundle passed.
- Headless `runClient` reached the `Sound engine started` marker.
- Dedicated-server startup validated `jobs=6 buildings=2 guardTypes=3 modules=6` and loaded all **10/10** Structurize blueprints.
- All **six live Minecraft GameTests** passed:
  1. Daily tax idempotency, economy/profession cooldowns, realm/law state and treasury/audit round-trip.
  2. Discovery of a real temporary MineColonies colony, rename preservation and Imperium SavedData round-trip.
  3. Real Imperial Archive registration, worker job assignment and level 1–5 capacity.
  4. Real Imperial Guard Tower registration, military specialists and level 1–5 capacity.
  5. Real Brigadier dispatch: owner tax/treasury/parliament actions, non-owner denial of tax/policy/treasury/assent/veto, emperor-identity audit of a valid veto, and insufficient-funds treasury rollback.
  6. Cross-dimension realm invitation, owner-only join, duplicate-join denial and realm NBT round-trip.

## Candidate integrity

JAR SHA-256:

```text
351eeafb48e8e6bea538a701c55ae24f10b806df2a3ed15c8979c7701d13bebe
```

Artifacts from the fully green workflow:
- [Self-checking release-candidate bundle](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768472386)
- [Mod JAR + SHA256SUMS.txt](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768327658)
- [Live GameTest log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11767998216)
- [Dedicated-server/blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768902441)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38296167316/artifacts/11768358033)

## Implemented systems

- Real MineColonies colony lookup and Imperium-owned versioned SavedData keyed by dimension + colony ID.
- Imperial Archive and Imperial Guard Tower with five tiers each and ten Structurize schematics.
- Philosopher, Tax Collector and Diplomat jobs; Siege Engineer, Imperial Field Medic and Imperial Cavalier specialist roles.
- Tier-scaled hiring/training capacity and persisted per-worker production cooldowns.
- Treasury, daily tax collection, provincial focus/development, economic policies, knowledge investment, imperial remittance and audited atomic treasury transfers.
- Parliament/faction votes, Emperor assent/veto, shared realm laws, stability, legitimacy, unrest, strikes, revolts and separatist petitions.
- Diplomacy, multi-colony realms, cross-dimension invitations, governors, regional events, supply routes, defensive orders and military campaigns.
- BlockUI Imperial Ledger and server-authoritative Imperial Theatre with province selection and pan/zoom/recenter.
- English/Dutch translations, command/help contract checks, regression tests and reproducible/checksummed RC packaging.

## Remaining final-release gates

These require hands-on play in a real world/server; CI cannot honestly satisfy them:

1. Build both huts in survival, upgrade levels 1–5, hire workers/guards, observe sustained behavior and save/reload the actual world.
2. Click through the BlockUI ledger and Imperial Theatre in a real client, including a multi-province realm and a remote dimension.
3. Run a real two-player dedicated-server session for invitations, permissions, concurrent treasury actions, reconnect and save/reload.
4. Review gameplay balance/usability and release notes.
5. Choose and apply an explicit distribution license before any final public release.

The release checklist is [docs/PLAYTEST_RELEASE_CHECKLIST.md](docs/PLAYTEST_RELEASE_CHECKLIST.md). The draft PR remains open for review: [PR #2](https://github.com/syllater/minecolonies-history/pull/2).

## Deferred non-blocking maintenance

- NeoForge currently emits a deprecation warning around `EventBusSubscriber.bus()`; migration can be handled with a future platform update.
- Gradle/GitHub Actions components emit future-compatibility warnings that do not block the current build or launch tests.

