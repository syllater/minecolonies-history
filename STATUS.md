# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified code commit: `d5c987833ed49c7c3dc6e215d1506a008aec2147`
- Estimated overall completion: about 75% (planning estimate, not a measured metric).

## Latest automated verification
Commit `d5c987833ed49c7c3dc6e215d1506a008aec2147` passed all GitHub Actions jobs:
- English/Dutch translation-key validation, including quick-help strings.
- Generated building-tier progression validation.
- Gradle compile, unit tests and JAR upload.
- Headless Minecraft client startup smoke test.
- Dedicated-server startup and validation of all ten generated Structurize schematics.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38126846525

The CI artifact is named `imperium-realms-d5c987833ed49c7c3dc6e215d1506a008aec2147`; use the workflow's Artifacts section while it remains available.

## Recently added and hardened
- `/imperium` and `/imperium help` display a localized quick-start guide.
- Realm invitations accept a dimension ID and colony ID to invite a colony from another dimension.
- Strategic war campaigns are rejected against member provinces in the same realm, even when no bilateral diplomacy score has been recorded.

## Implemented systems
- MineColonies API integration and versioned Imperium-owned SavedData.
- Imperial Archive and Imperial Guard Tower levels 1–5 with ten generated Structurize schematics.
- Philosopher, Tax Collector, Diplomat and three specialist guard roles.
- Tier-scaled building capacity, Archive production and guard training.
- Per-citizen persisted and bounded work cooldowns.
- Treasury, daily taxes, policies, investment and province development.
- Parliament, faction votes, legitimacy/stability/unrest, strikes, revolts and separatist petitions.
- Diplomacy, multi-colony realms, invitations, shared treasury, laws, audit log, governors and regional events.
- Military campaigns, postures, supply routes, defensive orders and strategic-priority actions.
- BlockUI Imperial Ledger and server-authoritative interactive Imperial Theatre.
- English/Dutch localization and automated validation.

## Remaining release acceptance
1. Build both huts in survival, hire workers/guards, upgrade levels 1–5 and save/reload.
2. Click through the theatre with a real multi-province realm, including off-screen/unloaded and cross-dimension provinces.
3. Run dedicated-server two-player tests for permissions, invitations/membership, treasury concurrency, allied-war restrictions, reconnect and save/reload.
4. Complete in-world balance/usability review.
5. Prepare release notes and final JAR/package review; make an explicit distribution/license decision.

Green CI verifies compile/tests and client/server startup, but does not replace the real in-game checks above. The release is not complete until those are performed and documented.
