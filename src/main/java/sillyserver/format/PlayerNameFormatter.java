package sillyserver.format;

import sillyserver.Rank;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class PlayerNameFormatter {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static net.kyori.adventure.text.Component formatPlayerName(Rank rank, String playerName) {
        return MM.deserialize(rank.format(), Placeholder.unparsed("name", playerName));
    }
}
