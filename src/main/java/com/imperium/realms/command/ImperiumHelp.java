package com.imperium.realms.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class ImperiumHelp {
    private static final String[] KEYS = {
            "help_header", "help_basics", "help_economy", "help_politics",
            "help_realm", "help_military", "help_map", "help_permissions"
    };

    private ImperiumHelp() {
    }

    public static int show(final CommandSourceStack source) {
        for (final String key : KEYS) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message." + key), false);
        }
        return 1;
    }
}
