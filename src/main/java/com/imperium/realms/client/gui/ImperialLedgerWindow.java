package com.imperium.realms.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.views.BOWindow;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;

/**
 * BlockUI ledger front-end. All commands are executed by the server and retain
 * the normal MineColonies permission checks in ImperiumCommands.
 */
public final class ImperialLedgerWindow extends BOWindow implements ButtonHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation WINDOW_ID =
            ResourceLocation.fromNamespaceAndPath("imperium_realms", "gui/imperial_ledger.xml");

    private static final Map<String, String> COMMANDS = Map.ofEntries(
            Map.entry("status", "imperium status"),
            Map.entry("politics", "imperium politics"),
            Map.entry("tax_10", "imperium taxes rate 10"),
            Map.entry("tax_20", "imperium taxes rate 20"),
            Map.entry("tax_30", "imperium taxes rate 30"),
            Map.entry("policy_balanced", "imperium policy balanced"),
            Map.entry("policy_works", "imperium policy public_works"),
            Map.entry("policy_scholarship", "imperium policy scholarship"),
            Map.entry("parliament_status", "imperium parliament status"),
            Map.entry("propose_works", "imperium parliament propose public_works_act"),
            Map.entry("propose_scholarship", "imperium parliament propose scholarship_charter"),
            Map.entry("propose_tax_relief", "imperium parliament propose tax_relief_charter"),
            Map.entry("vote_yes", "imperium parliament vote yes"),
            Map.entry("vote_no", "imperium parliament vote no"),
            Map.entry("resolve", "imperium parliament resolve"),
            Map.entry("emperor_status", "imperium emperor status"),
            Map.entry("emperor_claim", "imperium emperor claim"),
            Map.entry("emperor_abdicate", "imperium emperor abdicate"),
            Map.entry("diplomacy_status", "imperium diplomacy status"),
            Map.entry("diplomacy_accept", "imperium diplomacy accept"),
            Map.entry("diplomacy_decline", "imperium diplomacy decline")
    );

    public ImperialLedgerWindow() {
        super(WINDOW_ID);
    }

    @Override
    public void onButtonClicked(@NotNull final Button button) {
        if ("close".equals(button.getID())) {
            close();
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if ("collect_taxes".equals(button.getID())) {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.translatable("gui.imperium.ledger.taxes_automatic"), false);
            }
            return;
        }

        final String command = COMMANDS.get(button.getID());
        if (command == null) {
            LOGGER.warn("Unknown Imperial Ledger button id: {}", button.getID());
            return;
        }
        if (minecraft.player == null || minecraft.player.connection == null) {
            return;
        }

        // No data is changed on the client. This is the same command path that
        // would be used in chat; server-side colony and permission checks remain authoritative.
        minecraft.player.connection.sendCommand(command);
        minecraft.player.displayClientMessage(
                Component.translatable("gui.imperium.ledger.command_sent"), true);
    }
}
