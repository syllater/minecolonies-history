package com.imperium.realms.colony;

import com.imperium.realms.economy.ImperialPolicy;
import com.imperium.realms.government.GovernmentType;
import com.imperium.realms.government.PolicyBill;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Shared Imperium registry stored on the overworld, keyed across dimensions. */
public final class EmpireStateSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_empire_state";
    private static final int SCHEMA_VERSION = 3;
    private static final Factory<EmpireStateSavedData> FACTORY = new Factory<>(
            EmpireStateSavedData::new, EmpireStateSavedData::load, DataFixTypes.LEVEL);
    private final Map<ColonyIdentity, EmpireState> colonies = new LinkedHashMap<>();

    public static EmpireStateSavedData get(final ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private EmpireStateSavedData() { }

    private static EmpireStateSavedData load(final CompoundTag root, final HolderLookup.Provider registries) {
        final EmpireStateSavedData data = new EmpireStateSavedData();
        final int schema = root.contains("schema_version", Tag.TAG_INT) ? root.getInt("schema_version") : 0;
        if (schema > SCHEMA_VERSION || schema < SCHEMA_VERSION) data.setDirty();
        if (!root.contains("colonies", Tag.TAG_LIST)) return data;

        final ListTag entries = root.getList("colonies", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            final CompoundTag entry = entries.getCompound(i);
            try {
                final ColonyIdentity identity = new ColonyIdentity(
                        entry.getString("dimension"), entry.getInt("colony_id"));
                final long firstSeen = Math.max(0L, entry.getLong("first_seen"));
                final long lastSeen = Math.max(firstSeen, entry.getLong("last_seen"));
                final ImperialPolicy policy = ImperialPolicy.fromId(entry.getString("policy"))
                        .orElse(ImperialPolicy.BALANCED);
                final long oldDay = EmpireState.gameDay(lastSeen);
                final long lastEconomyDay = entry.contains("last_economy_day", Tag.TAG_LONG)
                        ? entry.getLong("last_economy_day") : oldDay;
                final long lastDiplomaticDay = entry.contains("last_diplomatic_day", Tag.TAG_LONG)
                        ? entry.getLong("last_diplomatic_day") : -1L;
                final EmpireState state = new EmpireState(identity, entry.getString("colony_name"),
                        firstSeen, lastSeen, policy, entry.getLong("treasury_crowns"),
                        entry.getLong("diplomatic_influence"), lastEconomyDay, lastDiplomaticDay);
                // New governance fields are absent in v2 worlds; the state object
                // has safe constitutional-monarchy and faction-support defaults.
                state.readPoliticalData(entry);
                data.colonies.put(identity, state);
            } catch (IllegalArgumentException ignored) {
                // Skip malformed entries rather than aborting a world load.
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

    /** Idempotently initialize a record or refresh observed metadata only. */
    public boolean observeColony(final ColonyIdentity identity, final String name, final long gameTime) {
        final EmpireState existing = colonies.get(identity);
        if (existing == null) {
            colonies.put(identity, EmpireState.create(identity, name, gameTime));
            setDirty();
            return true;
        }
        if (existing.observe(name, gameTime)) setDirty();
        return false;
    }

    public boolean setPolicy(final ColonyIdentity identity, final ImperialPolicy policy) {
        final EmpireState state = colonies.get(identity);
        if (state == null || !state.setPolicy(policy)) return false;
        setDirty();
        return true;
    }

    public boolean setGovernmentType(final ColonyIdentity identity, final GovernmentType governmentType) {
        final EmpireState state = colonies.get(identity);
        if (state == null || !state.setGovernmentType(governmentType)) return false;
        setDirty();
        return true;
    }

    public Optional<PolicyBill> proposePolicy(
            final ColonyIdentity identity, final ImperialPolicy policy, final UUID proposer, final long gameTime) {
        final EmpireState state = colonies.get(identity);
        if (state == null) return Optional.empty();
        final PolicyBill bill = state.proposePolicy(policy, proposer, gameTime);
        if (bill == null) return Optional.empty();
        setDirty();
        return Optional.of(bill);
    }

    public PolicyBill.VoteResult castPolicyVote(
            final ColonyIdentity identity,
            final UUID voter,
            final boolean approve,
            final Set<UUID> electorate) {
        final EmpireState state = colonies.get(identity);
        if (state == null) return PolicyBill.VoteResult.NOT_OPEN;
        final PolicyBill before = state.currentBill().orElse(null);
        final Map<UUID, Boolean> priorVotes = before == null ? Map.of() : before.votes();
        final PolicyBill.VoteResult result = state.castPolicyVote(voter, approve, electorate);
        if (result == PolicyBill.VoteResult.RECORDED
                || result == PolicyBill.VoteResult.PASSED
                || result == PolicyBill.VoteResult.REJECTED) {
            setDirty();
        } else if (before != null && !priorVotes.equals(before.votes())) {
            // Future vote outcomes added here should also persist any real change.
            setDirty();
        }
        return result;
    }

    /** The daily marker is persisted even when a colony currently has no residents. */
    public long collectDailyTax(final ColonyIdentity identity, final long gameTime, final int residents) {
        final EmpireState state = colonies.get(identity);
        if (state == null || EmpireState.gameDay(gameTime) <= state.lastEconomyDay()) return 0L;
        final long income = state.collectDailyTax(gameTime, residents);
        setDirty();
        return income;
    }

    public boolean recordDiplomaticWork(final ColonyIdentity identity, final String name, final long gameTime) {
        observeColony(identity, name, gameTime);
        final EmpireState state = colonies.get(identity);
        if (state == null || !state.recordDiplomaticWork(gameTime)) return false;
        setDirty();
        return true;
    }

    public boolean creditCrowns(final ColonyIdentity identity, final long amount) {
        final EmpireState state = colonies.get(identity);
        if (state == null || !state.creditCrowns(amount)) return false;
        setDirty();
        return true;
    }

    public boolean spendCrowns(final ColonyIdentity identity, final long amount) {
        final EmpireState state = colonies.get(identity);
        if (state == null || !state.spendCrowns(amount)) return false;
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(final CompoundTag root, final HolderLookup.Provider registries) {
        root.putInt("schema_version", SCHEMA_VERSION);
        final ListTag entries = new ListTag();
        for (final EmpireState state : colonies.values()) {
            final CompoundTag entry = new CompoundTag();
            entry.putString("dimension", state.identity().dimensionId());
            entry.putInt("colony_id", state.identity().colonyId());
            entry.putString("colony_name", state.colonyName());
            entry.putLong("first_seen", state.firstSeenGameTime());
            entry.putLong("last_seen", state.lastSeenGameTime());
            entry.putString("policy", state.policy().id());
            entry.putLong("treasury_crowns", state.treasuryCrowns());
            entry.putLong("diplomatic_influence", state.diplomaticInfluence());
            entry.putLong("last_economy_day", state.lastEconomyDay());
            entry.putLong("last_diplomatic_day", state.lastDiplomaticDay());
            state.writePoliticalData(entry);
            entries.add(entry);
        }
        root.put("colonies", entries);
        return root;
    }
}
