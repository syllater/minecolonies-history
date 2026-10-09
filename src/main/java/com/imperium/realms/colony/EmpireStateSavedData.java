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
import java.util.Map;
import java.util.Optional;

/**
 * Global Imperium storage stored on the server overworld.
 *
 * <p>Records include the dimension in their identity, so colonies in separate
 * dimensions cannot collide even though this SavedData instance is global.</p>
 */
public final class EmpireStateSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_empire_state";
    private static final int SCHEMA_VERSION = 1;
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

        // Version 0 means a save written before schema versioning; it had no
        // records in this format. Unknown newer versions are read best-effort,
        // because the field names are independently checked below.
        if (savedSchema > SCHEMA_VERSION) {
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
                final String name = entry.getString("colony_name");
                data.colonies.put(identity, new EmpireState(identity, name, firstSeen, lastSeen));
            } catch (IllegalArgumentException exception) {
                // Skip malformed records rather than aborting the entire world load.
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

    /**
     * Insert default state on first observation, and refresh mutable colony
     * metadata on later scans without resetting the record.
     *
     * @return true only when a new record was inserted.
     */
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
            entries.add(entry);
        }
        root.put(TAG_COLONIES, entries);
        return root;
    }
}
