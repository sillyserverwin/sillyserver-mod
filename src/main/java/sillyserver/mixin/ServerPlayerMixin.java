package sillyserver.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sillyserver.PlayerNameFormatter;
import sillyserver.Rank;
import sillyserver.Sillyserver;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method="getTabListDisplayName", at=@At("HEAD"), cancellable = true)
    private void getTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer)(Object)this;

        Rank rank = Sillyserver.getPlayerRank(player);
        if (rank != null) {
            Component ret = PlayerNameFormatter.formatPlayerName(rank, player.getScoreboardName());
            cir.setReturnValue(ret);
        }
    }
}
