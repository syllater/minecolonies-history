# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository and verification
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified commit: `2bf8caa30ebf7d8980e18dab3fc0a41293da7ce2`
- Overall completion estimate: approximately 77% (planning estimate, not a measured metric).
- Current Gradle artifact version string remains `0.1.0-milestone2`; final versioning remains part of release preparation.

All GitHub Actions jobs passed in [run 38128386716](https://github.com/syllater/minecolonies-history/actions/runs/38128386716):
- English/Dutch translation-key validation.
- Generated building-tier progression validation.
- Gradle compilation, unit tests and JAR upload.
- Headless `runClient` startup smoke test.
- Dedicated-server startup and validation of all ten generated Structurize schematics.

Artifacts (available while unexpired):
- [Mod JAR artifact](https://github.com/syllater/minecolonies-history/actions/runs/38128386716/artifacts/11697869571)
- [Dedicated-server blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38128386716/artifacts/11698550658)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38128386716/artifacts/11698445806)

## Implemented systems
- MineColonies API integration and versioned Imperium-owned SavedData keyed by dimension and colony ID.
- Imperial Archive and Imperial Guard Tower at levels 1–5, with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat jobs, plus Siege Engineer, Field Medic and Cavalier specialist roles.
- Tier-scaled worker/guard capacity, production and training, with persisted per-citizen work cooldowns.
- Treasury, daily taxes, economic policies, knowledge investments and province development.
- Parliament, faction votes, Emperor assent/veto, legitimacy, stability, unrest, strikes, revolts and separatist petitions.
- Diplomacy, multi-colony realms, same- and cross-dimension invitations, shared treasury/laws, audit history, governors and regional events.
- Military campaigns, postures, supply routes, defensive orders, strategic-priority actions and protection against targeting a province in the same realm.
- Dimension-aware commands:
  - `/imperium diplomacy improve-at <dimension> <colonyId>`
  - `/imperium campaign launch-at <type> <dimension> <colonyId>`
- BlockUI Imperial Ledger and server-authoritative Imperial Theatre map.
- English and Dutch localized quick-start/help and command guidance.

## Remaining release acceptance
Use [the end-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md).

1. In a real survival world, build both huts, hire workers/guards, upgrade levels 1–5, and save/reload.
2. Click through the graphical theatre with a real multi-province realm, including cross-dimension and unloaded provinces.
3. Test dedicated-server multiplayer permissions, cross-dimension membership, treasury concurrency, allied-campaign restrictions, reconnect and save/reload with two players.
4. Perform an in-world balance/usability review.
5. Prepare release notes and final package review, decide the distribution license, and change the development version before publishing.

Green CI verifies compilation, automated tests, client/server startup and blueprint loading. It does not replace the real in-game checks above; the release is not complete until those checks are documented.
