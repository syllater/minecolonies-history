# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Verification is run by the repository's GitHub Actions workflow; this connected session can commit source but does not execute a local shell.

## Latest fully verified commit
Commit `bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73` passed the complete GitHub Actions workflow:
- English/Dutch translation-key validation: success.
- Generated five-tier Structurize blueprint progression validation: success.
- Gradle compile, unit tests and mod JAR upload: success.
- Headless Minecraft client startup smoke test: success.
- Dedicated-server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119451312

Available build artifact: `imperium-realms-bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73` (JAR uploaded by that workflow).

## Implemented systems
- MineColonies public API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat professions; Siege Engineer, Field Medic and Cavalier specialist guard roles.
- Archive/tower worker and guard slots scale from one at tier 1 to five at tier 5.
- Daily taxes, treasury, investments, economic policy, factions, parliament bills, Emperor assent/veto and province focus/development.
- Citizen-happiness-linked stability, legitimacy, unrest, strikes, revolts and separatist petitions.
- Diplomatic influence/relations, multi-colony realms, capitals, invitations, shared reserve, common laws, audit trail, governors and regional events.
- Persistent military campaigns, three-front cap, postures, supply routes, temporary defensive orders and deterministic priority actions.
- BlockUI Imperial Ledger and a full-screen interactive Imperial Theatre map.
- The map snapshot is requested through a bounded custom payload and built server-side from the player's real MineColonies colony and realm. The map UI supports pan, zoom, recenter, province selection and a panel for stability/unrest/loyalty.
- English and Dutch translations, CI localization validation and blueprint progression validation.

## Current milestone assessment
Milestones 0–9 have substantial implementation and automated coverage. This does **not** mean the mod is release-ready. A green client smoke test proves the client starts, not that every GUI interaction works in a real survival world.

## Remaining acceptance gaps
- In-world survival test: build both hut types, hire workers/guards, upgrade levels 1–5 and save/reload both new and existing MineColonies colonies.
- Click-through playtest of the Imperial Theatre in a real client, including an invited realm with multiple provinces and off-screen/unloaded colonies.
- Real two-player permission, realm membership, simultaneous treasury and reconnect tests on a dedicated server.
- Richer level-specific building silhouettes and deeper building modules.
- Release notes, final JAR/package review and an explicit distribution/license decision.

## Next actions
1. Add focused unit coverage for map-payload limits and normalization.
2. Keep running the full build/client/server workflow after changes.
3. Continue fixing build or startup blockers immediately; defer cosmetic-only issues.
4. Keep real survival and multiplayer checks marked pending until actually performed in Minecraft.
5. Do not publish a final release or choose a distribution license on the owner's behalf.
