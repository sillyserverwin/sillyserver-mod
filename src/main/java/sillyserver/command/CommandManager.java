package sillyserver.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import sillyserver.format.PlayerNameFormatter;
import sillyserver.Rank;
import sillyserver.Sillyserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;


public class CommandManager {
    public static final Function<CommandSourceStack, Boolean> ADMIN_PERMS = (CommandSourceStack src) -> src.permissions().hasPermission(Permissions.COMMANDS_MODERATOR) || (src.isPlayer() && src.getPlayer() != null && src.getPlayer().getStringUUID().equals("d512bc739d3f43f989921b9506adc867"));

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("rank")
                .then(Commands.literal("get")
                        .then(Commands.literal("current").executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command"));
                                return 0;
                            }

                            Player player = context.getSource().getPlayer();

                            if (player == null) return 0;

                            Rank rank = Sillyserver.getPlayerRank(player);
                            if (rank == null) return 0;

                            context.getSource().sendSuccess(() -> Component.literal("Your rank is: " + rank.id()), false);

                            return 1;
                        }))
                        .then(Commands.literal("unlocked").executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command"));
                                return 0;
                            }

                            Player player = context.getSource().getPlayer();

                            if (player == null) return 0;

                            List<Rank> ranks = Sillyserver.getUnlockedRanks(player);
                            if (ranks == null || ranks.isEmpty()) {
                                context.getSource().sendSuccess(() -> Component.literal("You have no ranks."), false);
                                return 1;
                            }

                            List<String> ids = ranks.stream().map(Rank::id).toList();

                            context.getSource().sendSuccess(() -> Component.literal("Unlocked ranks: " + String.join(", ", ids)), false);

                            return 1;
                        }))
                )
                .then(Commands.literal("buy").executes(context -> {
                    if (!context.getSource().isPlayer()) {
                        context.getSource().sendFailure(Component.literal("Only players can use this command"));
                        return 0;
                    }

                    Player player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    context.getSource().sendSuccess(() -> Component.literal("Click to purchase a rank.").withStyle(style -> {
                        try {
                            return style.withClickEvent(new ClickEvent.OpenUrl(new URI("https://sillyserver.win/ranks?uuid=" + player.getStringUUID())))
                                    .withColor(ChatFormatting.GREEN)
                                    .withUnderlined(true);
                        } catch (URISyntaxException e) {
                            // should never happen
                            System.err.println(e.getLocalizedMessage());
                        }
                        return style;
                    }), false);

                    return 1;
                }))
                .then(Commands.literal("preview")
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests(new RankSuggestionProvider()).executes(context -> {
                                    if (!context.getSource().isPlayer()) {
                                        context.getSource().sendFailure(Component.literal("Only players can use this command"));
                                        return 0;
                                    }

                                    Player  player = context.getSource().getPlayer();
                                    if (player == null) return 0;

                                    String rankId = StringArgumentType.getString(context, "rank");
                                    Rank rank = Sillyserver.RANKS.get(rankId);
                                    if (rank == null) {
                                        context.getSource().sendFailure(Component.literal("Rank not found"));
                                        return 0;
                                    }

                                    net.kyori.adventure.text.Component preview = net.kyori.adventure.text.Component.text()
                                        .append(net.kyori.adventure.text.Component.text("<"))
                                        .append(PlayerNameFormatter.formatPlayerName(rank, player.getScoreboardName()))
                                        .append(net.kyori.adventure.text.Component.text("> This is a rank preview."))
                                        .build();

                                    context.getSource().sendMessage(preview);

                                    return 1;
                                })))
        );
    }
}
