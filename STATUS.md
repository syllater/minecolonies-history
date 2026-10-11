# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository and verification

- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified branch head: `eb6b790ebf5f58ef6ea52a36ae7ba5ca33a8f4ab`.
- Latest feature-code commit (source unchanged by the later CI/docs-only commits): `329c6ccde562bcfd8fb9b18c2cb372373e0d0c43`.
- Overall completion estimate: approximately **80%** (planning estimate, not a measured metric).
- Current development artifact version: `0.1.0-milestone2`; final release versioning remains open.

All jobs passed in [GitHub Actions run 38133948650](https://github.com/syllater/minecolonies-history/actions/runs/38133948650):
- English/Dutch translation-key and placeholder validation.
- Generated building-tier progression validation.
- `./gradlew --no-daemon test build`.
- SHA-256 manifest generation and `sha256sum --check SHA256SUMS.txt`; the built JAR verified successfully before upload.
- Headless `runClient` startup smoke test.
- Dedicated-server startup and validation of all ten generated Structurize schematics.

Artifacts from this run are retained until 2026-10-25:
- [Mod JAR + SHA256SUMS.txt bundle](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700525650)
  - Artifact ZIP SHA-256: `0eb6d4c0393ffa072ad10ae711d4860161fd9565b2bca7e8cfa5f9eccb327fd3`.
  - The separate `SHA256SUMS.txt` inside the bundle records the JAR checksum; CI verified it with `sha256sum --check`.
- [Dedicated-server blueprint log](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700520830)
- [Client startup log](https://github.com/syllater/minecolonies-history/actions/runs/38133948650/artifacts/11700421184)

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
- CI release-artifact hardening: the JAR checksum manifest is generated, verified, and uploaded alongside the JAR.

## Deferred, non-blocking maintenance

- NeoForge emits deprecation warnings for `EventBusSubscriber.bus()` usage in existing event subscribers. This does not prevent compilation or client/server startup on the pinned baseline; migrate these subscribers as a dedicated compatibility task before upgrading NeoForge.
- Gradle and some GitHub Actions dependencies emit future-compatibility/deprecation warnings. They are tracked as maintenance items and are not current build failures.

## Remaining release acceptance

Use [the end-to-end survival and multiplayer checklist](docs/PLAYTEST_RELEASE_CHECKLIST.md).

1. In a real survival world, build both huts, hire workers/guards, upgrade levels 1–5, then save/reload.
2. Click through the graphical theatre with a real multi-province realm; test the dimension selector with loaded and unloaded provinces.
3. Run a two-player dedicated-server test covering permissions, cross-dimension membership, treasury concurrency, allied-campaign restrictions, reconnect and save/reload.
4. Perform an in-world balance/usability review.
5. Prepare release notes and final package review, decide the distribution license, and change the development version before publishing.

Green CI verifies compilation, automated tests, client/server startup, schematic loading and artifact checksums. It does not replace an actual survival-world or multiplayer playtest, so the project is not yet a final release.
