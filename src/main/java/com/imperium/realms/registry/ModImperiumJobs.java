package com.imperium.realms.registry;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.JobPhilosopher;
import com.imperium.realms.colony.JobDiplomat;
import com.imperium.realms.colony.JobTaxCollector;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.colony.jobs.views.DefaultJobView;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** MineColonies job registry integration for the first Imperium profession. */
public final class ModImperiumJobs {
    private static final DeferredRegister<JobEntry> JOBS =
            DeferredRegister.create(CommonMinecoloniesAPIImpl.JOBS, ImperiumRealms.MOD_ID);

    public static final DeferredHolder<JobEntry, JobEntry> PHILOSOPHER =
            JOBS.register("philosopher", () -> new JobEntry.Builder()
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "philosopher"))
                    .setJobProducer(JobPhilosopher::new)
                    .setJobViewProducer(() -> DefaultJobView::new)
                    .createJobEntry());

    public static final DeferredHolder<JobEntry, JobEntry> TAX_COLLECTOR =
            JOBS.register("tax_collector", () -> new JobEntry.Builder()
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "tax_collector"))
                    .setJobProducer(JobTaxCollector::new)
                    .setJobViewProducer(() -> DefaultJobView::new)
                    .createJobEntry());

    public static final DeferredHolder<JobEntry, JobEntry> DIPLOMAT =
            JOBS.register("diplomat", () -> new JobEntry.Builder()
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "diplomat"))
                    .setJobProducer(JobDiplomat::new)
                    .setJobViewProducer(() -> DefaultJobView::new)
                    .createJobEntry());

    private ModImperiumJobs() {
    }

    public static void register(final IEventBus modEventBus) {
        JOBS.register(modEventBus);
    }
}
