package com.imperium.realms;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ImperialJobRegistry {
    public static final DeferredRegister<JobEntry> JOBS = DeferredRegister.create(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "jobs"), ImperiumRealms.MOD_ID);
    public static final ResourceLocation DIPLOMAT_ID =
            ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "diplomat");

    public static final DeferredHolder<JobEntry, JobEntry> DIPLOMAT = JOBS.register("diplomat",
            () -> new JobEntry.Builder()
                    .setJobProducer(JobDiplomat::new)
                    .setJobViewProducer(() -> com.minecolonies.core.colony.jobs.views.DefaultJobView::new)
                    .setRegistryName(DIPLOMAT_ID)
                    .createJobEntry());

    static {
        if (!ModJobs.jobs.contains(DIPLOMAT_ID)) ModJobs.jobs.add(DIPLOMAT_ID);
    }

    private ImperialJobRegistry() { }
    public static void register(final IEventBus modEventBus) { JOBS.register(modEventBus); }
}
