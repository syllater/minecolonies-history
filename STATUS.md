# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- This connected environment edits through GitHub APIs and uses Actions for build/runtime checks; it cannot execute a local shell. Build/runtime claims refer to actual Actions results.

## Latest fully verified commit
Commit `d2cbad845583688e935153dae55ebb48e2169c5a` passed the complete workflow:
- English/Dutch validation: 265 literal and dynamic localization keys checked, with matching placeholder types.
- Gradle compilation, unit tests and mod JAR packaging: success.
- Headless Minecraft client startup smoke test: success.
- Dedicated server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075373770

The verified automated suite includes strategic map grid behavior and NBT round-trip tests for province economy/profession progress and realm membership/laws/routes/defence/governors/audit state.

## Next prepared change
- Scale the three Imperial Archive worker module limits with building level. Level 1 permits one Philosopher, Tax Collector and Diplomat each; Level 5 permits up to five of each. The Guard Tower's specialist guard limits already scale with its level.
- Make the localization validator compare placeholder indices and format types, not just the types.
- Extend the in-game acceptance checklist to confirm these level-dependent worker limits.
These changes have been prepared but still need CI validation on the next commit.

## Major systems implemented
- MineColonies API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat MineColonies professions with work AI.
- Imperial Siege Engineer, Field Medic and Cavalier specialist training tracks.
- Daily taxation, treasury, knowledge investment, economic policies, faction voting and parliamentary decisions.
- Citizen-happiness feedback, faction approval, legitimacy, stability, unrest, strikes and revolts.
- Diplomatic relations and persisted strategic military campaigns.
- Province ranks and agriculture, trade, scholarship, military and civic focus.
- Federated realms with Emperor/capital, up to 64 real colony provinces, invitations and shared central reserve.
- Imperial tax/policy laws voted through the capital propagate to current provinces; new members inherit active laws.
- Enacted imperial tax law remits a portion of already-collected provincial receipts to the central reserve, avoiding a second citizen tax.
- Persistent audit history, governor appointments, regional events, cohesion and separatist petitions.
- Up to three concurrent operations against distinct targets, persisted doctrine, direct supply routes and temporary emergency defence orders.
- Strategic priority planner and BlockUI controls for doctrine, priority route construction and priority defence.
- `/imperium empire map` renders a coordinate grid from loaded MineColonies colony centers, listing other-dimension/unresolved colonies separately.
- English and Dutch translations plus automated localization coverage validation.
- CI for build/tests, dedicated-server schematic validation and client startup.

## Remaining acceptance gaps
- Real survival test: place both buildings, hire workers, upgrade levels 1–5, then save/reload an existing and a new MineColonies colony.
- Multiplayer playtest with at least two players for permissions, realm membership, governor appointments and concurrent treasury actions.
- More mechanical/visual differentiation between building levels and deeper building modules.
- The coordinate map is a compact text-grid/chat view, not a fully interactive pan/zoom theatre map.
- Release notes, final artifact/package review and an explicit distribution/license decision.

## Next actions
1. Verify the level-dependent Archive hiring limits and placeholder-index validator with a full CI run.
2. Continue improving high-value runtime/persistence features; defer non-blocking cosmetic warnings.
3. Keep survival and multiplayer tests marked pending until actually performed in Minecraft.
