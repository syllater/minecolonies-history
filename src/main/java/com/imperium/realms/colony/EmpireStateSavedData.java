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
 * Global Imperium registry stored in the server overworld. Records use
 * dimension + MineColonies colony ID, so same-number colonies in different
 * dimensions never collide.
 */
public final class EmpireStateSavedData extends SavedData {
    public static final String DATA_NAME = "imperium_realms_empire_state";
    private static final int SCHEMA_VERSION = 2;
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

        if (savedSchema < SCHEMA_VERSION || savedSchema > SCHEMA_VERSION) {
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
                        entry.contains("last_scholar_work_tick") ? entry.getLong("last_scholar_work_tick") : -1L);
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

    /**
     * Insert defaults for a first observation and refresh only metadata on later
     * scans. This is safe to run repeatedly for old and new MineColonies saves.
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
            entry.putLong("last_tax_day", state.lastTaxDay());
            entry.putLong("last_scholar_work_tick", state.lastScholarWorkTick());
            entries.add(entry);
        }
        root.put(TAG_COLONIES, entries);
        return root;
    }
}
