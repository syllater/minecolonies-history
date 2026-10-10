package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

/**
 * Periodically discovers colonies, and processes the economic turn once per
 * overworld day. Every mutation happens on the logical server.
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class MineColoniesLifecycleEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long SCAN_INTERVAL_TICKS = 200L;
    private static final long TAX_TURN_INTERVAL_TICKS = 24_000L;

    private MineColoniesLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (Math.floorMod(level.getGameTime(), SCAN_INTERVAL_TICKS) == 0L) {
            final int created = MineColoniesIntegration.synchronizeLoadedColonies(level);
            if (created > 0) {
                LOGGER.info("Initialized {} Imperium empire record(s) in dimension {}",
                        created, level.dimension().location());
            }
        }

        // Use one stable clock for every dimension to prevent a multi-world
        // server from charging the same colony more than once per day.
        final long gameTime = level.getGameTime();
        if (level.dimension().equals(Level.OVERWORLD)
                && gameTime > 0L
                && Math.floorMod(gameTime, TAX_TURN_INTERVAL_TICKS) == 0L) {
            collectDailyTaxes(level, Math.floorDiv(gameTime, TAX_TURN_INTERVAL_TICKS));
        }
    }

    private static void collectDailyTaxes(final ServerLevel overworld, final long dayIndex) {
        final var data = EmpireStateSavedData.get(overworld);
        long totalRevenue = 0L;
        long totalImperialRemittance = 0L;
        int coloniesAssessed = 0;

        for (final ServerLevel colonyLevel : overworld.getServer().getAllLevels()) {
            for (final IColony colony : IMinecoloniesAPI.getInstance()
                    .getColonyManager()
                    .getColonies(colonyLevel)) {
                if (colony == null) {
                    continue;
                }

                final EmpireState state = MineColoniesIntegration.getOrCreateState(overworld, colony);
                final long population = colony.getCitizenManager().getCitizens().size();
                final boolean hasGovernor = data.realmForProvince(state.identity())
                        .map(realm -> realm.hasGovernor(state.identity())).orElse(false);
                final long provinceRevenue = state.collectDailyTaxes(
                        dayIndex, population, colony.getOverallHappiness(), hasGovernor);
                totalRevenue += provinceRevenue;
                totalImperialRemittance += data.remitImperialTaxReceipts(state.identity(), provinceRevenue, dayIndex);
                coloniesAssessed++;
            }
        }

        // Campaign resolution uses the same authoritative overworld day clock as taxation.
        final int resolvedCampaigns = data.resolveDueMilitaryCampaigns(dayIndex);

        // The turn index, stability changes and zero-income turns are persisted too.
        if (coloniesAssessed > 0 || resolvedCampaigns > 0) {
            data.markChanged();
        }
        if (resolvedCampaigns > 0) {
            LOGGER.info("Resolved {} strategic military operation(s) for day {}",
                    resolvedCampaigns, dayIndex);
        }
        if (totalRevenue > 0L) {
            LOGGER.info("Imperium collected {} crown(s) across {} loaded colony record(s) for day {}",
                    totalRevenue, coloniesAssessed, dayIndex);
        }
        if (totalImperialRemittance > 0L) {
            LOGGER.info("Imperial realms remitted {} crown(s) from provincial taxes into their central reserves for day {}",
                    totalImperialRemittance, dayIndex);
        }
    }
}
