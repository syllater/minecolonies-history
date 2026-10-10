package com.imperium.realms.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Imperium-owned state for one MineColonies colony. All mutable state in this
 * object is persisted by EmpireStateSavedData, never in MineColonies-private NBT.
 */
public final class EmpireState {
    public static final long MAX_TREASURY = 1_000_000_000L;
    private static final int MIN_TAX_RATE = 0;
    private static final int MAX_TAX_RATE = 25;
    private static final long SCHOLAR_WORK_INTERVAL_TICKS = 1_200L;
    private static final long TAX_COLLECTOR_WORK_INTERVAL_TICKS = 1_200L;
    private static final int MAX_TAX_COLLECTION_EFFICIENCY_PERCENT = 25;
    private static final long DIPLOMAT_WORK_INTERVAL_TICKS = 2_400L;
    public static final long MAX_DIPLOMATIC_INFLUENCE = 1_000L;
    private static final int MAX_DIPLOMATIC_RELATIONS = 256;
    private static final int MAX_STORED_PROPOSALS = 20;

    public enum ProposalResolution {
        PASSED,
        REJECTED,
        EXPIRED,
        NOT_FOUND,
        ALREADY_RESOLVED
    }

    public enum CivicDisorder {
        CALM,
        STRIKE,
        REVOLT
    }

    public enum MilitaryDiscipline {
        SIEGE_ENGINEERING,
        FIELD_MEDICINE,
        CAVALRY_DRILL
    }

    public static final long MAX_MILITARY_TRAINING_POINTS = 1_000L;

    private static final String[] FACTION_IDS = {"merchants", "commons", "nobility", "scholars"};

    private final ColonyIdentity identity;
    private String colonyName;
    private final long firstSeenGameTime;
    private long lastSeenGameTime;

    private long treasuryCrowns;
    private int taxRatePercent = 5;
    private EconomicPolicy economicPolicy = EconomicPolicy.BALANCED;
    private long knowledgePoints;
    private int stability = 50;
    private int legitimacy = 50;
    private long lastTaxDay = -1L;
    private long lastScholarWorkTick = -1L;
    private long lastTaxCollectorWorkTick = -1L;
    private int taxCollectionEfficiencyPercent;
    private long diplomaticInfluence;
    private long lastDiplomatWorkTick = -1L;
    private final Map<ColonyIdentity, Integer> diplomaticRelations = new LinkedHashMap<>();
    private final Map<String, Integer> factionApproval = new LinkedHashMap<>();
    private int unrest;
    private CivicDisorder civicDisorder = CivicDisorder.CALM;
    private long lastCivicDisorderChangeDay = -1L;
    private long siegeEngineeringPoints;
    private long fieldMedicinePoints;
    private long cavalryDrillPoints;

    private ProvinceFocus provinceFocus = ProvinceFocus.AGRICULTURE;
    private int provinceDevelopmentPoints;

    private long nextCampaignId = 1L;
    private final List<MilitaryCampaign> militaryCampaigns = new ArrayList<>();
    private static final int MAX_STORED_CAMPAIGNS = 12;

