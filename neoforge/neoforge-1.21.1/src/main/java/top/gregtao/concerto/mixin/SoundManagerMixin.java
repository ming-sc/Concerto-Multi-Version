package top.gregtao.concerto.mixin;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.core.player.MusicPlayer;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Inject(at = @At("HEAD"), method = "pause")
    private void pauseAllInject(CallbackInfo ci) {
        MusicPlayer.INSTANCE.pause();
    }

    @Inject(at = @At("HEAD"), method = "resume")
    private void resumeAllInject(CallbackInfo ci) {
        MusicPlayer.INSTANCE.resume();
    }

    @Inject(at = @At("TAIL"), method = "updateSourceVolume")
    private void updateSoundVolumeInject(SoundSource category, float volume, CallbackInfo ci) {
        if (category == SoundSource.MASTER || category == SoundSource.MUSIC) {
            MusicPlayer.INSTANCE.syncVolume();
        }
    }
}
