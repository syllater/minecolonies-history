# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified commit: `adda424cc0e53ac36c15345815d772681ac682f4`
- Estimated overall completion: about 75% (planning estimate, not a measured metric).

## Latest automated verification
Commit `adda424cc0e53ac36c15345815d772681ac682f4` passed all jobs in GitHub Actions:
- English and Dutch translation-key validation, including all eight new quick-help strings.
- Generated building-tier progression validation.
- Gradle compile, unit tests and mod JAR upload.
- Headless Minecraft client startup smoke test.
- Dedicated-server startup and validation of all ten generated Structurize schematics.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38126511176

The CI JAR artifact is named `imperium-realms-adda424cc0e53ac36c15345815d772681ac682f4` and is available from the workflow's Artifacts section until it expires.

## Recent usability improvement
- `/imperium` and `/imperium help` now display a localized quick-start guide.
- The guide explains colony context, economy proposals, parliamentary assent/veto, realm membership, military operations, the map and server permission checks.
- Both EN/NL quick-help keys are covered by the automated localization validator.

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
1. In a real survival world, build both huts, hire workers/guards, upgrade levels 1–5 and save/reload.
2. Click through the graphical theatre with a real multi-province realm, including off-screen/unloaded provinces.
3. Run dedicated-server two-player tests for permissions, invitations/membership, treasury concurrency and reconnect/save-reload.
4. Complete in-world balance/usability review.
5. Prepare release notes and final JAR/package review; make an explicit distribution/license decision.

A green CI run proves compile/tests and client/server startup, but does not replace the in-game checks above. Do not mark the release milestone complete until those checks are performed and documented.
