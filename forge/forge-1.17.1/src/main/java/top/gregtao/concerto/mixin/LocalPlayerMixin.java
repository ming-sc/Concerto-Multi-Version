package top.gregtao.concerto.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.port.command.forge.ClientCommandHandler;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "chat", at = @At("HEAD"), cancellable = true)
    public void chat(String pMessage, CallbackInfo ci) {
        if (ClientCommandHandler.sendMessage(pMessage)) {
            ci.cancel();
        }
    }

}
