package com.imperium.realms.client;

import com.imperium.realms.ImperiumRealms;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.views.BOWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Small BlockUI front end for the server-authoritative imperial ledger.
 * Buttons only submit fixed command literals handled and permission-checked
 * by ImperiumCommands on the server.
 */
public final class ImperiumLedgerWindow extends BOWindow implements ButtonHandler {
    private static final Map<String, String> COMMANDS = Map.ofEntries(
            Map.entry("status", "imperium status"),
            Map.entry("parliament", "imperium parliament status"),
            Map.entry("province", "imperium province status"),
            Map.entry("campaign", "imperium campaign status"),
            Map.entry("empire", "imperium empire status"),
            Map.entry("empire_audit", "imperium empire audit"),
            Map.entry("empire_events", "imperium empire events"),
            Map.entry("empire_petitions", "imperium empire petitions"),
            Map.entry("governors", "imperium governor status"),
            Map.entry("empire_deposit50", "imperium empire deposit 50"),
            Map.entry("empire_withdraw50", "imperium empire withdraw 50"),
            Map.entry("focus_agriculture", "imperium province focus agriculture"),
            Map.entry("focus_trade", "imperium province focus trade"),
            Map.entry("focus_scholarship", "imperium province focus scholarship"),
            Map.entry("focus_military", "imperium province focus military"),
            Map.entry("focus_civic", "imperium province focus civic"),
            Map.entry("develop_province", "imperium province develop"),
            Map.entry("tax0", "imperium tax 0"),
            Map.entry("tax5", "imperium tax 5"),
            Map.entry("tax10", "imperium tax 10"),
            Map.entry("tax15", "imperium tax 15"),
            Map.entry("tax20", "imperium tax 20"),
            Map.entry("tax25", "imperium tax 25"),
            Map.entry("policy_balanced", "imperium policy balanced"),
            Map.entry("policy_mercantile", "imperium policy mercantile"),
            Map.entry("policy_welfare", "imperium policy welfare"),
            Map.entry("policy_austerity", "imperium policy austerity"),
            Map.entry("invest10", "imperium invest 10"),
            Map.entry("invest50", "imperium invest 50"),
            Map.entry("invest100", "imperium invest 100"));

    public ImperiumLedgerWindow() {
        super(ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "gui/imperial_ledger.xml"));
    }

    @Override
    public void onButtonClicked(@NotNull final Button button) {
        if ("close".equals(button.getID())) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        final String command = COMMANDS.get(button.getID());
        if (command == null) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.connection == null) {
            return;
        }

        // The server performs colony resolution, permission checks and mutations.
        minecraft.player.connection.sendCommand(command);
        minecraft.setScreen(null);
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (Minecraft.getInstance().player == null) {
            Minecraft.getInstance().setScreen(null);
        }
    }
}
