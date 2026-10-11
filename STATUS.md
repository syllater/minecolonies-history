# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository and verification

- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified code commit: `329c6ccde562bcfd8fb9b18c2cb372373e0d0c43`.
- Overall completion estimate: approximately **80%** (planning estimate, not a measured metric).
- Current development artifact version: `0.1.0-milestone2`; final release versioning remains open.

All GitHub Actions jobs passed in [run 38133512797](https://github.com/syllater/minecolonies-history/actions/runs/38133512797):
- English/Dutch translation-key and format-placeholder validation.
- Generated building-tier progression validation.
- Gradle compilation, unit tests and mod JAR upload.
- Headless `runClient` startup smoke test.
- Dedicated-server startup and validation of all ten generated Structurize schematics.
- Automated map-request cooldown tests, including pruning stale entries when a request occurs after the elapsed cleanup deadline rather than exactly on a tick boundary.

Artifacts from this run are retained until 2026-10-25:
- [Mod JAR artifact](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700226575)
  - SHA-256: `1fde2be03c3837fcb71fb4a230ac4baef38d495a3ed10ae88489d8dbe82efa1f`
- [Dedicated-server blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700405519)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38133512797/artifacts/11700361099)

## Implemented systems

- MineColonies public API integration and versioned Imperium-owned SavedData keyed by dimension and colony ID.
- Imperial Archive and Imperial Guard Tower at levels 1–5, with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat jobs, plus Siege Engineer, Field Medic and Cavalier specialist roles.
- Tier-scaled worker/guard capacity, production and training, with persisted per-citizen work cooldowns.
- Treasury, daily taxes, economic policies, knowledge investments and province development.
- Parliament, faction votes, Emperor assent/veto, legitimacy, stability, unrest, strikes, revolts and separatist petitions.
- Diplomacy, multi-colony realms, same- and cross-dimension invitations, shared treasury/laws, audit history, governors and regional events.
- Military campaigns, postures, supply routes, defensive orders, strategic-priority actions and protection against targeting a province in the same realm.
- Cross-dimension realm-management commands for routes, defensive orders and governor appointments/dismissals; the server resolves targets and checks realm membership and permissions.
- BlockUI Imperial Ledger and server-authoritative Imperial Theatre with pan, zoom, selection and a **Next dimension** control. The server only permits map projections for dimensions represented by actual provinces in the current realm.
- Per-player map-snapshot cooldown of 10 overworld ticks, with stale limiter entries pruned at elapsed intervals and across server clock resets.
- English and Dutch quick-start/help and command guidance.
- Regression tests for map-request bounds, realm dimension allowlisting, request throttling and stale-entry pruning.

## Remaining release acceptance

Use [the end-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md).

1. In a real survival world, build both huts, hire workers/guards, upgrade levels 1–5, then save/reload.
2. Click through the graphical theatre with a real multi-province realm; test the dimension selector with loaded and unloaded provinces.
3. Run a two-player dedicated-server test covering permissions, cross-dimension membership, treasury concurrency, allied-campaign restrictions, reconnect and save/reload.
4. Perform an in-world balance/usability review.
5. Prepare release notes and final package review, decide the distribution license, and change the development version before publishing.

Green CI verifies compilation, automated tests, client/server startup and schematic loading. It does not replace an actual survival-world or multiplayer playtest, so the project is not yet a final release.
