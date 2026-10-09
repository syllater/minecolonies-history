package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-owned storage for bilateral offers and established diplomatic relations.
 * All diplomacy is independent of MineColonies-private NBT.
 */
public final class DiplomacySavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_diplomacy";
    private static final int SCHEMA_VERSION = 1;

    private static final Factory<DiplomacySavedData> FACTORY = new Factory<>(
            DiplomacySavedData::new,
            DiplomacySavedData::load,
            DataFixTypes.LEVEL);

    private final Map<String, DiplomaticRelation> relations = new LinkedHashMap<>();
    private final Map<ColonyIdentity, DiplomacyOffer> pendingByTarget = new LinkedHashMap<>();

    public static DiplomacySavedData get(final ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private DiplomacySavedData() {
    }

    public boolean offer(
            final ColonyIdentity source,
            final ColonyIdentity target,
            final TreatyType treaty,
            final UUID proposerId,
            final String proposerName,
            final long gameDay) {
        if (source.equals(target) || pendingByTarget.containsKey(target)) {
            return false;
        }
        pendingByTarget.put(target, new DiplomacyOffer(
                source, target, treaty, proposerId, proposerName, gameDay));
        setDirty();
        return true;
    }

    public Optional<DiplomacyOffer> pendingOfferFor(final ColonyIdentity target) {
        return Optional.ofNullable(pendingByTarget.get(target));
    }

    public Optional<DiplomaticRelation.Snapshot> accept(
            final ColonyIdentity target,
            final UUID acceptingPlayer,
            final String acceptingPlayerName,
            final long gameDay) {
        final DiplomacyOffer offer = pendingByTarget.get(target);
        if (offer == null || offer.source().equals(target)) {
            return Optional.empty();
        }

        final DiplomaticRelation relation = relations.computeIfAbsent(
                relationKey(offer.source(), offer.target()),
                ignored -> new DiplomaticRelation(
                        offer.source(), offer.target(), 0, offer.treaty(), gameDay));
        relation.applyTreaty(offer.treaty(), gameDay);
        pendingByTarget.remove(target);
        setDirty();
        return Optional.of(relation.snapshotFor(target));
    }

    public boolean decline(final ColonyIdentity target) {
        if (pendingByTarget.remove(target) == null) {
            return false;
        }
        setDirty();
        return true;
    }

    public List<DiplomaticRelation.Snapshot> relationsFor(final ColonyIdentity identity) {
        final List<DiplomaticRelation.Snapshot> results = new ArrayList<>();
        for (final DiplomaticRelation relation : relations.values()) {
            if (relation.first().equals(identity) || relation.second().equals(identity)) {
                results.add(relation.snapshotFor(identity));
            }
        }
        results.sort((left, right) -> left.otherColony().storageKey().compareTo(right.otherColony().storageKey()));
        return List.copyOf(results);
    }

    private static String relationKey(final ColonyIdentity a, final ColonyIdentity b) {
        final String left = a.storageKey();
        final String right = b.storageKey();
        return left.compareTo(right) < 0 ? left + "|" + right : right + "|" + left;
    }

    private static ColonyIdentity readIdentity(final CompoundTag entry, final String prefix) {
        return new ColonyIdentity(
                entry.getString(prefix + "_dimension"),
                entry.getInt(prefix + "_colony_id"));
    }

    private static void writeIdentity(final CompoundTag entry, final String prefix, final ColonyIdentity identity) {
        entry.putString(prefix + "_dimension", identity.dimensionId());
        entry.putInt(prefix + "_colony_id", identity.colonyId());
    }

    private static DiplomacySavedData load(
            final CompoundTag root,
            final HolderLookup.Provider registries) {
        final DiplomacySavedData data = new DiplomacySavedData();

        final ListTag relationEntries = root.getList("relations", Tag.TAG_COMPOUND);
        for (int index = 0; index < relationEntries.size(); index++) {
            final CompoundTag entry = relationEntries.getCompound(index);
            try {
                final ColonyIdentity first = readIdentity(entry, "first");
                final ColonyIdentity second = readIdentity(entry, "second");
                final TreatyType treaty = TreatyType.fromId(entry.getString("treaty"))
                        .orElse(TreatyType.FRIENDSHIP);
                final DiplomaticRelation relation = new DiplomaticRelation(
                        first, second, entry.getInt("standing"), treaty, entry.getLong("last_changed_day"));
                data.relations.put(relationKey(first, second), relation);
            } catch (IllegalArgumentException ignored) {
                // Skip corrupted records while keeping the rest of the save usable.
            }
        }

        final ListTag offers = root.getList("offers", Tag.TAG_COMPOUND);
        for (int index = 0; index < offers.size(); index++) {
            final CompoundTag entry = offers.getCompound(index);
            try {
                final ColonyIdentity source = readIdentity(entry, "source");
                final ColonyIdentity target = readIdentity(entry, "target");
                final TreatyType treaty = TreatyType.fromId(entry.getString("treaty")).orElse(null);
                final UUID proposer = UUID.fromString(entry.getString("proposer_id"));
                if (treaty == null) {
                    continue;
                }
                data.pendingByTarget.put(target, new DiplomacyOffer(
                        source, target, treaty, proposer, entry.getString("proposer_name"),
                        entry.getLong("proposed_day")));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed pending offers.
            }
        }

        if (root.getInt("schema_version") > SCHEMA_VERSION) {
            data.setDirty();
        }
        return data;
    }

    @Override
    public CompoundTag save(final CompoundTag root, final HolderLookup.Provider registries) {
        root.putInt("schema_version", SCHEMA_VERSION);

        final ListTag relationEntries = new ListTag();
        for (final DiplomaticRelation relation : relations.values()) {
            final CompoundTag entry = new CompoundTag();
            writeIdentity(entry, "first", relation.first());
            writeIdentity(entry, "second", relation.second());
            entry.putInt("standing", relation.standing());
            entry.putString("treaty", relation.treaty().id());
            entry.putLong("last_changed_day", relation.lastChangedGameDay());
            relationEntries.add(entry);
        }
        root.put("relations", relationEntries);

        final ListTag offers = new ListTag();
        for (final DiplomacyOffer offer : pendingByTarget.values()) {
            final CompoundTag entry = new CompoundTag();
            writeIdentity(entry, "source", offer.source());
            writeIdentity(entry, "target", offer.target());
            entry.putString("treaty", offer.treaty().id());
            entry.putString("proposer_id", offer.proposerId().toString());
            entry.putString("proposer_name", offer.proposerName());
            entry.putLong("proposed_day", offer.proposedGameDay());
            offers.add(entry);
        }
        root.put("offers", offers);
        return root;
    }
}
