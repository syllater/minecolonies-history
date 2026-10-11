# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Automated verification runs through GitHub Actions. This connected session commits through GitHub and does not run a local shell.

## Latest fully verified commit
Commit `eb0b9a9d4011e5abb63de424d76655f0395e8694` passed the complete GitHub Actions workflow:
- English/Dutch translation-key validation: success.
- Generated blueprint tier/progression validation: success.
- Gradle compile, unit tests and JAR upload: success, including the tier-scaled production/training NBT round-trip regression test.
- Headless Minecraft client startup smoke test: success.
- Dedicated-server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38121078239

Build artifact: `imperium-realms-eb0b9a9d4011e5abb63de424d76655f0395e8694`, uploaded by that workflow.

## Implemented systems
- MineColonies public API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Visible architectural progression for levels 1–5: oriented roof eaves, larger/raised detailing, archive shelving/roof ridge/lanterns, tower stone trim/iron-barred windows/battlements and upper-level ornaments.
- Archive/tower worker and guard slots scale from one at tier 1 to five at tier 5.
- Archive production is tier-scaled: Philosophers gain 1/2/3 knowledge per work interval at tiers 1–2/3–4/5, Tax Collectors gain 1/2/3 efficiency percentage points per filing cycle, and Diplomats gain 1/2/3 influence. Existing caps and work intervals still apply.
- Specialist guard level-ups grant tier-scaled Siege Engineering, Field Medicine or Cavalry Drill points based on the assigned Guard Tower level; Military province focus doubles the gain, and each discipline retains its cap.
- Archive output cooldowns are now tracked per citizen UUID, persisted and bounded; additional workers can contribute in the same work interval, while repeat calls from one worker remain throttled.
- NBT round-trip coverage verifies tier-5 Archive output, tier-5 military training, persisted work timestamps and that loading does not permit an immediate duplicate work cycle.
- Philosopher, Tax Collector and Diplomat professions; Siege Engineer, Field Medic and Cavalier specialist guard roles.
- Daily taxes, treasury, investments, economic policy, factions, parliament bills, Emperor assent/veto and province focus/development.
- Citizen-happiness-linked stability, legitimacy, unrest, strikes, revolts and separatist petitions.
- Diplomatic influence/relations, multi-colony realms, capitals, invitations, shared reserve, common laws, audit trail, governors and regional events.
- Persistent military campaigns, three-front cap, postures, supply routes, temporary defensive orders and deterministic priority actions.
- BlockUI Imperial Ledger and a full-screen interactive Imperial Theatre map.
- The map snapshot is requested through a bounded custom payload and built server-side from the player's real MineColonies colony and realm. The map UI supports pan, zoom, recenter, province selection and a panel for stability/unrest/loyalty.
- English and Dutch translations, CI localization validation and generated-blueprint validation.

## Current milestone assessment
Milestones 0–9 have substantial implementation and automated coverage. The green client smoke test proves client startup only; it does not click through the GUI or verify survival-world interactions.

## Remaining acceptance gaps
- In-world survival test: build both hut types, hire workers/guards, upgrade levels 1–5, inspect tier silhouettes and save/reload both new and existing MineColonies colonies.
- Click-through playtest of the Imperial Theatre in a real client, including an invited realm with multiple provinces and off-screen/unloaded colonies.
- Real two-player permission, realm membership, simultaneous treasury and reconnect tests on a dedicated server.
- Balance pass of tier-scaled output, in-world save/reload checks, release notes, final JAR/package review and an explicit distribution/license decision.

## Next actions
1. Continue focused state/payload/persistence tests and fix build/startup blockers immediately.
2. Keep real survival and multiplayer checks marked pending until they are actually performed in Minecraft.
3. Prepare a release candidate after automated and human acceptance; do not publish a final release or choose a distribution license on the owner's behalf.
