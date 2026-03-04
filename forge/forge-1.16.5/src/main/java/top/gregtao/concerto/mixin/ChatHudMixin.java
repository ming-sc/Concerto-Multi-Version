package top.gregtao.concerto.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.api.MusicJsonParsers;
import top.gregtao.concerto.core.music.Music;
import top.gregtao.concerto.core.util.ConcertoRunner;
import top.gregtao.concerto.core.util.JsonUtil;
import top.gregtao.concerto.network.ClientMusicNetworkHandler;
import top.gregtao.concerto.network.MusicDataPacket;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = NewChatGui.class)
public class ChatHudMixin {
    @Unique
    private static final Pattern PATTERN = Pattern.compile("Concerto:Share:([a-zA-Z0-9+/=]+)");

    @Unique
    private static void concerto$handleMessage(ITextComponent text) {
        if (ConcertoClient.isServerAvailable()) return;
        Matcher matcher = PATTERN.matcher(text.getString());
        if (!matcher.find()) return;
        String code = new String(Base64.getDecoder().decode(matcher.group(1)));
        Music music = MusicJsonParsers.from(JsonUtil.from(code), false);
        if (music == null) return;
        music.getMeta();
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        String[] authors = music.getMeta().getSource().split(",\\s");
        String sender = authors[authors.length - 1];
        if (client.player.getDisplayName() == null || client.player.getDisplayName().getString().equalsIgnoreCase(sender)) {
            try {
                ClientMusicNetworkHandler.addToWaitList(client, new MusicDataPacket(music, sender, true), client.player);
            } catch (Exception e) {
                ConcertoClient.LOGGER.warn("Received an unsafe music data packet");
            }
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/util/text/ITextComponent;)V", at = @At("HEAD"))
    public void addMessageInject1(ITextComponent message, CallbackInfo ci) {
        ConcertoRunner.run(() -> concerto$handleMessage(message));
    }

    @Inject(method = "addMessage(Lnet/minecraft/util/text/ITextComponent;IIZ)V", at = @At("HEAD"))
    public void addMessageInject2(ITextComponent message, int pChatLineId, int p_93793_, boolean p_93794_, CallbackInfo ci) {
        ConcertoRunner.run(() -> concerto$handleMessage(message));
    }

}
