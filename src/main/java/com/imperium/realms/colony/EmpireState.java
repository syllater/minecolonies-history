package com.imperium.realms.colony;

import com.imperium.realms.economy.ImperialPolicy;
import com.imperium.realms.government.GovernmentType;
import com.imperium.realms.government.ImperialFaction;
import com.imperium.realms.government.PolicyBill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Imperium-owned persistent state for one MineColonies colony. */
public final class EmpireState {
    private static final long TICKS_PER_DAY = 24_000L;

    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;
    private ImperialPolicy policy;
    private long treasuryCrowns;
    private long diplomaticInfluence;
    private long lastEconomyDay;
    private long lastDiplomaticDay;

    private GovernmentType governmentType = GovernmentType.CONSTITUTIONAL_MONARCHY;
    private final EnumMap<ImperialFaction, Integer> factionSupport = new EnumMap<>(ImperialFaction.class);
    private int nextBillId = 1;
    private PolicyBill currentBill;

    EmpireState(
            final ColonyIdentity identity, final String colonyName,
            final long firstSeenGameTime, final long lastSeenGameTime,
            final ImperialPolicy policy, final long treasuryCrowns,
            final long diplomaticInfluence, final long lastEconomyDay,
            final long lastDiplomaticDay) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.policy = Objects.requireNonNullElse(policy, ImperialPolicy.BALANCED);
        this.treasuryCrowns = Math.max(0L, treasuryCrowns);
        this.diplomaticInfluence = Math.max(0L, diplomaticInfluence);
        this.lastEconomyDay = Math.max(-1L, lastEconomyDay);
        this.lastDiplomaticDay = Math.max(-1L, lastDiplomaticDay);
        for (final ImperialFaction faction : ImperialFaction.values()) {
            factionSupport.put(faction, faction.defaultSupport());
        }
    }

    public ColonyIdentity identity() { return identity; }
    public String colonyName() { return colonyName; }
    public long firstSeenGameTime() { return firstSeenGameTime; }
    public long lastSeenGameTime() { return lastSeenGameTime; }
    public ImperialPolicy policy() { return policy; }
    public long treasuryCrowns() { return treasuryCrowns; }
    public long diplomaticInfluence() { return diplomaticInfluence; }
    public long lastEconomyDay() { return lastEconomyDay; }
    public long lastDiplomaticDay() { return lastDiplomaticDay; }
    public GovernmentType governmentType() { return governmentType; }
    public int nextBillId() { return nextBillId; }
    public Optional<PolicyBill> currentBill() { return Optional.ofNullable(currentBill); }

    public int factionSupport(final ImperialFaction faction) {
        return factionSupport.getOrDefault(Objects.requireNonNull(faction), faction.defaultSupport());
    }

    /** Aggregate faction approval is the initial government-stability score. */
    public int governmentStability() {
        int total = 0;
        for (final ImperialFaction faction : ImperialFaction.values()) total += factionSupport(faction);
        return total / ImperialFaction.values().length;
    }

    /** Unrest rises when the Commons or Guilds withdraw support. */
    public int unrestLevel() {
        return 100 - (factionSupport(ImperialFaction.COMMONS)
                + factionSupport(ImperialFaction.GUILDS)) / 2;
    }

    boolean observe(final String currentColonyName, final long gameTime) {
        boolean changed = false;
        final String name = normalizeName(currentColonyName);
        if (!colonyName.equals(name)) {
            colonyName = name;
            changed = true;
        }
        final long safeTime = Math.max(firstSeenGameTime, gameTime);
        if (gameDay(safeTime) > gameDay(lastSeenGameTime)) {
            lastSeenGameTime = safeTime;
            changed = true;
        }
        return changed;
    }

    static EmpireState create(final ColonyIdentity identity, final String name, final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        return new EmpireState(identity, name, safeTime, safeTime,
                ImperialPolicy.BALANCED, 0L, 0L, gameDay(safeTime), -1L);
    }

    public boolean setPolicy(final ImperialPolicy nextPolicy) {
        Objects.requireNonNull(nextPolicy, "nextPolicy");
        if (policy == nextPolicy) return false;
        policy = nextPolicy;
        return true;
    }

    public boolean setGovernmentType(final GovernmentType nextType) {
        Objects.requireNonNull(nextType, "nextType");
        if (governmentType == nextType) return false;
        governmentType = nextType;
        return true;
    }

    /**
     * Introduce a policy bill. Only one bill can be open at a time; after a
     * decision, a later bill may replace the retained result for status display.
     */
    public PolicyBill proposePolicy(
            final ImperialPolicy proposedPolicy, final UUID proposer, final long gameTime) {
        Objects.requireNonNull(proposedPolicy, "proposedPolicy");
        Objects.requireNonNull(proposer, "proposer");
        if (currentBill != null && currentBill.status() == PolicyBill.Status.OPEN) return null;
        currentBill = new PolicyBill(nextBillId++, proposedPolicy, proposer,
                Math.max(0L, gameTime), PolicyBill.Status.OPEN, Map.of());
        return currentBill;
    }

    /** Record a vote and enact the target policy only after a strict majority. */
    public PolicyBill.VoteResult castPolicyVote(
            final UUID voter, final boolean approve, final Set<UUID> electorate) {
        if (currentBill == null) return PolicyBill.VoteResult.NOT_OPEN;
        final PolicyBill.VoteResult result = currentBill.castVote(voter, approve, electorate);
        if (result == PolicyBill.VoteResult.PASSED) setPolicy(currentBill.proposedPolicy());
        return result;
    }

    /** Apply taxes and update faction support once per new Minecraft day. */
    long collectDailyTax(final long gameTime, final int residentCount) {
        final long day = gameDay(gameTime);
        if (day <= lastEconomyDay) return 0L;
        lastEconomyDay = day;
        updateFactionSupportForDailyPolicy();

        final long income = Math.max(0, residentCount) * (long) policy.crownsPerCitizenPerDay();
        if (income <= 0L) return 0L;
        final long previous = treasuryCrowns;
        treasuryCrowns = saturatedAdd(treasuryCrowns, income);
        return treasuryCrowns - previous;
    }

    boolean recordDiplomaticWork(final long gameTime) {
        final long day = gameDay(gameTime);
        if (day <= lastDiplomaticDay) return false;
        lastDiplomaticDay = day;
        if (diplomaticInfluence < Long.MAX_VALUE) diplomaticInfluence++;
        return true;
    }

    public boolean canCreditCrowns(final long amount) {
        return amount > 0L && amount <= Long.MAX_VALUE - treasuryCrowns;
    }

    boolean creditCrowns(final long amount) {
        if (!canCreditCrowns(amount)) return false;
        treasuryCrowns += amount;
        return true;
    }

    boolean spendCrowns(final long amount) {
        if (amount <= 0L || amount > treasuryCrowns) return false;
        treasuryCrowns -= amount;
        return true;
    }

    void readPoliticalData(final CompoundTag entry) {
        if (entry.contains("government_type", Tag.TAG_STRING)) {
            governmentType = GovernmentType.fromId(entry.getString("government_type"))
                    .orElse(GovernmentType.CONSTITUTIONAL_MONARCHY);
        }
        if (entry.contains("faction_support", Tag.TAG_COMPOUND)) {
            final CompoundTag support = entry.getCompound("faction_support");
            for (final ImperialFaction faction : ImperialFaction.values()) {
                if (support.contains(faction.id(), Tag.TAG_INT)) {
                    factionSupport.put(faction, clampSupport(support.getInt(faction.id())));
                }
            }
        }
        nextBillId = Math.max(1, entry.getInt("next_bill_id"));
        if (!entry.contains("current_bill", Tag.TAG_COMPOUND)) return;

        final CompoundTag billTag = entry.getCompound("current_bill");
        final Optional<ImperialPolicy> target = ImperialPolicy.fromId(billTag.getString("policy"));
        final UUID proposer = parseUuid(billTag.getString("proposer"));
        if (target.isEmpty() || proposer == null || !billTag.contains("id", Tag.TAG_INT)) return;

        final Map<UUID, Boolean> votes = new LinkedHashMap<>();
        final ListTag voteTags = billTag.getList("votes", Tag.TAG_COMPOUND);
        for (int i = 0; i < voteTags.size(); i++) {
            final CompoundTag vote = voteTags.getCompound(i);
            final UUID voter = parseUuid(vote.getString("voter"));
            if (voter != null && vote.contains("approve", Tag.TAG_BYTE)) {
                votes.put(voter, vote.getBoolean("approve"));
            }
        }
        final int id = Math.max(1, billTag.getInt("id"));
        currentBill = new PolicyBill(id, target.get(), proposer,
                Math.max(0L, billTag.getLong("created_game_time")),
                PolicyBill.Status.fromId(billTag.getString("status")), votes);
        nextBillId = Math.max(nextBillId, id + 1);
    }

    void writePoliticalData(final CompoundTag entry) {
        entry.putString("government_type", governmentType.id());
        final CompoundTag support = new CompoundTag();
        for (final ImperialFaction faction : ImperialFaction.values()) {
            support.putInt(faction.id(), factionSupport(faction));
        }
        entry.put("faction_support", support);
        entry.putInt("next_bill_id", nextBillId);
        if (currentBill == null) return;

        final CompoundTag bill = new CompoundTag();
        bill.putInt("id", currentBill.id());
        bill.putString("policy", currentBill.proposedPolicy().id());
        bill.putString("proposer", currentBill.proposer().toString());
        bill.putLong("created_game_time", currentBill.createdGameTime());
        bill.putString("status", currentBill.status().id());
        final ListTag voteTags = new ListTag();
        currentBill.votes().forEach((voter, approve) -> {
            final CompoundTag vote = new CompoundTag();
            vote.putString("voter", voter.toString());
            vote.putBoolean("approve", approve);
            voteTags.add(vote);
        });
        bill.put("votes", voteTags);
        entry.put("current_bill", bill);
    }

    private void updateFactionSupportForDailyPolicy() {
        switch (policy) {
            case LOW_TAX -> {
                adjustSupport(ImperialFaction.COMMONS, 2);
                adjustSupport(ImperialFaction.GUILDS, 1);
                adjustSupport(ImperialFaction.CROWN, -1);
            }
            case BALANCED -> adjustSupport(ImperialFaction.CROWN, 1);
            case EMERGENCY_LEVY -> {
                adjustSupport(ImperialFaction.CROWN, 2);
                adjustSupport(ImperialFaction.GUILDS, -2);
                adjustSupport(ImperialFaction.COMMONS, -3);
            }
        }
    }

    private void adjustSupport(final ImperialFaction faction, final int delta) {
        factionSupport.put(faction, clampSupport(factionSupport(faction) + delta));
    }

    private static int clampSupport(final int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static UUID parseUuid(final String value) {
        try {
            return value == null || value.isBlank() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static long gameDay(final long gameTime) {
        return Math.floorDiv(Math.max(0L, gameTime), TICKS_PER_DAY);
    }

    private static long saturatedAdd(final long current, final long amount) {
        return amount > Long.MAX_VALUE - current ? Long.MAX_VALUE : current + amount;
    }

    private static String normalizeName(final String name) {
        return name == null || name.isBlank() ? "Unnamed colony" : name.trim();
    }
}
