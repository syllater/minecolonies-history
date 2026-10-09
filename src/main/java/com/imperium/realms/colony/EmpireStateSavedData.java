package com.imperium.realms.colony;

import com.imperium.realms.economy.EmpirePolicy;
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

        // Previous schema versions did not have economy fields. The constructor
        // supplies their migration defaults: 0 crowns, 10% tax, balanced policy.
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
                final long treasury = entry.contains("treasury", Tag.TAG_LONG)
                        ? Math.max(0L, entry.getLong("treasury")) : 0L;
                final int taxRate = entry.contains("tax_rate", Tag.TAG_INT)
                        ? EmpireState.clampTaxRate(entry.getInt("tax_rate")) : 10;
                final EmpirePolicy policy = EmpirePolicy.fromId(entry.getString("policy"))
                        .orElse(EmpirePolicy.BALANCED);
                final long lastTaxDay = entry.contains("last_tax_day", Tag.TAG_LONG)
                        ? entry.getLong("last_tax_day") : -1L;
                data.colonies.put(identity, new EmpireState(
                        identity, name, firstSeen, lastSeen, treasury, taxRate, policy, lastTaxDay));
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

    public boolean setTaxRate(final ColonyIdentity identity, final int percent) {
        final EmpireState state = colonies.get(identity);
        if (state == null) {
            return false;
        }
        if (state.setTaxRatePercent(percent)) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean setPolicy(final ColonyIdentity identity, final EmpirePolicy policy) {
        final EmpireState state = colonies.get(identity);
        if (state == null) {
            return false;
        }
        if (state.setPolicy(policy)) {
            setDirty();
            return true;
        }
        return false;
    }

    public Optional<EmpireState.TaxCollectionResult> collectTaxes(
            final ColonyIdentity identity,
            final int population,
            final long gameTime) {
        final EmpireState state = colonies.get(identity);
        if (state == null) {
            return Optional.empty();
        }
        final Optional<EmpireState.TaxCollectionResult> result = state.collectTaxes(population, gameTime);
        result.ifPresent(ignored -> setDirty());
        return result;
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
            entry.putLong("treasury", state.treasury());
            entry.putInt("tax_rate", state.taxRatePercent());
            entry.putString("policy", state.policy().id());
            entry.putLong("last_tax_day", state.lastTaxCollectionDay());
            entries.add(entry);
        }
        root.put(TAG_COLONIES, entries);
        return root;
    }
}
