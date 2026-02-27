package top.gregtao.concerto.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.ClientCommandHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Unique
    private final static Set<String> CONCERTO_CLIENT_COMMAND = new HashSet<>() {{
        add("music");
        add("concerto");
        add("sharemusic");
        add("musicroom");
    }};

    /**
     * 自 1.19 后, 原版在此方法中直接发送命令到服务器, 这使聊天界面的链接无法执行仅在客户端注册的命令.
     * <p>
     * 但 Fabric 保留了原来的逻辑, NeoForge 也在 1.21.x (除了 1.21.2 和 1.21.3) 中重新加入该逻辑,
     * 这里通过 Mixin 添加仅对 concerto 自身命令的修复
     *
     * @see <a href="https://github.com/neoforged/NeoForge/pull/554">NeoForge/pr554</a>
     */
    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    public void sendUnsignedCommandInject(String pCommand, CallbackInfoReturnable<Boolean> cir) {
        String[] split = pCommand.split(" ");
        if (split.length > 0 && CONCERTO_CLIENT_COMMAND.contains(split[0])) {
            if (ClientCommandHandler.runCommand(pCommand)) {
                cir.setReturnValue(true);
            }
        }
    }

}
