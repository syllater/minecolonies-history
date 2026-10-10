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
    private static final int SCHEMA_VERSION = 19;

    public enum PetitionResolutionResult {
        RESOLVED, REALM_NOT_FOUND, NOT_MEMBER, CAPITAL_PROVINCE, NO_PETITION, INSUFFICIENT_TREASURY
    }
    private static final String TAG_SCHEMA_VERSION = "schema_version";
    private static final String TAG_COLONIES = "colonies";

    private static final Factory<EmpireStateSavedData> FACTORY = new Factory<>(
            EmpireStateSavedData::new,
            EmpireStateSavedData::load,
            DataFixTypes.LEVEL);

    private final Map<ColonyIdentity, EmpireState> colonies = new LinkedHashMap<>();
    private final Map<Long, EmpireRealm> realms = new LinkedHashMap<>();
    private long nextRealmId = 1L;

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

        final ListTag entries = root.contains(TAG_COLONIES, Tag.TAG_LIST)
                ? root.getList(TAG_COLONIES, Tag.TAG_COMPOUND) : new ListTag();
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
                state.restoreMilitaryPosture(MilitaryPosture.fromId(entry.getString("military_posture"))
                        .orElse(MilitaryPosture.BALANCED));

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
                state.restoreMilitaryTraining(
                        entry.contains("siege_engineering_points")
                                ? entry.getLong("siege_engineering_points") : 0L,
                        entry.contains("field_medicine_points")
                                ? entry.getLong("field_medicine_points") : 0L,
                        entry.contains("cavalry_drill_points")
                                ? entry.getLong("cavalry_drill_points") : 0L);
                state.restoreProvinceState(
                        ProvinceFocus.fromId(entry.getString("province_focus"))
                                .orElse(ProvinceFocus.AGRICULTURE),
                        entry.contains("province_development") ? entry.getInt("province_development") : 0);

                final List<MilitaryCampaign> campaigns = new ArrayList<>();
                if (entry.contains("military_campaigns", Tag.TAG_LIST)) {
                    final ListTag savedCampaigns = entry.getList("military_campaigns", Tag.TAG_COMPOUND);
                    for (int campaignIndex = 0; campaignIndex < savedCampaigns.size(); campaignIndex++) {
                        final CompoundTag campaignTag = savedCampaigns.getCompound(campaignIndex);
                        try {
                            final MilitaryCampaign.Type type = MilitaryCampaign.Type
                                    .fromId(campaignTag.getString("type")).orElse(null);
                            if (type == null) {
                                continue;
                            }
                            final ColonyIdentity target = new ColonyIdentity(
                                    campaignTag.getString("target_dimension"),
                                    campaignTag.getInt("target_colony_id"));
                            MilitaryCampaign.Outcome outcome = MilitaryCampaign.Outcome.PENDING;
                            try {
                                if (campaignTag.contains("outcome", Tag.TAG_STRING)) {
                                    outcome = MilitaryCampaign.Outcome.valueOf(
                                            campaignTag.getString("outcome"));
                                }
                            } catch (IllegalArgumentException ignored) {
                                // Unknown future outcomes are kept pending and can expire by normal resolution.
                            }
                            campaigns.add(MilitaryCampaign.restore(
                                    campaignTag.getLong("id"),
                                    type,
                                    target,
                                    campaignTag.getString("target_name"),
                                    campaignTag.getString("commander"),
                                    campaignTag.getLong("started_day"),
                                    campaignTag.getLong("resolves_day"),
                                    campaignTag.getInt("launch_readiness"),
                                    outcome,
                                    campaignTag.contains("resolved_day")
                                            ? campaignTag.getLong("resolved_day") : -1L));
                        } catch (IllegalArgumentException exception) {
                            // Ignore malformed operations without discarding the colony's economy or politics.
                        }
                    }
                }
                state.restoreMilitaryCampaigns(
                        campaigns,
                        entry.contains("next_campaign_id") ? entry.getLong("next_campaign_id") : 1L);
                data.colonies.put(identity, state);
            } catch (IllegalArgumentException exception) {
                // Skip malformed records instead of failing the whole world load.
            }
        }

        data.nextRealmId = root.contains("next_realm_id", Tag.TAG_LONG)
                ? Math.max(1L, root.getLong("next_realm_id")) : 1L;
        if (root.contains("realms", Tag.TAG_LIST)) {
            final ListTag savedRealms = root.getList("realms", Tag.TAG_COMPOUND);
            for (int realmIndex = 0; realmIndex < savedRealms.size(); realmIndex++) {
                final CompoundTag realmTag = savedRealms.getCompound(realmIndex);
                try {
                    final long realmId = realmTag.getLong("id");
                    final ColonyIdentity capital = new ColonyIdentity(
                            realmTag.getString("capital_dimension"),
                            realmTag.getInt("capital_colony_id"));
                    if (realmId < 1L || data.realms.containsKey(realmId)
                            || data.realmForProvince(capital).isPresent()) {
                        continue;
                    }

                    final List<ColonyIdentity> memberProvinces = new ArrayList<>();
                    if (realmTag.contains("provinces", Tag.TAG_LIST)) {
                        final ListTag members = realmTag.getList("provinces", Tag.TAG_COMPOUND);
                        for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) {
                            final CompoundTag member = members.getCompound(memberIndex);
                            try {
                                final ColonyIdentity identity = new ColonyIdentity(
                                        member.getString("dimension"), member.getInt("colony_id"));
                                if (!data.realmForProvince(identity).isPresent()) {
                                    memberProvinces.add(identity);
                                }
                            } catch (IllegalArgumentException ignored) {
                                // Ignore a malformed member; preserve the rest of the realm.
                            }
                        }
                    }

                    final Map<ColonyIdentity, Long> invitations = new LinkedHashMap<>();
                    if (realmTag.contains("invitations", Tag.TAG_LIST)) {
                        final ListTag savedInvitations = realmTag.getList("invitations", Tag.TAG_COMPOUND);
                        for (int invitationIndex = 0; invitationIndex < savedInvitations.size(); invitationIndex++) {
                            final CompoundTag invitation = savedInvitations.getCompound(invitationIndex);
                            try {
                                final ColonyIdentity identity = new ColonyIdentity(
                                        invitation.getString("dimension"), invitation.getInt("colony_id"));
                                if (!memberProvinces.contains(identity) && !capital.equals(identity)) {
                                    invitations.put(identity, Math.max(0L, invitation.getLong("expires_day")));
                                }
                            } catch (IllegalArgumentException ignored) {
                                // Ignore malformed invitations individually.
                            }
                        }
                    }

                    final List<ImperialAuditEntry> auditEntries = new ArrayList<>();
                    if (realmTag.contains("audit_entries", Tag.TAG_LIST)) {
                        final ListTag savedAudit = realmTag.getList("audit_entries", Tag.TAG_COMPOUND);
                        for (int auditIndex = 0; auditIndex < savedAudit.size(); auditIndex++) {
                            final CompoundTag auditTag = savedAudit.getCompound(auditIndex);
                            try {
                                auditEntries.add(new ImperialAuditEntry(
                                        Math.max(0L, auditTag.getLong("day")),
                                        auditTag.getString("actor"),
                                        auditTag.getString("action"),
                                        auditTag.getString("subject"),
                                        Math.max(0L, auditTag.getLong("amount"))));
                            } catch (IllegalArgumentException ignored) {
                                // Skip malformed audit entries without losing realm membership.
                            }
                        }
                    }

                    final Map<ColonyIdentity, ProvinceGovernor> governors = new LinkedHashMap<>();
                    if (realmTag.contains("governors", Tag.TAG_LIST)) {
                        final ListTag savedGovernors = realmTag.getList("governors", Tag.TAG_COMPOUND);
                        for (int governorIndex = 0; governorIndex < savedGovernors.size(); governorIndex++) {
                            final CompoundTag governorTag = savedGovernors.getCompound(governorIndex);
                            try {
                                final ColonyIdentity province = new ColonyIdentity(
                                        governorTag.getString("dimension"), governorTag.getInt("colony_id"));
                                governors.put(province, new ProvinceGovernor(
                                        governorTag.getString("player_uuid"),
                                        governorTag.getString("player_name"),
                                        Math.max(0L, governorTag.getLong("appointed_day"))));
                            } catch (IllegalArgumentException ignored) {
                                // Skip one malformed appointment without losing other provinces.
                            }
                        }
                    }

                    final Map<ColonyIdentity, Integer> loyalty = new LinkedHashMap<>();
                    if (realmTag.contains("provincial_loyalty", Tag.TAG_LIST)) {
                        final ListTag list = realmTag.getList("provincial_loyalty", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            final CompoundTag tag = list.getCompound(i);
                            try { loyalty.put(new ColonyIdentity(tag.getString("dimension"), tag.getInt("colony_id")), tag.getInt("value")); }
                            catch (IllegalArgumentException ignored) { /* Skip malformed loyalty. */ }
                        }
                    }
                    final Map<ColonyIdentity, Integer> pressureDays = new LinkedHashMap<>();
                    if (realmTag.contains("loyalty_pressure_days", Tag.TAG_LIST)) {
                        final ListTag list = realmTag.getList("loyalty_pressure_days", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            final CompoundTag tag = list.getCompound(i);
                            try { pressureDays.put(new ColonyIdentity(tag.getString("dimension"), tag.getInt("colony_id")), tag.getInt("days")); }
                            catch (IllegalArgumentException ignored) { /* Skip malformed pressure data. */ }
                        }
                    }
                    final Map<ColonyIdentity, Long> petitions = new LinkedHashMap<>();
                    if (realmTag.contains("separatist_petitions", Tag.TAG_LIST)) {
                        final ListTag list = realmTag.getList("separatist_petitions", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            final CompoundTag tag = list.getCompound(i);
                            try { petitions.put(new ColonyIdentity(tag.getString("dimension"), tag.getInt("colony_id")), Math.max(0L, tag.getLong("day"))); }
                            catch (IllegalArgumentException ignored) { /* Skip malformed petitions. */ }
                        }
                    }

                    final EmpireRealm realm = EmpireRealm.restore(
                            realmId,
                            realmTag.getString("name"),
                            capital,
                            realmTag.getString("emperor_uuid"),
                            realmTag.getString("emperor_name"),
                            Math.max(0L, realmTag.getLong("founded_day")),
                            Math.max(0L, realmTag.getLong("imperial_treasury")),
                            memberProvinces,
                            invitations,
                            realmTag.contains("imperial_tax_rate", Tag.TAG_INT)
                                    ? realmTag.getInt("imperial_tax_rate") : -1,
                            realmTag.getString("imperial_policy"),
                            auditEntries,
                            governors,
                            Math.max(0L, realmTag.getLong("last_regional_event_day")));
                    realm.restoreCohesionState(loyalty, pressureDays, petitions,
                            Math.max(0L, realmTag.getLong("last_cohesion_day")));
                    data.realms.put(realmId, realm);
                    if (realmId < Long.MAX_VALUE) {
                        data.nextRealmId = Math.max(data.nextRealmId, realmId + 1L);
                    }
                } catch (IllegalArgumentException exception) {
                    // An invalid realm must not prevent the world and its colonies from loading.
                }
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

    /**
     * Resolve due operations for colony records stored in the same world index.
     * The operation and both colonies' consequences are saved atomically in
     * this SavedData instance.
     *
     * @return number of operations resolved during this daily turn.
     */
    /** Advance province loyalty once per in-game day; prolonged low loyalty raises a petition. */
    public int updateProvincialLoyalty(final long dayIndex) {
        if (dayIndex < 0L) return 0;
        int processed = 0;
        for (final EmpireRealm realm : new ArrayList<>(realms.values())) {
            if (!realm.beginCohesionTurn(dayIndex)) continue;
            processed++;
            for (final ColonyIdentity province : realm.provinces()) {
                final EmpireState state = colonies.get(province);
                if (state == null) continue;
                int delta = 0;
                if (state.stability() >= 65 && state.legitimacy() >= 60 && state.unrest() < 25) delta++;
                if (state.stability() < 35) delta--;
                if (state.legitimacy() < 35) delta--;
                if (state.unrest() >= 60) delta -= 2;
                else if (state.unrest() >= 35) delta--;
                if (realm.hasGovernor(province)) delta++;
                if (state.taxRatePercent() >= 15) delta--;
                if (state.economicPolicy() == EconomicPolicy.WELFARE) delta++;
                else if (state.economicPolicy() == EconomicPolicy.AUSTERITY) delta--;
                if (realm.updateProvinceLoyalty(province, delta, dayIndex)) {
                    realm.recordAudit(dayIndex, "System", "separatist-petition", province.storageKey(), realm.provincialLoyalty(province));
                }
            }
        }
        if (processed > 0) setDirty();
        return processed;
    }

    public PetitionResolutionResult resolveSeparatistPetition(final long realmId, final ColonyIdentity province,
            final boolean crackdown, final String actor, final long dayIndex) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null) return PetitionResolutionResult.REALM_NOT_FOUND;
        if (province == null || !colonies.containsKey(province) || !realm.containsProvince(province)) return PetitionResolutionResult.NOT_MEMBER;
        if (realm.capital().equals(province)) return PetitionResolutionResult.CAPITAL_PROVINCE;
        if (!realm.hasSeparatistPetition(province)) return PetitionResolutionResult.NO_PETITION;
        final long cost = crackdown ? 25L : 50L;
        if (realm.imperialTreasuryCrowns() < cost || !realm.withdrawImperialTreasury(cost)) return PetitionResolutionResult.INSUFFICIENT_TREASURY;
        final EmpireState state = colonies.get(province);
        if (!realm.resolveSeparatistPetition(province, crackdown ? 10 : 25)) {
            realm.depositImperialTreasury(cost);
            return PetitionResolutionResult.NO_PETITION;
        }
        if (crackdown) state.adjustPoliticalMetrics(-5, -10, 15);
        else state.adjustPoliticalMetrics(3, 5, -10);
        realm.recordAudit(dayIndex, actor, crackdown ? "petition-crackdown" : "petition-reassured", province.storageKey(), cost);
        setDirty();
        return PetitionResolutionResult.RESOLVED;
    }

    /** Resolves at most one deterministic realm event every seven in-game days. */
    public int resolveDueRegionalEvents(final long dayIndex) {
        if (dayIndex < 0L) return 0;
        int resolved = 0;
        for (final EmpireRealm realm : new ArrayList<>(realms.values())) {
            if (!realm.isRegionalEventDue(dayIndex) || !realm.markRegionalEvent(dayIndex)) continue;
            final ImperialRegionalEvent regionalEvent = ImperialRegionalEvent.forTurn(realm.id(), dayIndex);
            int provincesAffected = 0;
            for (final ColonyIdentity province : realm.provinces()) {
                final EmpireState state = colonies.get(province);
                if (state != null) { state.applyRegionalEvent(regionalEvent); provincesAffected++; }
            }
            final long centralDelta = regionalEvent.imperialTreasuryDelta();
            if (centralDelta > 0L) {
                final long room = EmpireRealm.MAX_IMPERIAL_TREASURY - realm.imperialTreasuryCrowns();
                if (room > 0L) realm.depositImperialTreasury(Math.min(room, centralDelta));
            } else if (centralDelta < 0L) {
                realm.withdrawImperialTreasury(Math.min(realm.imperialTreasuryCrowns(), -centralDelta));
            }
            realm.recordAudit(dayIndex, "System", "regional-event", regionalEvent.id(), provincesAffected);
            resolved++;
        }
        if (resolved > 0) setDirty();
        return resolved;
    }

    public int resolveDueMilitaryCampaigns(final long dayIndex) {
        int resolved = 0;
        for (final EmpireState source : new ArrayList<>(colonies.values())) {
            for (final MilitaryCampaign campaign : source.pendingMilitaryCampaigns()) {
                final EmpireState target = colonies.get(campaign.targetIdentity());
                if (target == null) {
                    continue;
                }
                if (source.resolveMilitaryCampaign(campaign.id(), target, dayIndex).isPresent()) {
                    resolved++;
                }
            }
        }
        if (resolved > 0) {
            setDirty();
        }
        return resolved;
    }

    public List<EmpireRealm> allRealms() {
        return List.copyOf(realms.values());
    }

    public Optional<EmpireRealm> realmById(final long realmId) {
        return Optional.ofNullable(realms.get(realmId));
    }

    public Optional<EmpireRealm> realmForProvince(final ColonyIdentity identity) {
        if (identity == null) {
            return Optional.empty();
        }
        return realms.values().stream().filter(realm -> realm.containsProvince(identity)).findFirst();
    }

    public Optional<EmpireRealm> createRealm(
            final String name,
            final ColonyIdentity capital,
            final String emperorUuid,
            final String emperorName,
            final long foundedDay) {
        if (capital == null || emperorUuid == null || emperorUuid.isBlank()
                || nextRealmId <= 0L || nextRealmId == Long.MAX_VALUE
                || realmForProvince(capital).isPresent()
                || realms.values().stream().anyMatch(
                        realm -> realm.name().equalsIgnoreCase(name == null ? "" : name.trim()))) {
            return Optional.empty();
        }
        if (!colonies.containsKey(capital)) {
            return Optional.empty();
        }

        final EmpireRealm realm;
        try {
            realm = EmpireRealm.found(
                    nextRealmId, name, capital, emperorUuid, emperorName, Math.max(0L, foundedDay));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        nextRealmId++;
        realms.put(realm.id(), realm);
        setDirty();
        return Optional.of(realm);
    }

    public boolean inviteProvince(
            final long realmId,
            final ColonyIdentity province,
            final long currentDay) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null || province == null || !colonies.containsKey(province)
                || realmForProvince(province).isPresent()) {
            return false;
        }
        boolean changed = false;
        for (final EmpireRealm other : realms.values()) {
            if (other.expireInvitations(currentDay)) {
                changed = true;
            }
            if (other.invitationExpiry(province) >= 0L && other.id() != realmId) {
                if (changed) setDirty();
                return false;
            }
        }
        if (!realm.inviteProvince(province, currentDay)) {
            if (changed) setDirty();
            return false;
        }
        setDirty();
        return true;
    }

    public Optional<EmpireRealm> acceptRealmInvitation(
            final ColonyIdentity province,
            final long currentDay) {
        if (province == null || !colonies.containsKey(province) || realmForProvince(province).isPresent()) {
            return Optional.empty();
        }
        EmpireRealm selected = null;
        boolean invitationsExpired = false;
        for (final EmpireRealm realm : realms.values()) {
            invitationsExpired |= realm.expireInvitations(currentDay);
            if (selected == null && realm.hasValidInvitation(province, currentDay)) {
                selected = realm;
            }
        }
        if (selected == null || !selected.acceptInvitation(province, currentDay)) {
            if (invitationsExpired) {
                setDirty();
            }
            return Optional.empty();
        }
        final EmpireState joiningState = colonies.get(province);
        if (joiningState != null) {
            if (selected.hasImperialTaxLaw()) {
                joiningState.setTaxRatePercent(selected.imperialTaxRatePercent());
            }
            if (selected.hasImperialPolicyLaw()) {
                EconomicPolicy.fromId(selected.imperialEconomicPolicyId())
                        .ifPresent(joiningState::setEconomicPolicy);
            }
        }
        setDirty();
        return Optional.of(selected);
    }

    /**
     * Applies an enacted imperial tax law to every current province. The capital's
     * Parliament calls this only after a bill passes its council and imperial assent.
     */
    public boolean applyImperialTaxLaw(final long realmId, final int taxRatePercent) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null || taxRatePercent < 0 || taxRatePercent > 25) {
            return false;
        }
        boolean changed = realm.setImperialTaxRatePercent(taxRatePercent);
        for (final ColonyIdentity province : realm.provinces()) {
            final EmpireState state = colonies.get(province);
            if (state != null) {
                changed |= state.setTaxRatePercent(taxRatePercent);
            }
        }
        if (changed) {
            setDirty();
        }
        return true;
    }

    /** Applies an enacted imperial economic policy to every current province. */
    public boolean applyImperialPolicyLaw(final long realmId, final EconomicPolicy policy) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null || policy == null) {
            return false;
        }
        boolean changed = realm.setImperialEconomicPolicy(policy);
        for (final ColonyIdentity province : realm.provinces()) {
            final EmpireState state = colonies.get(province);
            if (state != null) {
                changed |= state.setEconomicPolicy(policy);
            }
        }
        if (changed) {
            setDirty();
        }
        return true;
    }

    /**
     * Transfers a bounded share of tax receipts already credited to a member province
     * into its realm reserve. If the reserve is full or a debit fails, no money is lost.
     *
     * @return crowns transferred into the imperial treasury.
     */
    public long remitImperialTaxReceipts(
            final ColonyIdentity province,
            final long collectedTaxReceipts,
            final long dayIndex) {
        if (province == null || collectedTaxReceipts <= 0L) {
            return 0L;
        }
        final EmpireState state = colonies.get(province);
        final EmpireRealm realm = realmForProvince(province).orElse(null);
        if (state == null || realm == null) {
            return 0L;
        }
        final long remittance = realm.calculateImperialTaxRemittance(collectedTaxReceipts);
        if (remittance <= 0L || !state.debitTreasury(remittance)) {
            return 0L;
        }
        if (!realm.depositImperialTreasury(remittance)) {
            // The provincial debit guarantees at least this much treasury capacity
            // is available for rollback unless an implementation invariant is broken.
            state.creditTreasury(remittance);
            return 0L;
        }
        realm.recordAudit(dayIndex, "System", "tax-remittance", province.storageKey(), remittance);
        setDirty();
        return remittance;
    }

    /** Assigns a player as governor of a non-capital province in this realm. */
    public boolean appointGovernor(final long realmId, final ColonyIdentity province,
            final String playerUuid, final String playerName, final long currentDay) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null || !colonies.containsKey(province)
                || !realm.appointGovernor(province, playerUuid, playerName, currentDay)) {
            return false;
        }
        realm.recordAudit(currentDay, playerName, "governor-appointed", province.storageKey(), 0L);
        setDirty();
        return true;
    }

    /** Removes the current governor appointment from a member province. */
    public boolean dismissGovernor(final long realmId, final ColonyIdentity province,
            final String actor, final long currentDay) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null || !realm.dismissGovernor(province)) return false;
        realm.recordAudit(currentDay, actor, "governor-dismissed", province.storageKey(), 0L);
        setDirty();
        return true;
    }

    /** Adds a bounded audit entry to a realm and persists the change. */
    public boolean recordImperialAudit(
            final long realmId,
            final long dayIndex,
            final String actor,
            final String actionId,
            final String subject,
            final long amount) {
        final EmpireRealm realm = realms.get(realmId);
        if (realm == null) return false;
        realm.recordAudit(dayIndex, actor, actionId, subject, amount);
        setDirty();
        return true;
    }

    public boolean leaveRealm(final ColonyIdentity province) {
        final EmpireRealm realm = realmForProvince(province).orElse(null);
        if (realm == null || !realm.removeProvince(province)) {
            return false;
        }
        setDirty();
        return true;
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
            entry.putLong("siege_engineering_points", state.siegeEngineeringPoints());
            entry.putLong("field_medicine_points", state.fieldMedicinePoints());
            entry.putLong("cavalry_drill_points", state.cavalryDrillPoints());
            entry.putString("military_posture", state.militaryPosture().id());
            entry.putLong("last_tax_day", state.lastTaxDay());
            entry.putString("province_focus", state.provinceFocus().id());
            entry.putInt("province_development", state.provinceDevelopmentPoints());

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

            entry.putLong("next_campaign_id", state.nextCampaignId());
            final ListTag campaigns = new ListTag();
            for (final MilitaryCampaign campaign : state.storedMilitaryCampaigns()) {
                final CompoundTag campaignTag = new CompoundTag();
                campaignTag.putLong("id", campaign.id());
                campaignTag.putString("type", campaign.type().id());
                campaignTag.putString("target_dimension", campaign.targetIdentity().dimensionId());
                campaignTag.putInt("target_colony_id", campaign.targetIdentity().colonyId());
                campaignTag.putString("target_name", campaign.targetName());
                campaignTag.putString("commander", campaign.commander());
                campaignTag.putLong("started_day", campaign.startedDay());
                campaignTag.putLong("resolves_day", campaign.resolvesDay());
                campaignTag.putInt("launch_readiness", campaign.launchReadiness());
                campaignTag.putString("outcome", campaign.outcome().name());
                campaignTag.putLong("resolved_day", campaign.resolvedDay());
                campaigns.add(campaignTag);
            }
            entry.put("military_campaigns", campaigns);
            entries.add(entry);
        }
        root.put(TAG_COLONIES, entries);

        root.putLong("next_realm_id", nextRealmId);
        final ListTag savedRealms = new ListTag();
        for (final EmpireRealm realm : realms.values()) {
            final CompoundTag realmTag = new CompoundTag();
            realmTag.putLong("id", realm.id());
            realmTag.putString("name", realm.name());
            realmTag.putString("capital_dimension", realm.capital().dimensionId());
            realmTag.putInt("capital_colony_id", realm.capital().colonyId());
            realmTag.putString("emperor_uuid", realm.emperorUuid());
            realmTag.putString("emperor_name", realm.emperorName());
            realmTag.putLong("founded_day", realm.foundedDay());
            realmTag.putLong("last_regional_event_day", realm.lastRegionalEventDay());
            realmTag.putLong("last_cohesion_day", realm.lastCohesionDay());
            realmTag.putLong("imperial_treasury", realm.imperialTreasuryCrowns());
            realmTag.putInt("imperial_tax_rate", realm.imperialTaxRatePercent());
            realmTag.putString("imperial_policy", realm.imperialEconomicPolicyId());

            final ListTag members = new ListTag();
            for (final ColonyIdentity province : realm.provinces()) {
                final CompoundTag member = new CompoundTag();
                member.putString("dimension", province.dimensionId());
                member.putInt("colony_id", province.colonyId());
                members.add(member);
            }
            realmTag.put("provinces", members);

            final ListTag invitations = new ListTag();
            realm.invitations().forEach((province, expiresDay) -> {
                final CompoundTag invitation = new CompoundTag();
                invitation.putString("dimension", province.dimensionId());
                invitation.putInt("colony_id", province.colonyId());
                invitation.putLong("expires_day", expiresDay);
                invitations.add(invitation);
            });
            realmTag.put("invitations", invitations);

            final ListTag auditEntries = new ListTag();
            for (final ImperialAuditEntry audit : realm.storedAuditEntries()) {
                final CompoundTag auditTag = new CompoundTag();
                auditTag.putLong("day", audit.dayIndex());
                auditTag.putString("actor", audit.actor());
                auditTag.putString("action", audit.actionId());
                auditTag.putString("subject", audit.subject());
                auditTag.putLong("amount", audit.amount());
                auditEntries.add(auditTag);
            }
            realmTag.put("audit_entries", auditEntries);

            final ListTag savedGovernors = new ListTag();
            for (final Map.Entry<ColonyIdentity, ProvinceGovernor> governor : realm.governors().entrySet()) {
                final CompoundTag governorTag = new CompoundTag();
                governorTag.putString("dimension", governor.getKey().dimensionId());
                governorTag.putInt("colony_id", governor.getKey().colonyId());
                governorTag.putString("player_uuid", governor.getValue().playerUuid());
                governorTag.putString("player_name", governor.getValue().playerName());
                governorTag.putLong("appointed_day", governor.getValue().appointedDay());
                savedGovernors.add(governorTag);
            }
            realmTag.put("governors", savedGovernors);

            final ListTag loyalty = new ListTag();
            realm.provincialLoyalties().forEach((province, value) -> {
                final CompoundTag tag = new CompoundTag();
                tag.putString("dimension", province.dimensionId()); tag.putInt("colony_id", province.colonyId()); tag.putInt("value", value);
                loyalty.add(tag);
            });
            realmTag.put("provincial_loyalty", loyalty);
            final ListTag pressure = new ListTag();
            realm.separatistPressureDays().forEach((province, days) -> {
                final CompoundTag tag = new CompoundTag();
                tag.putString("dimension", province.dimensionId()); tag.putInt("colony_id", province.colonyId()); tag.putInt("days", days);
                pressure.add(tag);
            });
            realmTag.put("loyalty_pressure_days", pressure);
            final ListTag petitions = new ListTag();
            realm.separatistPetitions().forEach((province, day) -> {
                final CompoundTag tag = new CompoundTag();
                tag.putString("dimension", province.dimensionId()); tag.putInt("colony_id", province.colonyId()); tag.putLong("day", day);
                petitions.add(tag);
            });
            realmTag.put("separatist_petitions", petitions);
            savedRealms.add(realmTag);
        }
        root.put("realms", savedRealms);
        return root;
    }
}
