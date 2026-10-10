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
    public static final long MAX_INVITATIONS = 128L;
    public static final long MAX_IMPERIAL_TREASURY = 10_000_000_000L;

    private final long id;
    private String name;
    private final ColonyIdentity capital;
    private final String emperorUuid;
    private String emperorName;
    private final long foundedDay;
    private long imperialTreasuryCrowns;
    private final Set<ColonyIdentity> provinces = new LinkedHashSet<>();
    private final Map<ColonyIdentity, Long> invitations = new LinkedHashMap<>();

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
            final Map<ColonyIdentity, Long> savedInvitations) {
        final EmpireRealm realm = new EmpireRealm(
                id, name, capital, emperorUuid, emperorName, Math.max(0L, foundedDay),
                clampTreasury(savedImperialTreasury));
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

    public long imperialTreasuryCrowns() {
        return imperialTreasuryCrowns;
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
