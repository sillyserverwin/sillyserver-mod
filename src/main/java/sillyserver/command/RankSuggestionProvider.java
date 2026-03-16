package sillyserver.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import sillyserver.Sillyserver;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RankSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        Collection<String> rankNames = Sillyserver.RANKS.keySet();

        rankNames.forEach(suggestionsBuilder::suggest);

        return suggestionsBuilder.buildFuture();
    }
}
