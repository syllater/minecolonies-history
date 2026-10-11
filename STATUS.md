# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest verified commit: `117da4d4b97693902d90dd2b490d5a18cd4c8b01`
- Estimated total completion: about 75% (planning estimate, not a measured metric).

## Latest automated verification
Commit `117da4d4b97693902d90dd2b490d5a18cd4c8b01` passed all jobs:
- English/Dutch translation-key validation.
- Generated building-tier progression validation.
- Gradle compilation, unit tests and mod JAR upload.
- Headless Minecraft client startup smoke test.
- Dedicated-server startup and validation of all 10 generated Structurize schematics.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38121255613

The CI artifact is named `imperium-realms-117da4d4b97693902d90dd2b490d5a18cd4c8b01` and is available from the workflow's Artifacts section until it expires.

## Implemented
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
- Build both huts, hire workers/guards, upgrade levels 1–5 and save/reload them in a real Minecraft world.
- Click through the graphical map with a real multi-province realm.
- Run a two-player dedicated-server test for permissions, realm invitations, treasury concurrency and reconnect/save-reload.
- Complete an in-world balance/usability pass.
- Prepare release notes and final JAR/package review; make an explicit distribution/license decision.

A green CI run proves build/tests and client/server startup, but does not replace the in-game checks above. Do not mark the release milestone complete until those checks are performed and documented.
