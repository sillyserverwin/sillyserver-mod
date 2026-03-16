package sillyserver.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sillyserver.PlayerNameFormatter;
import sillyserver.Rank;
import sillyserver.Sillyserver;

@Mixin(Player.class)
public class PlayerMixin {

    @WrapOperation(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getName()Lnet/minecraft/network/chat/Component;"))
    public Component getCustomName(Player player, Operation<Component> original) {
        if (player instanceof ServerPlayer) {
            Rank rank = Sillyserver.getPlayerRank(player);
            if (rank != null) {
                return PlayerNameFormatter.formatPlayerName(rank, player.getScoreboardName());
            }
        }
        return original.call(player);
    }
}
