package com.imperium.realms.colony;

import com.imperium.realms.ImperiumRealms;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

/**
 * Periodically discovers existing/new colonies through MineColonies' public API.
 * The idempotent persistence service prevents repeated scans from resetting data.
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MineColoniesLifecycleEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long SCAN_INTERVAL_TICKS = 200L;

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

        final int created = MineColoniesIntegration.synchronizeLoadedColonies(level);
        if (created > 0) {
            LOGGER.info("Initialized {} Imperium empire record(s) in dimension {}",
                    created, level.dimension().location());
        }
    }
}
