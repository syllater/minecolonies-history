package com.imperium.realms.professions;

import com.imperium.realms.ImperiumRealms;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import net.minecraft.resources.ResourceLocation;

/**
 * MineColonies-compatible anchor block for the Imperial Tax Collector office.
 */
public final class TaxCollectorHutBlock extends AbstractBlockHut<TaxCollectorHutBlock> {
    @Override
    public String getHutName() {
        return "blockhut_tax_collector";
    }

    @Override
    public BuildingEntry getBuildingEntry() {
        return ImperialProfessionRegistry.TAX_COLLECTOR_BUILDING.get();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "tax_collector_hut");
    }
}
