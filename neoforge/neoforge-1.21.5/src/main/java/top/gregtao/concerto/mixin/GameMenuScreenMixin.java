package top.gregtao.concerto.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.util.ConcertoRunner;

@Mixin(PauseScreen.class)
public class GameMenuScreenMixin {

    @Inject(at = @At("HEAD"), method = "onDisconnect")
    private void disconnectInject(CallbackInfo ci) {
        ConcertoRunner.run(MusicPlayer.INSTANCE::pause);
    }
}
