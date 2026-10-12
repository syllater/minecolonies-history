package com.imperium.realms.registry;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.JobImperialCavalier;
import com.imperium.realms.colony.JobImperialFieldMedic;
import com.imperium.realms.colony.JobImperialSiegeEngineer;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native MineColonies guard types for the imperial military roles. */
public final class ModImperiumGuardTypes {
    private static final DeferredRegister<GuardType> GUARD_TYPES =
            DeferredRegister.create(CommonMinecoloniesAPIImpl.GUARD_TYPES, ImperiumRealms.MOD_ID);

    public static final DeferredHolder<GuardType, GuardType> SIEGE_ENGINEER =
            GUARD_TYPES.register("imperial_siege_engineer", () -> new GuardType.Builder()
                    .setJobTranslationKey("imperium_realms.job.imperial_siege_engineer")
                    .setButtonTranslationKey("imperium_realms.gui.guard.imperial_siege_engineer")
                    .setPrimarySkill(Skill.Adaptability)
                    .setSecondarySkill(Skill.Stamina)
                    .setWorkerSoundName("knight")
                    .setJobEntry(() -> ModImperiumJobs.IMPERIAL_SIEGE_ENGINEER.get())
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_siege_engineer"))
                    .setClazz(JobImperialSiegeEngineer.class)
                    .createGuardType());

    public static final DeferredHolder<GuardType, GuardType> FIELD_MEDIC =
            GUARD_TYPES.register("imperial_field_medic", () -> new GuardType.Builder()
                    .setJobTranslationKey("imperium_realms.job.imperial_field_medic")
                    .setButtonTranslationKey("imperium_realms.gui.guard.imperial_field_medic")
                    .setPrimarySkill(Skill.Mana)
                    .setSecondarySkill(Skill.Focus)
                    .setWorkerSoundName("druid")
                    .setJobEntry(() -> ModImperiumJobs.IMPERIAL_FIELD_MEDIC.get())
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_field_medic"))
                    .setClazz(JobImperialFieldMedic.class)
                    .createGuardType());

    public static final DeferredHolder<GuardType, GuardType> CAVALIER =
            GUARD_TYPES.register("imperial_cavalier", () -> new GuardType.Builder()
                    .setJobTranslationKey("imperium_realms.job.imperial_cavalier")
                    .setButtonTranslationKey("imperium_realms.gui.guard.imperial_cavalier")
                    .setPrimarySkill(Skill.Adaptability)
                    .setSecondarySkill(Skill.Stamina)
                    .setWorkerSoundName("archer")
                    .setJobEntry(() -> ModImperiumJobs.IMPERIAL_CAVALIER.get())
                    .setRegistryName(ResourceLocation.fromNamespaceAndPath(
                            ImperiumRealms.MOD_ID, "imperial_cavalier"))
                    .setClazz(JobImperialCavalier.class)
                    .createGuardType());

    private ModImperiumGuardTypes() {
    }

    public static void register(final IEventBus modEventBus) {
        GUARD_TYPES.register(modEventBus);
    }
}
