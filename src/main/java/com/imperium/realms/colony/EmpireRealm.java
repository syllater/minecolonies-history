package com.imperium.realms.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A persistent imperial realm that unifies several real MineColonies colonies
 * under one Emperor. Each member colony remains a real, independent settlement;
 * this record supplies the shared political identity and province membership.
 */
public final class EmpireRealm {
    public static final long INVITATION_VALIDITY_DAYS = 7L;
    public static final int MAX_PROVINCES = 64;
    public static final int MAX_AUDIT_ENTRIES = 100;
    public static final long REGIONAL_EVENT_INTERVAL_DAYS = 7L;
    public static final long MAX_INVITATIONS = 128L;
    public static final long MAX_IMPERIAL_TREASURY = 10_000_000_000L;
    public static final int IMPERIAL_TAX_REMITTANCE_PERCENT = 10;

    private final long id;
    private String name;
    private final ColonyIdentity capital;
    private final String emperorUuid;
    private String emperorName;
    private final long foundedDay;
    private long lastRegionalEventDay;
    private long imperialTreasuryCrowns;
    // -1 means no empire-wide tax law has been enacted yet.
    private int imperialTaxRatePercent = -1;
    // Empty means province-level policies remain independent.
    private String imperialEconomicPolicyId = "";
    private final Set<ColonyIdentity> provinces = new LinkedHashSet<>();
    private final Map<ColonyIdentity, Long> invitations = new LinkedHashMap<>();
    private final List<ImperialAuditEntry> auditEntries = new ArrayList<>();
    private final Map<ColonyIdentity, ProvinceGovernor> governors = new LinkedHashMap<>();

    private EmpireRealm(
            final long id,
            final String name,
            final ColonyIdentity capital,
            final String emperorUuid,
            final String emperorName,
            final long foundedDay,
            final long imperialTreasuryCrowns) {
        if (id < 1L || foundedDay < 0L) {
            throw new IllegalArgumentException("Invalid realm ID or founded day");
        }
        this.id = id;
        this.name = normalizeRealmName(name);
        this.capital = Objects.requireNonNull(capital, "capital");
        this.emperorUuid = normalize(emperorUuid, "unknown");
        this.emperorName = normalize(emperorName, "Unknown Emperor");
        this.foundedDay = foundedDay;
        this.lastRegionalEventDay = foundedDay;
        this.imperialTreasuryCrowns = clampTreasury(imperialTreasuryCrowns);
        this.provinces.add(capital);
    }

    static EmpireRealm found(
            final long id,
            final String name,
            final ColonyIdentity capital,
            final String emperorUuid,
            final String emperorName,
            final long foundedDay) {
        return new EmpireRealm(id, name, capital, emperorUuid, emperorName, Math.max(0L, foundedDay), 0L);
    }

