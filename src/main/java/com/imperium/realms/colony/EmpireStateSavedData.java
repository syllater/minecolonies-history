package com.imperium.realms.colony;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Global Imperium registry stored in the server overworld. */
public final class EmpireStateSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_empire_state";
    private static final int SCHEMA_VERSION = 8;
    private static final String TAG_SCHEMA_VERSION = "schema_version";
    private static final String TAG_COLONIES = "colonies";

    private static final Factory<EmpireStateSavedData> FACTORY = new Factory<>(
            EmpireStateSavedData::new,
            EmpireStateSavedData::load,
            DataFixTypes.LEVEL);

    private final Map<ColonyIdentity, EmpireState> colonies = new LinkedHashMap<>();

    public static EmpireStateSavedData get(final ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private EmpireStateSavedData() {
    }

    private static EmpireStateSavedData load(
            final CompoundTag root,
            final HolderLookup.Provider registries) {
        final EmpireStateSavedData data = new EmpireStateSavedData();
        final int savedSchema = root.contains(TAG_SCHEMA_VERSION, Tag.TAG_INT)
                ? root.getInt(TAG_SCHEMA_VERSION)
                : 0;

        if (savedSchema != SCHEMA_VERSION) {
            data.setDirty();
        }

        if (!root.contains(TAG_COLONIES, Tag.TAG_LIST)) {
            return data;
        }

        final ListTag entries = root.getList(TAG_COLONIES, Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            final CompoundTag entry = entries.getCompound(index);
            try {
                final ColonyIdentity identity = new ColonyIdentity(
                        entry.getString("dimension"),
                        entry.getInt("colony_id"));
                final long firstSeen = Math.max(0L, entry.getLong("first_seen"));
                final long lastSeen = Math.max(firstSeen, entry.getLong("last_seen"));
                final EconomicPolicy policy = EconomicPolicy.fromId(entry.getString("economic_policy"))
                        .orElse(EconomicPolicy.BALANCED);

                final List<ParliamentProposal> proposals = new ArrayList<>();
                if (entry.contains("parliament_proposals", Tag.TAG_LIST)) {
                    final ListTag savedProposals = entry.getList("parliament_proposals", Tag.TAG_COMPOUND);
                    for (int proposalIndex = 0; proposalIndex < savedProposals.size(); proposalIndex++) {
                        final CompoundTag proposalTag = savedProposals.getCompound(proposalIndex);
                        try {
                            final Map<String, Boolean> votes = new LinkedHashMap<>();
                            if (proposalTag.contains("council_votes", Tag.TAG_LIST)) {
                                final ListTag savedVotes = proposalTag.getList("council_votes", Tag.TAG_COMPOUND);
                                for (int voteIndex = 0; voteIndex < savedVotes.size(); voteIndex++) {
                                    final CompoundTag vote = savedVotes.getCompound(voteIndex);
                                    final String seat = vote.getString("seat");
                                    if (seat.equals("merchants") || seat.equals("commons")
                                            || seat.equals("nobility") || seat.equals("scholars")) {
                                        votes.put(seat, vote.getBoolean("yes"));
                                    }
                                }
                            }

                            // Schema versions <= 3 stored only tax bills. Missing type
                            // therefore migrates safely to TAX_RATE.
                            ParliamentProposal.Type proposalType = ParliamentProposal.Type.TAX_RATE;
                            try {
                                if (proposalTag.contains("proposal_type", Tag.TAG_STRING)) {
                                    proposalType = ParliamentProposal.Type.valueOf(
                                            proposalTag.getString("proposal_type"));
                                }
                            } catch (IllegalArgumentException ignored) {
                                // Unknown bill types are treated as the legacy tax-bill shape.
                            }

                            final EconomicPolicy previousPolicy = EconomicPolicy.fromId(
                                    proposalTag.getString("previous_policy"))
                                    .orElse(proposalType == ParliamentProposal.Type.ECONOMIC_POLICY
                                            ? EconomicPolicy.BALANCED : null);
                            final EconomicPolicy proposedPolicy = EconomicPolicy.fromId(
                                    proposalTag.getString("proposed_policy"))
                                    .orElse(proposalType == ParliamentProposal.Type.ECONOMIC_POLICY
                                            ? EconomicPolicy.BALANCED : null);

                            ParliamentProposal.Status proposalStatus = ParliamentProposal.Status.OPEN;
                            try {
                                proposalStatus = ParliamentProposal.Status.valueOf(proposalTag.getString("status"));
                            } catch (IllegalArgumentException ignored) {
                                // An unknown future status is treated as open and will expire normally.
                            }

                            proposals.add(ParliamentProposal.restore(
                                    proposalTag.getLong("id"),
                                    proposalType,
                                    proposalTag.getString("proposer"),
                                    proposalTag.getInt("previous_tax"),
                                    proposalTag.getInt("proposed_tax"),
                                    previousPolicy,
                                    proposedPolicy,
                                    proposalTag.getLong("created_day"),
                                    proposalTag.getLong("expires_day"),
                                    votes,
                                    proposalStatus,
                                    proposalTag.getString("resolved_by"),
                                    proposalTag.contains("resolved_day")
                                            ? proposalTag.getLong("resolved_day") : -1L));
                        } catch (IllegalArgumentException ignored) {
                            // Ignore one malformed bill without losing the colony's economy.
                        }
                    }
                }

                final Map<ColonyIdentity, Integer> diplomaticRelations = new LinkedHashMap<>();
                if (entry.contains("diplomatic_relations", Tag.TAG_LIST)) {
                    final ListTag savedRelations = entry.getList("diplomatic_relations", Tag.TAG_COMPOUND);
                    for (int relationIndex = 0; relationIndex < savedRelations.size(); relationIndex++) {
                        final CompoundTag relation = savedRelations.getCompound(relationIndex);
                        try {
                            final ColonyIdentity target = new ColonyIdentity(
                                    relation.getString("dimension"),
                                    relation.getInt("colony_id"));
                            if (!identity.equals(target)) {
                                diplomaticRelations.put(target, relation.getInt("score"));
                            }
                        } catch (IllegalArgumentException ignored) {
                            // Skip a malformed diplomatic record without losing the colony's treasury.
                        }
                    }
                }

                final EmpireState state = new EmpireState(
                        identity,
                        entry.getString("colony_name"),
                        firstSeen,
                        lastSeen,
                        entry.getLong("treasury_crowns"),
                        entry.contains("tax_rate") ? entry.getInt("tax_rate") : 5,
                        policy,
                        entry.getLong("knowledge_points"),
                        entry.contains("stability") ? entry.getInt("stability") : 50,
                        entry.contains("last_tax_day") ? entry.getLong("last_tax_day") : -1L,
                        entry.contains("last_scholar_work_tick") ? entry.getLong("last_scholar_work_tick") : -1L,
                        entry.contains("next_proposal_id") ? entry.getLong("next_proposal_id") : 1L,
                        proposals,
                        entry.contains("legitimacy") ? entry.getInt("legitimacy") : 50,
                        entry.contains("tax_collection_efficiency")
                                ? entry.getInt("tax_collection_efficiency") : 0,
                        entry.contains("last_tax_collector_work_tick")
                                ? entry.getLong("last_tax_collector_work_tick") : -1L,
                        entry.contains("diplomatic_influence")
                                ? entry.getLong("diplomatic_influence") : 0L,
                        entry.contains("last_diplomat_work_tick")
                                ? entry.getLong("last_diplomat_work_tick") : -1L,
                        diplomaticRelations);

                final Map<String, Integer> factionApproval = new LinkedHashMap<>();
                if (entry.contains("faction_approval", Tag.TAG_LIST)) {
                    final ListTag savedFactionApproval = entry.getList("faction_approval", Tag.TAG_COMPOUND);
                    for (int factionIndex = 0; factionIndex < savedFactionApproval.size(); factionIndex++) {
                        final CompoundTag faction = savedFactionApproval.getCompound(factionIndex);
                        final String factionId = faction.getString("id");
                        if (factionId.equals("merchants") || factionId.equals("commons")
                                || factionId.equals("nobility") || factionId.equals("scholars")) {
                            factionApproval.put(factionId, faction.getInt("approval"));
                        }
                    }
                }

                EmpireState.CivicDisorder civicDisorder = EmpireState.CivicDisorder.CALM;
                try {
                    if (entry.contains("civic_disorder", Tag.TAG_STRING)) {
                        civicDisorder = EmpireState.CivicDisorder.valueOf(entry.getString("civic_disorder"));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Treat an unknown future status as calm rather than locking a world in a crisis.
                }
                state.restorePoliticalSimulation(
                        factionApproval,
                        entry.contains("unrest") ? entry.getInt("unrest") : 0,
                        civicDisorder,
                        entry.contains("last_civic_disorder_change_day")
                                ? entry.getLong("last_civic_disorder_change_day") : -1L);
                data.colonies.put(identity, state);
            } catch (IllegalArgumentException exception) {
                // Skip malformed records instead of failing the whole world load.
            }
        }
        return data;
    }

    public Optional<EmpireState> get(final ColonyIdentity identity) {
        return Optional.ofNullable(colonies.get(identity));
    }

    public Collection<EmpireState> allStates() {
        return new ArrayList<>(colonies.values());
    }

    /** Idempotently create a record for a first observation or refresh metadata. */
    public boolean observeColony(
            final ColonyIdentity identity,
            final String colonyName,
            final long gameTime) {
        final EmpireState existing = colonies.get(identity);
        if (existing == null) {
            colonies.put(identity, EmpireState.create(identity, colonyName, gameTime));
            setDirty();
            return true;
        }

        if (existing.observe(colonyName, gameTime)) {
            setDirty();
        }
        return false;
    }

    public void markChanged() {
        setDirty();
    }

    @Override
    public CompoundTag save(final CompoundTag root, final HolderLookup.Provider registries) {
        root.putInt(TAG_SCHEMA_VERSION, SCHEMA_VERSION);
        final ListTag entries = new ListTag();
        for (final EmpireState state : colonies.values()) {
            final CompoundTag entry = new CompoundTag();
            entry.putString("dimension", state.identity().dimensionId());
            entry.putInt("colony_id", state.identity().colonyId());
            entry.putString("colony_name", state.colonyName());
            entry.putLong("first_seen", state.firstSeenGameTime());
            entry.putLong("last_seen", state.lastSeenGameTime());
            entry.putLong("treasury_crowns", state.treasuryCrowns());
            entry.putInt("tax_rate", state.taxRatePercent());
            entry.putString("economic_policy", state.economicPolicy().id());
            entry.putLong("knowledge_points", state.knowledgePoints());
            entry.putInt("stability", state.stability());
            entry.putInt("legitimacy", state.legitimacy());
            entry.putInt("tax_collection_efficiency", state.taxCollectionEfficiencyPercent());
            entry.putLong("last_tax_collector_work_tick", state.lastTaxCollectorWorkTick());
            entry.putLong("diplomatic_influence", state.diplomaticInfluence());
            entry.putLong("last_diplomat_work_tick", state.lastDiplomatWorkTick());
            entry.putInt("unrest", state.unrest());
            entry.putString("civic_disorder", state.civicDisorder().name());
            entry.putLong("last_civic_disorder_change_day", state.lastCivicDisorderChangeDay());
            entry.putLong("last_tax_day", state.lastTaxDay());

            final ListTag factionApproval = new ListTag();
            state.factionApproval().forEach((factionId, approval) -> {
                final CompoundTag faction = new CompoundTag();
                faction.putString("id", factionId);
                faction.putInt("approval", approval);
                factionApproval.add(faction);
            });
            entry.put("faction_approval", factionApproval);

            final ListTag diplomaticRelations = new ListTag();
            state.diplomaticRelations().forEach((target, score) -> {
                final CompoundTag relation = new CompoundTag();
                relation.putString("dimension", target.dimensionId());
                relation.putInt("colony_id", target.colonyId());
                relation.putInt("score", score);
                diplomaticRelations.add(relation);
            });
            entry.put("diplomatic_relations", diplomaticRelations);
            entry.putLong("last_scholar_work_tick", state.lastScholarWorkTick());
            entry.putLong("next_proposal_id", state.nextProposalId());

            final ListTag proposals = new ListTag();
            for (final ParliamentProposal proposal : state.storedParliamentProposals()) {
                final CompoundTag proposalTag = new CompoundTag();
                proposalTag.putLong("id", proposal.id());
                proposalTag.putString("proposal_type", proposal.type().name());
                proposalTag.putString("proposer", proposal.proposer());
                proposalTag.putInt("previous_tax", proposal.previousTaxRate());
                proposalTag.putInt("proposed_tax", proposal.proposedTaxRate());
                if (proposal.previousPolicy() != null) {
                    proposalTag.putString("previous_policy", proposal.previousPolicy().id());
                }
                if (proposal.proposedPolicy() != null) {
                    proposalTag.putString("proposed_policy", proposal.proposedPolicy().id());
                }
                proposalTag.putLong("created_day", proposal.createdDay());
                proposalTag.putLong("expires_day", proposal.expiresDay());
                proposalTag.putString("status", proposal.status().name());
                proposalTag.putString("resolved_by", proposal.resolvedBy());
                proposalTag.putLong("resolved_day", proposal.resolvedDay());

                final ListTag votes = new ListTag();
                proposal.councilVotes().forEach((seat, yes) -> {
                    final CompoundTag vote = new CompoundTag();
                    vote.putString("seat", seat);
                    vote.putBoolean("yes", yes);
                    votes.add(vote);
                });
                proposalTag.put("council_votes", votes);
                proposals.add(proposalTag);
            }
            entry.put("parliament_proposals", proposals);
            entries.add(entry);
        }
        root.put(TAG_COLONIES, entries);
        return root;
    }
}
