# Status

Last updated: 2026-10-11 Europe/Amsterdam.

## Repository and verification
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- Latest fully verified code commit: `095a0f4308904f272e356e0d979c69febb8df08e`
- Overall completion estimate: approximately 75% (planning estimate, not a measured metric).
- Current Gradle version string remains `0.1.0-milestone2`; update it as part of final release preparation.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38127250225

All CI jobs passed on commit `095a0f4308904f272e356e0d979c69febb8df08e`:
- English/Dutch translation-key validation.
- Generated blueprint progression validation.
- Gradle compile, unit tests and JAR upload.
- Headless client startup (`runClient` reached the Minecraft initialization marker).
- Dedicated server startup and validation of all ten generated Structurize blueprints.
- Unit coverage for cross-dimension realm membership and allied-war restrictions.

[Mod JAR artifact and smoke-test logs](https://github.com/syllater/minecolonies-history/actions/runs/38127250225/artifacts) are available from this workflow while the artifacts remain unexpired.

## Implemented
- MineColonies API integration and versioned Imperium-owned SavedData.
- Imperial Archive and Imperial Guard Tower at levels 1–5 with ten generated Structurize blueprints.
- Philosopher, Tax Collector, Diplomat and three specialist guard roles; tier-scaled capacity/output and persisted work cooldowns.
- Treasury, daily taxes, economic policies, investments and provincial development.
- Parliament, faction voting, Emperor assent/veto, legitimacy, stability, unrest, strikes, revolts and separatist petitions.
- Diplomacy, federated multi-colony realms, cross-dimension invitations, shared treasury/laws, audit log, governors and regional events.
- Military campaigns, postures, supply routes, defensive orders and strategic-priority actions. War campaigns against member provinces in the same realm are rejected.
- BlockUI Imperial Ledger and server-authoritative Imperial Theatre map.
- Localized English/Dutch commands, including `/imperium` and `/imperium help`.

## Remaining release acceptance
1. In a real survival world, build both huts, hire workers/guards, upgrade levels 1–5, and save/reload.
2. Play through the graphical theatre with a real multi-province and cross-dimension realm.
3. Test dedicated-server multiplayer permissions, invitations, treasury concurrency, allied-war restrictions, reconnect and save/reload with two players.
4. Review in-world balance/usability.
5. Prepare release notes and final package review, choose a distribution license, and update the version before publishing.

Green CI verifies compile/tests and client/server startup; it does not replace the real in-game checks above.