    static EmpireRealm restore(
            final long id,
            final String name,
            final ColonyIdentity capital,
            final String emperorUuid,
            final String emperorName,
            final long foundedDay,
            final long savedImperialTreasury,
            final List<ColonyIdentity> savedProvinces,
            final Map<ColonyIdentity, Long> savedInvitations,
            final int savedImperialTaxRatePercent,
            final String savedImperialEconomicPolicyId,
            final List<ImperialAuditEntry> savedAuditEntries,
            final Map<ColonyIdentity, ProvinceGovernor> savedGovernors,
            final long savedLastRegionalEventDay) {
        final EmpireRealm realm = new EmpireRealm(
                id, name, capital, emperorUuid, emperorName, Math.max(0L, foundedDay),
                clampTreasury(savedImperialTreasury));
        if (savedImperialTaxRatePercent >= 0 && savedImperialTaxRatePercent <= 25) {
            realm.imperialTaxRatePercent = savedImperialTaxRatePercent;
        }
        realm.imperialEconomicPolicyId = EconomicPolicy.fromId(savedImperialEconomicPolicyId)
                .map(EconomicPolicy::id).orElse("");
        if (savedProvinces != null) {
            for (final ColonyIdentity province : savedProvinces) {
                if (province != null && realm.provinces.size() < MAX_PROVINCES) {
                    realm.provinces.add(province);
                }
            }
        }
        if (savedInvitations != null) {
            for (final Map.Entry<ColonyIdentity, Long> invitation : savedInvitations.entrySet()) {
                if (invitation.getKey() != null && !realm.provinces.contains(invitation.getKey())) {
                    realm.invitations.put(invitation.getKey(), Math.max(0L,
                            invitation.getValue() == null ? 0L : invitation.getValue()));
                }
            }
        }
        realm.restoreAuditEntries(savedAuditEntries);
        realm.restoreGovernors(savedGovernors);
        realm.lastRegionalEventDay = Math.max(realm.foundedDay, savedLastRegionalEventDay);
        return realm;
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public ColonyIdentity capital() {
        return capital;
    }

    public String emperorUuid() {
        return emperorUuid;
    }

    public String emperorName() {
        return emperorName;
    }

    public long foundedDay() {
        return foundedDay;
    }

    public long lastRegionalEventDay() {
        return lastRegionalEventDay;
    }

    public boolean isRegionalEventDue(final long dayIndex) {
        return dayIndex >= foundedDay && dayIndex >= lastRegionalEventDay
                && dayIndex - lastRegionalEventDay >= REGIONAL_EVENT_INTERVAL_DAYS;
    }

    boolean markRegionalEvent(final long dayIndex) {
        if (!isRegionalEventDue(dayIndex)) return false;
        lastRegionalEventDay = dayIndex;
        return true;
    }

    public long imperialTreasuryCrowns() {
        return imperialTreasuryCrowns;
    }

    public int imperialTaxRatePercent() {
        return imperialTaxRatePercent;
    }

    public boolean hasImperialTaxLaw() {
        return imperialTaxRatePercent >= 0;
    }

    /**
     * Calculates the central share of already-collected provincial tax receipts.
     * The levy is a transfer of existing receipts, not an additional citizen tax.
     */
    public long calculateImperialTaxRemittance(final long provinceTaxReceipts) {
        if (!hasImperialTaxLaw() || provinceTaxReceipts <= 0L) {
            return 0L;
        }
        return (provinceTaxReceipts / 100L) * IMPERIAL_TAX_REMITTANCE_PERCENT
                + ((provinceTaxReceipts % 100L) * IMPERIAL_TAX_REMITTANCE_PERCENT) / 100L;
    }

    /** Records an enacted realm-wide tax law; returns false for an invalid or unchanged value. */
    public boolean setImperialTaxRatePercent(final int rate) {
        if (rate < 0 || rate > 25 || imperialTaxRatePercent == rate) {
            return false;
        }
        imperialTaxRatePercent = rate;
        return true;
    }

    public String imperialEconomicPolicyId() {
        return imperialEconomicPolicyId;
    }

    public boolean hasImperialPolicyLaw() {
        return !imperialEconomicPolicyId.isBlank();
    }

    /** Records an enacted realm-wide economic policy. */
    public boolean setImperialEconomicPolicy(final EconomicPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        if (imperialEconomicPolicyId.equals(policy.id())) {
            return false;
        }
        imperialEconomicPolicyId = policy.id();
        return true;
    }

    public boolean depositImperialTreasury(final long amount) {
        if (amount <= 0L || amount > MAX_IMPERIAL_TREASURY - imperialTreasuryCrowns) {
            return false;
        }
        imperialTreasuryCrowns += amount;
        return true;
    }

    public boolean withdrawImperialTreasury(final long amount) {
        if (amount <= 0L || amount > imperialTreasuryCrowns) {
            return false;
        }
        imperialTreasuryCrowns -= amount;
        return true;
    }

    public int provinceCount() {
        return provinces.size();
    }

    public boolean containsProvince(final ColonyIdentity identity) {
        return provinces.contains(Objects.requireNonNull(identity, "identity"));
    }

    public List<ColonyIdentity> provinces() {
        return Collections.unmodifiableList(new ArrayList<>(provinces));
    }

    public Map<ColonyIdentity, Long> invitations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(invitations));
    }

    /** Recent audit entries in newest-first order. */
    public List<ImperialAuditEntry> recentAuditEntries() {
        final List<ImperialAuditEntry> recent = new ArrayList<>(auditEntries);
        Collections.reverse(recent);
        return Collections.unmodifiableList(recent);
    }

    List<ImperialAuditEntry> storedAuditEntries() {
        return Collections.unmodifiableList(new ArrayList<>(auditEntries));
    }

    void recordAudit(final long dayIndex, final String actor, final String actionId,
            final String subject, final long amount) {
        auditEntries.add(new ImperialAuditEntry(Math.max(0L, dayIndex), actor,
                actionId, subject, Math.max(0L, amount)));
        while (auditEntries.size() > MAX_AUDIT_ENTRIES) auditEntries.remove(0);
    }

    void restoreAuditEntries(final List<ImperialAuditEntry> savedEntries) {
        auditEntries.clear();
        if (savedEntries == null) return;
        final int start = Math.max(0, savedEntries.size() - MAX_AUDIT_ENTRIES);
        for (int index = start; index < savedEntries.size(); index++) {
            final ImperialAuditEntry entry = savedEntries.get(index);
            if (entry != null) auditEntries.add(entry);
        }
    }

    public Map<ColonyIdentity, ProvinceGovernor> governors() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(governors));
    }

    public java.util.Optional<ProvinceGovernor> governorFor(final ColonyIdentity province) {
        return java.util.Optional.ofNullable(governors.get(province));
    }

    public boolean hasGovernor(final ColonyIdentity province) {
        return governors.containsKey(province);
    }

    boolean appointGovernor(final ColonyIdentity province, final String playerUuid,
            final String playerName, final long appointedDay) {
        if (province == null || !provinces.contains(province) || capital.equals(province)
                || playerUuid == null || playerUuid.isBlank() || appointedDay < 0L) {
            return false;
        }
        final ProvinceGovernor next = new ProvinceGovernor(playerUuid, playerName, appointedDay);
        if (next.equals(governors.get(province))) return false;
        governors.put(province, next);
        return true;
    }

    boolean dismissGovernor(final ColonyIdentity province) {
        if (province == null || capital.equals(province)) return false;
        return governors.remove(province) != null;
    }

    void restoreGovernors(final Map<ColonyIdentity, ProvinceGovernor> savedGovernors) {
        governors.clear();
        if (savedGovernors == null) return;
        for (final Map.Entry<ColonyIdentity, ProvinceGovernor> entry : savedGovernors.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null
                    && provinces.contains(entry.getKey()) && !capital.equals(entry.getKey())
                    && governors.size() < MAX_PROVINCES - 1) {
                governors.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public boolean isEmperor(final String playerUuid) {
        return playerUuid != null && emperorUuid.equals(playerUuid);
    }

    boolean inviteProvince(final ColonyIdentity identity, final long currentDay) {
        Objects.requireNonNull(identity, "identity");
        if (provinces.contains(identity) || invitations.containsKey(identity)
                || provinces.size() >= MAX_PROVINCES || invitations.size() >= MAX_INVITATIONS
                || currentDay < 0L) {
            return false;
        }
        final long expires = currentDay > Long.MAX_VALUE - INVITATION_VALIDITY_DAYS
                ? Long.MAX_VALUE : currentDay + INVITATION_VALIDITY_DAYS;
        invitations.put(identity, expires);
        return true;
    }

    long invitationExpiry(final ColonyIdentity identity) {
        return invitations.getOrDefault(identity, -1L);
    }

    boolean hasValidInvitation(final ColonyIdentity identity, final long currentDay) {
        final Long expiry = invitations.get(identity);
        return expiry != null && currentDay <= expiry && provinces.size() < MAX_PROVINCES;
    }

    boolean acceptInvitation(final ColonyIdentity identity, final long currentDay) {
        if (!hasValidInvitation(identity, currentDay)) {
            invitations.remove(identity);
            return false;
        }
        invitations.remove(identity);
        return provinces.add(identity);
    }

    boolean removeProvince(final ColonyIdentity identity) {
        Objects.requireNonNull(identity, "identity");
        if (capital.equals(identity)) {
            return false;
        }
        invitations.remove(identity);
        governors.remove(identity);
        return provinces.remove(identity);
    }

    boolean expireInvitations(final long currentDay) {
        return invitations.entrySet().removeIf(entry -> currentDay > entry.getValue());
    }

    private static long clampTreasury(final long treasury) {
        return Math.max(0L, Math.min(MAX_IMPERIAL_TREASURY, treasury));
    }

    private static String normalizeRealmName(final String value) {
        final String normalized = normalize(value, "");
        if (normalized.length() < 3 || normalized.length() > 32
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Realm name must contain 3–32 visible characters");
        }
        return normalized;
    }

    private static String normalize(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
