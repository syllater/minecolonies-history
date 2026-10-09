package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent emperor office registry, stored in Imperium's overworld SavedData.
 */
public final class ImperialOfficeSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_imperial_offices";
    private static final int SCHEMA_VERSION = 1;

    private static final Factory<ImperialOfficeSavedData> FACTORY = new Factory<>(
            ImperialOfficeSavedData::new,
            ImperialOfficeSavedData::load,
            DataFixTypes.LEVEL);

    private final Map<ColonyIdentity, EmperorOffice> offices = new LinkedHashMap<>();

    public static ImperialOfficeSavedData get(final ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private ImperialOfficeSavedData() {
    }

    public EmperorOffice office(final ColonyIdentity identity) {
        return offices.getOrDefault(identity, new EmperorOffice());
    }

    public boolean claim(
            final ColonyIdentity identity,
            final UUID claimant,
            final String claimantName,
            final long gameDay) {
        final EmperorOffice office = offices.getOrDefault(identity, new EmperorOffice());
        if (!office.claim(claimant, claimantName, gameDay)) {
            return false;
        }
        offices.put(identity, office);
        setDirty();
        return true;
    }

    public boolean appoint(
            final ColonyIdentity identity,
            final UUID actor,
            final UUID successor,
            final String successorName,
            final long gameDay) {
        final EmperorOffice office = offices.get(identity);
        if (office == null || !office.appoint(actor, successor, successorName, gameDay)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean abdicate(final ColonyIdentity identity, final UUID actor) {
        final EmperorOffice office = offices.get(identity);
        if (office == null || !office.abdicate(actor)) {
            return false;
        }
        setDirty();
        return true;
    }

    private static ImperialOfficeSavedData load(
            final CompoundTag root,
            final HolderLookup.Provider registries) {
        final ImperialOfficeSavedData data = new ImperialOfficeSavedData();
        final ListTag entries = root.getList("offices", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            final CompoundTag entry = entries.getCompound(index);
            try {
                final ColonyIdentity identity = new ColonyIdentity(
                        entry.getString("dimension"),
                        entry.getInt("colony_id"));
                final String emperor = entry.getString("emperor_id");
                final UUID emperorId = emperor.isBlank() ? null : UUID.fromString(emperor);
                data.offices.put(identity, new EmperorOffice(
                        emperorId,
                        entry.getString("emperor_name"),
                        entry.getLong("appointment_day"),
                        entry.getLong("reign_count"),
                        entry.getInt("abdications")));
            } catch (IllegalArgumentException ignored) {
                // Skip a malformed office record without preventing world startup.
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
        final ListTag entries = new ListTag();
        for (final Map.Entry<ColonyIdentity, EmperorOffice> value : offices.entrySet()) {
            final CompoundTag entry = new CompoundTag();
            entry.putString("dimension", value.getKey().dimensionId());
            entry.putInt("colony_id", value.getKey().colonyId());
            final EmperorOffice office = value.getValue();
            entry.putString("emperor_id", office.emperorId().map(UUID::toString).orElse(""));
            entry.putString("emperor_name", office.emperorName().orElse(""));
            entry.putLong("appointment_day", office.appointmentGameDay());
            entry.putLong("reign_count", office.reignCount());
            entry.putInt("abdications", office.abdications());
            entries.add(entry);
        }
        root.put("offices", entries);
        return root;
    }
}
