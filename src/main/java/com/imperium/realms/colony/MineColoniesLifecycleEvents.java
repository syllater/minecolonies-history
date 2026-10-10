package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

/**
 * Discovers MineColonies colonies and advances their empire simulation.
 *
 * <p>All state mutation occurs on the logical server. The shared overworld
 * clock makes daily bookkeeping consistent across dimensions, while the saved
 * last-processed day prevents duplicate collections or political updates.</p>
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class MineColoniesLifecycleEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long SCAN_INTERVAL_TICKS = 200L;
    private static final long TICKS_PER_GAME_DAY = 24_000L;

    private MineColoniesLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (Math.floorMod(level.getGameTime(), SCAN_INTERVAL_TICKS) != 0L) {
            return;
        }

        final var colonyManager = IMinecoloniesAPI.getInstance().getColonyManager();
        final var empireData = EmpireStateSavedData.get(level);
        final long sharedGameTime = level.getServer().overworld().getGameTime();
        final long gameDay = Math.floorDiv(sharedGameTime, TICKS_PER_GAME_DAY);

        for (final IColony colony : colonyManager.getColonies(level)) {
            if (colony == null) {
                continue;
            }

            final ColonyIdentity identity = ColonyIdentity.from(colony);
            empireData.observeColony(identity, colony.getName(), sharedGameTime);

            final int population = colony.getCitizenManager().getCitizens().size();
            empireData.collectTaxes(identity, population, sharedGameTime).ifPresent(report ->
                    LOGGER.info(
                            "Imperial accounts for '{}' on day {}: population={}, revenue={}, upkeep={}, balance={}",
                            colony.getName(),
                            report.gameDay(),
                            report.population(),
                            report.grossRevenue(),
                            report.upkeepPaid(),
                            report.treasuryBalance()));

            empireData.processPoliticalDay(identity, colony.getOverallHappiness(), gameDay);
        }
    }
}