    private long nextProposalId = 1L;
    private final List<ParliamentProposal> parliamentProposals = new ArrayList<>();

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime,
                0L, 5, EconomicPolicy.BALANCED, 0L, 50, -1L, -1L);
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasuryCrowns,
                taxRatePercent, economicPolicy, knowledgePoints, stability, lastTaxDay,
                lastScholarWorkTick, 1L, List.of());
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick,
            final long nextProposalId,
            final List<ParliamentProposal> loadedProposals) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasuryCrowns,
                taxRatePercent, economicPolicy, knowledgePoints, stability, lastTaxDay,
                lastScholarWorkTick, nextProposalId, loadedProposals, 50);
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick,
            final long nextProposalId,
            final List<ParliamentProposal> loadedProposals,
            final int legitimacy) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasuryCrowns,
                taxRatePercent, economicPolicy, knowledgePoints, stability, lastTaxDay,
                lastScholarWorkTick, nextProposalId, loadedProposals, legitimacy, 0, -1L);
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick,
            final long nextProposalId,
            final List<ParliamentProposal> loadedProposals,
            final int legitimacy,
            final int taxCollectionEfficiencyPercent,
            final long lastTaxCollectorWorkTick) {
        this(identity, colonyName, firstSeenGameTime, lastSeenGameTime, treasuryCrowns,
                taxRatePercent, economicPolicy, knowledgePoints, stability, lastTaxDay,
                lastScholarWorkTick, nextProposalId, loadedProposals, legitimacy,
                taxCollectionEfficiencyPercent, lastTaxCollectorWorkTick, 0L, -1L, Map.of());
    }

    EmpireState(
            final ColonyIdentity identity,
            final String colonyName,
            final long firstSeenGameTime,
            final long lastSeenGameTime,
            final long treasuryCrowns,
            final int taxRatePercent,
            final EconomicPolicy economicPolicy,
            final long knowledgePoints,
            final int stability,
            final long lastTaxDay,
            final long lastScholarWorkTick,
            final long nextProposalId,
            final List<ParliamentProposal> loadedProposals,
            final int legitimacy,
            final int taxCollectionEfficiencyPercent,
            final long lastTaxCollectorWorkTick,
            final long diplomaticInfluence,
            final long lastDiplomatWorkTick,
            final Map<ColonyIdentity, Integer> loadedDiplomaticRelations) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.colonyName = normalizeName(colonyName);
        this.firstSeenGameTime = Math.max(0L, firstSeenGameTime);
        this.lastSeenGameTime = Math.max(this.firstSeenGameTime, lastSeenGameTime);
        this.treasuryCrowns = clamp(treasuryCrowns, 0L, MAX_TREASURY);
        this.taxRatePercent = clamp(taxRatePercent, MIN_TAX_RATE, MAX_TAX_RATE);
        this.economicPolicy = Objects.requireNonNullElse(economicPolicy, EconomicPolicy.BALANCED);
        this.knowledgePoints = Math.max(0L, knowledgePoints);
        this.stability = clamp(stability, 0, 100);
        this.legitimacy = clamp(legitimacy, 0, 100);
        this.lastTaxDay = lastTaxDay;
        this.lastScholarWorkTick = lastScholarWorkTick;
        this.taxCollectionEfficiencyPercent = clamp(
                taxCollectionEfficiencyPercent, 0, MAX_TAX_COLLECTION_EFFICIENCY_PERCENT);
        this.lastTaxCollectorWorkTick = Math.max(-1L, lastTaxCollectorWorkTick);
        this.diplomaticInfluence = clamp(diplomaticInfluence, 0L, MAX_DIPLOMATIC_INFLUENCE);
        this.lastDiplomatWorkTick = Math.max(-1L, lastDiplomatWorkTick);
        if (loadedDiplomaticRelations != null) {
            for (final Map.Entry<ColonyIdentity, Integer> relation : loadedDiplomaticRelations.entrySet()) {
                if (relation.getKey() != null && !identity.equals(relation.getKey())
                        && diplomaticRelations.size() < MAX_DIPLOMATIC_RELATIONS) {
                    diplomaticRelations.put(relation.getKey(), clamp(
                            relation.getValue() == null ? 0 : relation.getValue(), -100, 100));
                }
            }
        }
        this.nextProposalId = Math.max(1L, nextProposalId);
        for (final String factionId : FACTION_IDS) {
            factionApproval.put(factionId, 50);
        }

        if (loadedProposals != null) {
            for (final ParliamentProposal proposal : loadedProposals) {
                if (proposal != null) {
                    parliamentProposals.add(proposal);
                    if (proposal.id() < Long.MAX_VALUE && proposal.id() >= this.nextProposalId) {
                        this.nextProposalId = proposal.id() + 1L;
                    }
                }
            }
        }
        trimProposals();
    }

    public ColonyIdentity identity() {
        return identity;
    }

    public String colonyName() {
        return colonyName;
    }

    public ProvinceFocus provinceFocus() {
        return provinceFocus;
    }

    public int provinceDevelopmentPoints() {
        return provinceDevelopmentPoints;
    }

    /** Administrative tier derived from accumulated development investment. */
    public String provinceTierId() {
        if (provinceDevelopmentPoints >= 800) return "kingdom";
        if (provinceDevelopmentPoints >= 500) return "principality";
        if (provinceDevelopmentPoints >= 250) return "duchy";
        if (provinceDevelopmentPoints >= 100) return "county";
        return "settlement";
    }

    private int provinceTierTaxBonusPercent() {
        if (provinceDevelopmentPoints >= 800) return 20;
        if (provinceDevelopmentPoints >= 500) return 15;
        if (provinceDevelopmentPoints >= 250) return 10;
        if (provinceDevelopmentPoints >= 100) return 5;
        return 0;
    }

    public boolean setProvinceFocus(final ProvinceFocus newFocus) {
        Objects.requireNonNull(newFocus, "newFocus");
        if (provinceFocus == newFocus) {
            return false;
        }
        provinceFocus = newFocus;
        return true;
    }

    /** Spend 10 knowledge points to develop the province; max progress is 1,000. */
    public boolean developProvince() {
        if (knowledgePoints < 10L || provinceDevelopmentPoints >= 1_000) {
            return false;
        }
        knowledgePoints -= 10L;
        provinceDevelopmentPoints = Math.min(1_000,
                provinceDevelopmentPoints + provinceFocus.developmentPerInvestment());
        return true;
    }

    void restoreProvinceState(final ProvinceFocus loadedFocus, final int loadedDevelopmentPoints) {
        provinceFocus = Objects.requireNonNullElse(loadedFocus, ProvinceFocus.AGRICULTURE);
        provinceDevelopmentPoints = clamp(loadedDevelopmentPoints, 0, 1_000);
    }

    public long firstSeenGameTime() {
        return firstSeenGameTime;
    }

    public long lastSeenGameTime() {
        return lastSeenGameTime;
    }

    public long treasuryCrowns() {
        return treasuryCrowns;
    }

    public int taxRatePercent() {
        return taxRatePercent;
    }

    public EconomicPolicy economicPolicy() {
        return economicPolicy;
    }

    public long knowledgePoints() {
        return knowledgePoints;
    }

    public int stability() {
        return stability;
    }

    /** Public confidence in the government's right to rule, influenced by citizen conditions. */
    public int legitimacy() {
        return legitimacy;
    }

    public long lastTaxDay() {
        return lastTaxDay;
    }

    public long lastScholarWorkTick() {
        return lastScholarWorkTick;
    }

    public long lastTaxCollectorWorkTick() {
        return lastTaxCollectorWorkTick;
    }

    public int taxCollectionEfficiencyPercent() {
        return taxCollectionEfficiencyPercent;
    }

    public long diplomaticInfluence() {
        return diplomaticInfluence;
    }

    public long lastDiplomatWorkTick() {
        return lastDiplomatWorkTick;
    }

    public Map<ColonyIdentity, Integer> diplomaticRelations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(diplomaticRelations));
    }

    public int relationScore(final ColonyIdentity target) {
        return diplomaticRelations.getOrDefault(Objects.requireNonNull(target, "target"), 0);
    }

    public static String relationStatusId(final int score) {
        if (score >= 75) {
            return "allied";
        }
        if (score >= 40) {
            return "friendly";
        }
        if (score >= 15) {
            return "cordial";
        }
        if (score <= -50) {
            return "hostile";
        }
        if (score <= -15) {
            return "unfriendly";
        }
        return "neutral";
    }

    public Map<String, Integer> factionApproval() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(factionApproval));
    }

    public int factionApproval(final String factionId) {
        if (factionId == null) {
            return 50;
        }
        return factionApproval.getOrDefault(factionId.trim().toLowerCase(java.util.Locale.ROOT), 50);
    }

    public int unrest() {
        return unrest;
    }

    public CivicDisorder civicDisorder() {
        return civicDisorder;
    }

    public long lastCivicDisorderChangeDay() {
        return lastCivicDisorderChangeDay;
    }

    public long siegeEngineeringPoints() {
        return siegeEngineeringPoints;
    }

    public long fieldMedicinePoints() {
        return fieldMedicinePoints;
    }

    public long cavalryDrillPoints() {
        return cavalryDrillPoints;
    }

    /**
     * Records one guard level-up as specialist military training. Each discipline
     * has its own persistent cap and is credited only on the logical server.
     *
     * @return true if the score changed.
     */
    public boolean recordMilitaryTraining(final MilitaryDiscipline discipline) {
        Objects.requireNonNull(discipline, "discipline");
        final long trainingGain = provinceFocus == ProvinceFocus.MILITARY ? 2L : 1L;
        return switch (discipline) {
            case SIEGE_ENGINEERING -> {
                if (siegeEngineeringPoints >= MAX_MILITARY_TRAINING_POINTS) {
                    yield false;
                }
                siegeEngineeringPoints = Math.min(MAX_MILITARY_TRAINING_POINTS,
                        siegeEngineeringPoints + trainingGain);
                yield true;
            }
            case FIELD_MEDICINE -> {
                if (fieldMedicinePoints >= MAX_MILITARY_TRAINING_POINTS) {
                    yield false;
                }
                fieldMedicinePoints = Math.min(MAX_MILITARY_TRAINING_POINTS,
                        fieldMedicinePoints + trainingGain);
                yield true;
            }
            case CAVALRY_DRILL -> {
                if (cavalryDrillPoints >= MAX_MILITARY_TRAINING_POINTS) {
                    yield false;
                }
                cavalryDrillPoints = Math.min(MAX_MILITARY_TRAINING_POINTS,
                        cavalryDrillPoints + trainingGain);
                yield true;
            }
        };
    }

    void restoreMilitaryTraining(
            final long siegeEngineering,
            final long fieldMedicine,
            final long cavalryDrill) {
        siegeEngineeringPoints = clamp(siegeEngineering, 0L, MAX_MILITARY_TRAINING_POINTS);
        fieldMedicinePoints = clamp(fieldMedicine, 0L, MAX_MILITARY_TRAINING_POINTS);
        cavalryDrillPoints = clamp(cavalryDrill, 0L, MAX_MILITARY_TRAINING_POINTS);
    }

    /**
     * Restores political values during SavedData loading. Unknown faction keys
     * are ignored, and all numeric values are bounded before entering gameplay.
     */
    void restorePoliticalSimulation(
            final Map<String, Integer> loadedFactionApproval,
            final int loadedUnrest,
            final CivicDisorder loadedDisorder,
            final long loadedLastChangeDay) {
        for (final String factionId : FACTION_IDS) {
            factionApproval.put(factionId, 50);
        }
        if (loadedFactionApproval != null) {
            for (final String factionId : FACTION_IDS) {
                final Integer value = loadedFactionApproval.get(factionId);
                if (value != null) {
                    factionApproval.put(factionId, clamp(value, 0, 100));
                }
            }
        }
        unrest = clamp(loadedUnrest, 0, 100);
        civicDisorder = Objects.requireNonNullElse(loadedDisorder, CivicDisorder.CALM);
        lastCivicDisorderChangeDay = Math.max(-1L, loadedLastChangeDay);
    }

    /** A Diplomat files reports at most once every 2,400 ticks for the colony. */
    public boolean recordDiplomatWork(final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        if (diplomaticInfluence >= MAX_DIPLOMATIC_INFLUENCE
                || (lastDiplomatWorkTick >= 0L
                    && (safeTime < lastDiplomatWorkTick
                        || safeTime - lastDiplomatWorkTick < DIPLOMAT_WORK_INTERVAL_TICKS))) {
            return false;
        }
        diplomaticInfluence++;
        lastDiplomatWorkTick = safeTime;
        return true;
    }

    /**
     * Spend 10 diplomatic influence to improve a single directional relation by
     * five points. Relations are capped at 100 and self-relations are rejected.
     */
    public boolean improveDiplomaticRelations(final ColonyIdentity target) {
        Objects.requireNonNull(target, "target");
        if (identity.equals(target) || diplomaticInfluence < 10L) {
            return false;
        }

        final Integer current = diplomaticRelations.get(target);
        if (current == null && diplomaticRelations.size() >= MAX_DIPLOMATIC_RELATIONS) {
            return false;
        }
        final int score = current == null ? 0 : current;
        if (score >= 100) {
            return false;
        }

        diplomaticInfluence -= 10L;
        diplomaticRelations.put(target, Math.min(100, score + 5));
        return true;
    }

    public long nextCampaignId() {
        return nextCampaignId;
    }

    /** Recent campaigns in newest-first order, returned as an immutable snapshot. */
    public List<MilitaryCampaign> recentMilitaryCampaigns() {
        final List<MilitaryCampaign> recent = new ArrayList<>(militaryCampaigns);
        Collections.reverse(recent);
        return Collections.unmodifiableList(recent);
    }

    /** Pending campaigns in insertion order. */
    public List<MilitaryCampaign> pendingMilitaryCampaigns() {
        return militaryCampaigns.stream()
                .filter(MilitaryCampaign::isPending)
                .toList();
    }

    List<MilitaryCampaign> storedMilitaryCampaigns() {
        return Collections.unmodifiableList(new ArrayList<>(militaryCampaigns));
    }

    void restoreMilitaryCampaigns(
            final List<MilitaryCampaign> loadedCampaigns,
            final long loadedNextCampaignId) {
        militaryCampaigns.clear();
        nextCampaignId = Math.max(1L, loadedNextCampaignId);
        if (loadedCampaigns != null) {
            for (final MilitaryCampaign campaign : loadedCampaigns) {
                if (campaign == null || militaryCampaigns.size() >= MAX_STORED_CAMPAIGNS) {
                    continue;
                }
                militaryCampaigns.add(campaign);
                if (campaign.id() < Long.MAX_VALUE && campaign.id() >= nextCampaignId) {
                    nextCampaignId = campaign.id() + 1L;
                }
            }
        }
    }

    public long totalMilitaryTrainingPoints() {
        return siegeEngineeringPoints + fieldMedicinePoints + cavalryDrillPoints;
    }

    /** A bounded strategic readiness score based on training, development and stability. */
    public int militaryReadinessScore() {
        long score = 10L
                + totalMilitaryTrainingPoints() / 10L
                + provinceDevelopmentPoints / 50L
                + stability / 10L;
        if (provinceFocus == ProvinceFocus.MILITARY) {
            score += 10L;
        }
        return (int) Math.max(0L, Math.min(1_000L, score));
    }

    /**
     * Launch an operation against an existing colony. One operation may be
     * active at a time, and all costs are charged immediately on the server.
     */
    public Optional<MilitaryCampaign> launchMilitaryCampaign(
            final String commander,
            final ColonyIdentity target,
            final String targetName,
            final MilitaryCampaign.Type type,
            final long currentDay) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(type, "type");
        if (identity.equals(target)
                || nextCampaignId <= 0L
                || nextCampaignId == Long.MAX_VALUE
                || !pendingMilitaryCampaigns().isEmpty()
                || totalMilitaryTrainingPoints() < type.minimumTrainingPoints()
                || treasuryCrowns < type.crownCost()
                || diplomaticInfluence < type.influenceCost()) {
            return Optional.empty();
        }
        if (type == MilitaryCampaign.Type.WAR_CAMPAIGN && relationScore(target) >= 75) {
            return Optional.empty();
        }

        treasuryCrowns -= type.crownCost();
        diplomaticInfluence -= type.influenceCost();
        final MilitaryCampaign campaign = MilitaryCampaign.start(
                nextCampaignId++, type, target, targetName, commander,
                Math.max(0L, currentDay), militaryReadinessScore());
        militaryCampaigns.add(campaign);
        trimMilitaryCampaigns();
        return Optional.of(campaign);
    }

    /**
     * Resolves one due operation and applies its political/economic consequences
     * to the real source and target colony records. No territory is transferred.
     */
    public Optional<MilitaryCampaign.Outcome> resolveMilitaryCampaign(
            final long campaignId,
            final EmpireState targetState,
            final long currentDay) {
        Objects.requireNonNull(targetState, "targetState");
        if (identity.equals(targetState.identity())) {
            return Optional.empty();
        }
        final MilitaryCampaign campaign = militaryCampaigns.stream()
                .filter(value -> value.id() == campaignId)
                .findFirst()
                .orElse(null);
        if (campaign == null || !campaign.isDue(currentDay)
                || !campaign.targetIdentity().equals(targetState.identity())) {
            return Optional.empty();
        }

        final int relation = relationScore(targetState.identity());
        final MilitaryCampaign.Outcome outcome = campaign.resolveIfDue(
                currentDay, targetState.militaryReadinessScore(), relation);
        if (outcome == MilitaryCampaign.Outcome.PENDING) {
            return Optional.empty();
        }

        switch (campaign.type()) {
            case BORDER_PATROL -> {
                if (outcome == MilitaryCampaign.Outcome.SUCCESS) {
                    adjustStability(2);
                    adjustDiplomaticRelation(targetState.identity(), 2);
                    targetState.adjustDiplomaticRelation(identity, 2);
                } else if (outcome == MilitaryCampaign.Outcome.DEFEAT) {
                    adjustStability(-1);
                }
            }
            case RELIEF_EXPEDITION -> {
                if (outcome == MilitaryCampaign.Outcome.SUCCESS) {
                    targetState.creditTreasury(25L);
                    targetState.adjustStability(4);
                    targetState.adjustLegitimacy(2);
                    adjustDiplomaticRelation(targetState.identity(), 10);
                    targetState.adjustDiplomaticRelation(identity, 10);
                } else if (outcome == MilitaryCampaign.Outcome.STALEMATE) {
                    targetState.adjustStability(1);
                    adjustDiplomaticRelation(targetState.identity(), 4);
                    targetState.adjustDiplomaticRelation(identity, 4);
                } else {
                    adjustStability(-2);
                }
            }
            case WAR_CAMPAIGN -> {
                if (outcome == MilitaryCampaign.Outcome.SUCCESS) {
                    creditTreasury(75L);
                    adjustStability(1);
                    targetState.adjustStability(-4);
                    targetState.adjustLegitimacy(-3);
                    adjustDiplomaticRelation(targetState.identity(), -15);
                    targetState.adjustDiplomaticRelation(identity, -15);
                    recordMilitaryTraining(MilitaryDiscipline.SIEGE_ENGINEERING);
                    recordMilitaryTraining(MilitaryDiscipline.FIELD_MEDICINE);
                    recordMilitaryTraining(MilitaryDiscipline.CAVALRY_DRILL);
                } else if (outcome == MilitaryCampaign.Outcome.STALEMATE) {
                    adjustStability(-1);
                    targetState.adjustStability(-1);
                    adjustDiplomaticRelation(targetState.identity(), -3);
                    targetState.adjustDiplomaticRelation(identity, -3);
                } else {
                    adjustStability(-3);
                    targetState.adjustDiplomaticRelation(identity, -5);
                    adjustDiplomaticRelation(targetState.identity(), -5);
                }
            }
        }
        return Optional.of(outcome);
    }

    public boolean adjustDiplomaticRelation(final ColonyIdentity target, final int delta) {
        Objects.requireNonNull(target, "target");
        if (identity.equals(target) || delta == 0) {
            return false;
        }
        final Integer current = diplomaticRelations.get(target);
        if (current == null && diplomaticRelations.size() >= MAX_DIPLOMATIC_RELATIONS) {
            return false;
        }
        final int before = current == null ? 0 : current;
        final int after = clamp(before + delta, -100, 100);
        if (before == after) {
            return false;
        }
        diplomaticRelations.put(target, after);
        return true;
    }

    private void adjustStability(final int change) {
        stability = clamp(stability + change, 0, 100);
    }

    private void adjustLegitimacy(final int change) {
        legitimacy = clamp(legitimacy + change, 0, 100);
    }

    private void trimMilitaryCampaigns() {
        while (militaryCampaigns.size() > MAX_STORED_CAMPAIGNS) {
            final int index = java.util.stream.IntStream.range(0, militaryCampaigns.size())
                    .filter(i -> !militaryCampaigns.get(i).isPending())
                    .findFirst().orElse(-1);
            if (index < 0) {
                break;
            }
            militaryCampaigns.remove(index);
        }
    }

    public long nextProposalId() {
        return nextProposalId;
    }

    /**
     * Refreshes observed metadata only. It never resets the first-seen time.
     *
     * @return true if a persisted field changed.
     */
    boolean observe(final String currentColonyName, final long gameTime) {
        boolean changed = false;
        final String normalizedName = normalizeName(currentColonyName);
        if (!colonyName.equals(normalizedName)) {
            colonyName = normalizedName;
            changed = true;
        }

        final long safeGameTime = Math.max(firstSeenGameTime, gameTime);
        if (Math.floorDiv(safeGameTime, 24_000L) > Math.floorDiv(lastSeenGameTime, 24_000L)) {
            lastSeenGameTime = safeGameTime;
            changed = true;
        }
        return changed;
    }

    static EmpireState create(
            final ColonyIdentity identity,
            final String colonyName,
            final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        return new EmpireState(identity, colonyName, safeTime, safeTime);
    }

    public boolean setTaxRatePercent(final int newRate) {
        if (newRate < MIN_TAX_RATE || newRate > MAX_TAX_RATE) {
            return false;
        }
        if (taxRatePercent == newRate) {
            return false;
        }
        taxRatePercent = newRate;
        return true;
    }

    public boolean setEconomicPolicy(final EconomicPolicy newPolicy) {
        Objects.requireNonNull(newPolicy, "newPolicy");
        if (economicPolicy == newPolicy) {
            return false;
        }
        economicPolicy = newPolicy;
        return true;
    }

    public boolean creditTreasury(final long amount) {
        if (amount <= 0L || amount > MAX_TREASURY - treasuryCrowns) {
            return false;
        }
        treasuryCrowns += amount;
        return true;
    }

    public boolean debitTreasury(final long amount) {
        if (amount <= 0L || amount > treasuryCrowns) {
            return false;
        }
        treasuryCrowns -= amount;
        return true;
    }

    /** Invest crowns at a deterministic rate of 10 crowns per knowledge point. */
    public boolean investInKnowledge(final long amount) {
        if (amount < 10L || amount % 10L != 0L || amount > treasuryCrowns) {
            return false;
        }
        treasuryCrowns -= amount;
        knowledgePoints = Math.min(Long.MAX_VALUE - (amount / 10L), knowledgePoints) + (amount / 10L);
        return true;
    }

    /**
     * A scholar assigned to the Imperial Archive produces one knowledge point
     * no more often than every in-game minute.
     */
    public boolean recordScholarWork(final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        if (lastScholarWorkTick >= 0L
                && (safeTime < lastScholarWorkTick
                    || safeTime - lastScholarWorkTick < SCHOLAR_WORK_INTERVAL_TICKS)) {
            return false;
        }
        if (knowledgePoints < Long.MAX_VALUE) {
            knowledgePoints++;
        }
        lastScholarWorkTick = safeTime;
        return true;
    }

    /**
     * A working Tax Collector improves collection accuracy. One successful
     * filing cycle adds one percentage point, capped at 25%, and is shared by
     * the colony rather than multiplying for every worker on the same tick.
     */
    public boolean recordTaxCollectorWork(final long gameTime) {
        final long safeTime = Math.max(0L, gameTime);
        if (taxCollectionEfficiencyPercent >= MAX_TAX_COLLECTION_EFFICIENCY_PERCENT
                || (lastTaxCollectorWorkTick >= 0L
                    && (safeTime < lastTaxCollectorWorkTick
                        || safeTime - lastTaxCollectorWorkTick < TAX_COLLECTOR_WORK_INTERVAL_TICKS))) {
            return false;
        }
        taxCollectionEfficiencyPercent++;
        lastTaxCollectorWorkTick = safeTime;
        return true;
    }

    /**
     * Compatibility overload for simulations/tests without a MineColonies
     * happiness reading. The live integration uses the three-argument method.
     */
    public long collectDailyTaxes(final long dayIndex, final long population) {
        return collectDailyTaxes(dayIndex, population, Double.NaN, false);
    }

    /**
     * Processes one day of tax and updates political stability/legitimacy from
     * MineColonies' overall happiness. MineColonies reports happiness on a
     * roughly 0–5.5 scale; NaN means no reading is available.
     *
     * @return crowns collected this turn.
     */
    public long collectDailyTaxes(
            final long dayIndex,
            final long population,
            final double overallHappiness) {
        return collectDailyTaxes(dayIndex, population, overallHappiness, false);
    }

    /**
     * Processes a daily tax turn; a governed province gains modest stability,
     * legitimacy and unrest-reduction benefits from active regional administration.
     */
    public long collectDailyTaxes(
            final long dayIndex,
            final long population,
            final double overallHappiness,
            final boolean hasGovernor) {
        expireParliamentProposals(dayIndex);
        if (dayIndex <= lastTaxDay) {
            return 0L;
        }

        lastTaxDay = dayIndex;
        int stabilityChange = economicPolicy.dailyStabilityChange()
                + provinceFocus.dailyStabilityBonus();
        int legitimacyChange = provinceFocus.dailyStabilityBonus();
        if (hasGovernor) {
            stabilityChange++;
            legitimacyChange++;
        }
        if (Double.isFinite(overallHappiness)) {
            final int citizenApprovalChange = citizenApprovalChange(population, overallHappiness);
            final int taxBurdenChange = population <= 0L ? 0
                    : taxRatePercent >= 20 ? -1 : taxRatePercent <= 5 ? 1 : 0;
            stabilityChange += citizenApprovalChange + taxBurdenChange;
            legitimacyChange += citizenApprovalChange + taxBurdenChange;
        }
        legitimacy = clamp(legitimacy + legitimacyChange, 0, 100);
        stability = clamp(stability + stabilityChange, 0, 100);
        updateFactionApproval(population, overallHappiness);
        updateCivicDisorder(dayIndex, population, overallHappiness);
        if (hasGovernor && unrest > 0) {
            unrest--;
        }

        final long safePopulation = Math.max(0L, Math.min(population, 1_000_000L));
        final long taxableBase = (safePopulation * taxRatePercent) / 5L;
        final long policyAdjusted = (taxableBase * economicPolicy.taxMultiplierPercent()) / 100L;
        final long provincialTaxBonus = 100L + provinceTierTaxBonusPercent()
                + provinceFocus.taxBonusPercent();
        final long provinceAdjusted = (policyAdjusted * provincialTaxBonus) / 100L;
        final long efficientRevenue = (provinceAdjusted * (100L + taxCollectionEfficiencyPercent)) / 100L;
        final long civicRevenue = switch (civicDisorder) {
            case CALM -> efficientRevenue;
            case STRIKE -> efficientRevenue / 2L;
            case REVOLT -> efficientRevenue / 10L;
        };
        final long deposited = Math.max(0L, Math.min(civicRevenue, MAX_TREASURY - treasuryCrowns));
        treasuryCrowns += deposited;
        final int knowledgeBonus = provinceFocus.dailyKnowledgeBonus();
        if (knowledgeBonus > 0) {
            knowledgePoints = Math.min(Long.MAX_VALUE - knowledgeBonus, knowledgePoints) + knowledgeBonus;
        }
        return deposited;
    }

    /**
     * Creates a tax bill for parliamentary review. The four faction votes are
     * calculated once at proposal time; the Emperor has a separate assent/veto.
     */
    public Optional<ParliamentProposal> createTaxProposal(
            final String proposer,
            final int proposedTaxRate,
            final long currentDay) {
        if (proposedTaxRate < MIN_TAX_RATE || proposedTaxRate > MAX_TAX_RATE
                || proposedTaxRate == taxRatePercent
                || nextProposalId <= 0L
                || nextProposalId == Long.MAX_VALUE) {
            return Optional.empty();
        }

        for (final ParliamentProposal existing : parliamentProposals) {
            if (existing.isOpen() && existing.proposedTaxRate() == proposedTaxRate) {
                return Optional.empty();
            }
        }

        final ParliamentProposal proposal = ParliamentProposal.createTaxProposal(
                nextProposalId++,
                proposer,
                taxRatePercent,
                proposedTaxRate,
                currentDay,
                economicPolicy,
                stability);
        parliamentProposals.add(proposal);
        trimProposals();
        return Optional.of(proposal);
    }

    public Optional<ParliamentProposal> createPolicyProposal(
            final String proposer,
            final EconomicPolicy proposedPolicy,
            final long currentDay) {
        Objects.requireNonNull(proposedPolicy, "proposedPolicy");
        if (proposedPolicy == economicPolicy
                || nextProposalId <= 0L
                || nextProposalId == Long.MAX_VALUE) {
            return Optional.empty();
        }

        for (final ParliamentProposal existing : parliamentProposals) {
            if (existing.isOpen()
                    && existing.type() == ParliamentProposal.Type.ECONOMIC_POLICY
                    && existing.proposedPolicy() == proposedPolicy) {
                return Optional.empty();
            }
        }

        final ParliamentProposal proposal = ParliamentProposal.createPolicyProposal(
                nextProposalId++,
                proposer,
                taxRatePercent,
                economicPolicy,
                proposedPolicy,
                currentDay,
                stability);
        parliamentProposals.add(proposal);
        trimProposals();
        return Optional.of(proposal);
    }

    public Optional<ParliamentProposal> findProposal(final long proposalId) {
        return parliamentProposals.stream()
                .filter(proposal -> proposal.id() == proposalId)
                .findFirst();
    }

    /** Most recent proposals first, returned as a detached snapshot. */
    public List<ParliamentProposal> recentParliamentProposals() {
        final List<ParliamentProposal> recent = new ArrayList<>(parliamentProposals);
        Collections.reverse(recent);
        return Collections.unmodifiableList(recent);
    }

    /** Proposals in insertion order, used by the persistence layer. */
    List<ParliamentProposal> storedParliamentProposals() {
        return Collections.unmodifiableList(new ArrayList<>(parliamentProposals));
    }

    public boolean expireParliamentProposals(final long currentDay) {
        boolean changed = false;
        for (final ParliamentProposal proposal : parliamentProposals) {
            changed |= proposal.expireIfDue(currentDay);
        }
        return changed;
    }

    public ProposalResolution resolveProposal(
            final long proposalId,
            final boolean emperorAssents,
            final long currentDay,
            final String emperorName) {
        final ParliamentProposal proposal = findProposal(proposalId).orElse(null);
        if (proposal == null) {
            return ProposalResolution.NOT_FOUND;
        }
        if (!proposal.isOpen()) {
            return ProposalResolution.ALREADY_RESOLVED;
        }

        final ParliamentProposal.Status status = proposal.resolveByEmperor(
                emperorAssents, currentDay, taxRatePercent, economicPolicy, emperorName);
        if (status == ParliamentProposal.Status.EXPIRED) {
            return ProposalResolution.EXPIRED;
        }
        if (status != ParliamentProposal.Status.PASSED) {
            return ProposalResolution.REJECTED;
        }

        final boolean applied = proposal.type() == ParliamentProposal.Type.TAX_RATE
                ? setTaxRatePercent(proposal.proposedTaxRate())
                : setEconomicPolicy(proposal.proposedPolicy());
        if (!applied) {
            // Defensive guard: a stale or duplicate bill must not alter state.
            return ProposalResolution.REJECTED;
        }
        return ProposalResolution.PASSED;
    }

    private void trimProposals() {
        while (parliamentProposals.size() > MAX_STORED_PROPOSALS) {
            parliamentProposals.remove(0);
        }
    }

    private static String normalizeName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unnamed colony";
        }
        return name.trim();
    }

    private void updateFactionApproval(final long population, final double overallHappiness) {
        final int merchantTaxChange = taxRatePercent <= 5 ? 1
                : taxRatePercent >= 20 ? -3
                : taxRatePercent >= 12 ? -2 : 0;
        int merchants = merchantTaxChange;
        int commons = taxRatePercent >= 20 ? -3
                : taxRatePercent >= 12 ? -2 : taxRatePercent <= 5 ? 1 : 0;
        int nobility = taxRatePercent >= 20 ? -2 : taxRatePercent >= 15 ? -1 : 0;
        int scholars = 0;

        switch (provinceFocus) {
            case TRADE -> merchants += 1;
            case SCHOLARSHIP -> scholars += 2;
            case MILITARY -> nobility += 1;
            case CIVIC -> commons += 1;
            case AGRICULTURE -> commons += 1;
        }

        switch (economicPolicy) {
            case BALANCED -> scholars += 1;
            case MERCANTILE -> {
                merchants += 2;
                nobility += 1;
            }
            case WELFARE -> {
                merchants -= 1;
                commons += 2;
                nobility -= 1;
                scholars += 1;
            }
            case AUSTERITY -> {
                merchants += 1;
                commons -= 2;
                nobility += 2;
                scholars -= 2;
            }
        }

        if (Double.isFinite(overallHappiness)) {
            final int happinessChange = citizenApprovalChange(population, overallHappiness);
            commons += happinessChange;
            scholars += happinessChange;
            if (overallHappiness >= 4.5 && population > 0L) {
                merchants += 1;
            } else if (overallHappiness < 2.5 || population <= 0L) {
                merchants -= 1;
                nobility -= 1;
            }
        }

        adjustFactionApproval("merchants", merchants);
        adjustFactionApproval("commons", commons);
        adjustFactionApproval("nobility", nobility);
        adjustFactionApproval("scholars", scholars);
    }

    private void adjustFactionApproval(final String factionId, final int change) {
        factionApproval.put(factionId, clamp(factionApproval(factionId) + change, 0, 100));
    }

    private void updateCivicDisorder(
            final long dayIndex,
            final long population,
            final double overallHappiness) {
        int approvalTotal = 0;
        for (final String factionId : FACTION_IDS) {
            approvalTotal += factionApproval(factionId);
        }
        final int averageApproval = approvalTotal / FACTION_IDS.length;

        int unrestChange;
        if (averageApproval < 35) {
            unrestChange = 3;
        } else if (averageApproval < 50) {
            unrestChange = 2;
        } else if (averageApproval < 60) {
            unrestChange = 1;
        } else if (averageApproval >= 75) {
            unrestChange = -3;
        } else if (averageApproval >= 65) {
            unrestChange = -2;
        } else {
            unrestChange = -1;
        }

        if (taxRatePercent >= 20) {
            unrestChange += 2;
        } else if (taxRatePercent <= 5) {
            unrestChange -= 1;
        }
        if (economicPolicy == EconomicPolicy.WELFARE) {
            unrestChange -= 1;
        } else if (economicPolicy == EconomicPolicy.AUSTERITY) {
            unrestChange += 1;
        }
        unrestChange += provinceFocus.dailyUnrestAdjustment();
        if (Double.isFinite(overallHappiness)) {
            if (population <= 0L || overallHappiness < 1.5) {
                unrestChange += 3;
            } else if (overallHappiness < 2.5) {
                unrestChange += 2;
            } else if (overallHappiness >= 4.5) {
                unrestChange -= 2;
            }
        }
        if (stability < 30) {
            unrestChange += 2;
        } else if (stability >= 75) {
            unrestChange -= 1;
        }
        if (legitimacy < 30) {
            unrestChange += 2;
        }
        unrest = clamp(unrest + unrestChange, 0, 100);

        final CivicDisorder previous = civicDisorder;
        if (unrest >= 85 && legitimacy <= 25) {
            civicDisorder = CivicDisorder.REVOLT;
        } else if (previous == CivicDisorder.REVOLT) {
            if (unrest <= 55 && legitimacy >= 35) {
                civicDisorder = unrest <= 40 ? CivicDisorder.CALM : CivicDisorder.STRIKE;
            }
        } else if (unrest >= 65) {
            civicDisorder = CivicDisorder.STRIKE;
        } else if (unrest <= 40) {
            civicDisorder = CivicDisorder.CALM;
        }

        if (civicDisorder != previous) {
            lastCivicDisorderChangeDay = Math.max(0L, dayIndex);
        }
    }

    private static int citizenApprovalChange(final long population, final double overallHappiness) {
        if (population <= 0L) {
            return -2;
        }
        final double happiness = Math.max(0.0, Math.min(5.5, overallHappiness));
        if (happiness >= 4.5) {
            return 2;
        }
        if (happiness >= 3.5) {
            return 1;
        }
        if (happiness >= 2.5) {
            return 0;
        }
        if (happiness >= 1.5) {
            return -1;
        }
        return -2;
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long clamp(final long value, final long min, final long max) {
        return Math.max(min, Math.min(max, value));
    }
}
