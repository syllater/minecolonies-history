package com.imperium.realms.command;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireRealm;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MilitaryCampaign;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Optional;

/**
 * Additional strategy commands for explicitly targeting a colony outside the
 * caller's current dimension. The original same-dimension commands remain
 * available and unchanged.
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class ImperiumCrossDimensionCommands {
    private ImperiumCrossDimensionCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("imperium")
                .then(Commands.literal("diplomacy")
                        .then(Commands.literal("improve-at")
                                .then(Commands.argument("dimension", StringArgumentType.word())
                                        .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                .executes(context -> improveDiplomacyAt(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "dimension"),
                                                        IntegerArgumentType.getInteger(context, "colonyId")))))))
                .then(Commands.literal("campaign")
                        .then(Commands.literal("launch-at")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"border_patrol", "relief_expedition", "war_campaign"},
                                                builder))
                                        .then(Commands.argument("dimension", StringArgumentType.word())
                                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                        .executes(context -> launchCampaignAt(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "type"),
                                                                StringArgumentType.getString(context, "dimension"),
                                                                IntegerArgumentType.getInteger(context, "colonyId"))))))));
    }

    private static int improveDiplomacyAt(
            final CommandSourceStack source,
            final String dimension,
            final int targetColonyId) {
        final EmpireContext context = resolveContext(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final IColony target = findColony(source, dimension, targetColonyId,
                "imperium_realms.message.diplomacy_target_missing");
        if (target == null) {
            return 0;
        }

        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        if (context.identity().equals(targetIdentity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.diplomacy_self_target"));
            return 0;
        }
        if (context.state().diplomaticInfluence() < 10L) {
            source.sendFailure(Component.translatable("imperium_realms.message.diplomacy_influence_insufficient"));
            return 0;
        }
        if (context.state().relationScore(targetIdentity) >= 100) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_relation_max", target.getName()));
            return 0;
        }
        if (!context.state().improveDiplomaticRelations(targetIdentity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.diplomacy_improve_failed"));
            return 0;
        }

        context.data().markChanged();
        final int score = context.state().relationScore(targetIdentity);
        final Component status = Component.translatable(
                "imperium_realms.diplomacy.relation." + EmpireState.relationStatusId(score));
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.diplomacy_improved",
                target.getName(),
                status,
                score,
                context.state().diplomaticInfluence()), true);
        return 1;
    }

    private static int launchCampaignAt(
            final CommandSourceStack source,
            final String requestedType,
            final String dimension,
            final int targetColonyId) {
        final EmpireContext context = resolveContext(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final MilitaryCampaign.Type type = MilitaryCampaign.Type.fromId(requestedType).orElse(null);
        if (type == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_type_unknown"));
            return 0;
        }
        final IColony target = findColony(source, dimension, targetColonyId,
                "imperium_realms.message.campaign_target_not_found");
        if (target == null) {
            return 0;
        }

        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        if (context.identity().equals(targetIdentity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_self_target"));
            return 0;
        }
        if (type == MilitaryCampaign.Type.WAR_CAMPAIGN
                && context.data().areProvincesInSameRealm(context.identity(), targetIdentity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_target_allied"));
            return 0;
        }

        MineColoniesIntegration.getOrCreateState(context.level(), target);
        final EmpireState state = context.state();
        final List<MilitaryCampaign> activeCampaigns = state.pendingMilitaryCampaigns();
        if (activeCampaigns.stream().anyMatch(campaign -> campaign.targetIdentity().equals(targetIdentity))) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_already_active"));
            return 0;
        }
        if (activeCampaigns.size() >= EmpireState.MAX_CONCURRENT_CAMPAIGNS) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.campaign_limit_reached", EmpireState.MAX_CONCURRENT_CAMPAIGNS));
            return 0;
        }
        if (state.treasuryCrowns() < type.crownCost()) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.campaign_crowns_required", type.crownCost()));
            return 0;
        }
        if (state.diplomaticInfluence() < type.influenceCost()) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.campaign_influence_required", type.influenceCost()));
            return 0;
        }
        if (state.totalMilitaryTrainingPoints() < type.minimumTrainingPoints()) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.campaign_training_required", type.minimumTrainingPoints()));
            return 0;
        }
        if (type == MilitaryCampaign.Type.WAR_CAMPAIGN && state.relationScore(targetIdentity) >= 75) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_target_allied"));
            return 0;
        }

        final long dayIndex = Math.max(0L,
                Math.floorDiv(source.getServer().overworld().getGameTime(), 24_000L));
        final MilitaryCampaign campaign = state.launchMilitaryCampaign(
                context.player().getGameProfile().getName(),
                targetIdentity,
                target.getName(),
                type,
                dayIndex,
                context.data().militaryLaunchLogisticsBonus(context.identity())).orElse(null);
        if (campaign == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_launch_failed"));
            return 0;
        }

        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.campaign_launched",
                campaign.id(),
                Component.translatable("imperium_realms.military_campaign.type." + campaign.type().id()),
                campaign.targetName(),
                campaign.resolvesDay()), true);
        return 1;
    }

    private static IColony findColony(
            final CommandSourceStack source,
            final String dimension,
            final int colonyId,
            final String errorKey) {
        final IColony target;
        try {
            target = MineColoniesIntegration.colonyByIdentity(
                    source.getServer(),
                    new ColonyIdentity(dimension, colonyId)).orElse(null);
        } catch (IllegalArgumentException invalidIdentity) {
            source.sendFailure(Component.translatable(errorKey, colonyId));
            return null;
        }
        if (target == null) {
            source.sendFailure(Component.translatable(errorKey, colonyId));
            return null;
        }
        return target;
    }

    private static EmpireContext resolveContext(final CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("imperium_realms.message.player_required"));
            return null;
        }

        final ServerLevel level = player.serverLevel();
        final IColony colony = MineColoniesIntegration.colonyAt(level, player.blockPosition()).orElse(null);
        if (colony == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.enter_colony"));
            return null;
        }

        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final ColonyIdentity identity = ColonyIdentity.from(colony);
        data.observeColony(identity, colony.getName(), level.getGameTime());
        final EmpireState state = data.get(identity).orElse(null);
        if (state == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.ledger_missing"));
            return null;
        }
        return new EmpireContext(player, level, colony, data, identity, state);
    }

    private static boolean mayManageEconomy(final EmpireContext context) {
        return context.player().hasPermissions(2)
                || context.colony().getPermissions().hasPermission(context.player(), Action.MANAGE_HUTS);
    }

    private static int denyPermission(final CommandSourceStack source) {
        source.sendFailure(Component.translatable("imperium_realms.message.permission_denied"));
        return 0;
    }

    private record EmpireContext(
            ServerPlayer player,
            ServerLevel level,
            IColony colony,
            EmpireStateSavedData data,
            ColonyIdentity identity,
            EmpireState state) {
    }
}
