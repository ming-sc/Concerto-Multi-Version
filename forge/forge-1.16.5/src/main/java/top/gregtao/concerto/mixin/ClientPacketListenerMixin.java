package top.gregtao.concerto.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.play.server.SCommandListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.port.command.forge.ClientCommandHandler;

@Mixin(ClientPlayNetHandler.class)
public class ClientPacketListenerMixin {

    @Shadow private CommandDispatcher<ISuggestionProvider> commands;

    @Inject(method = "handleCommands", at = @At("TAIL"))
    public void handleCommandsInject(SCommandListPacket p_195511_1_, CallbackInfo ci) {
        this.commands = ClientCommandHandler.mergeServerCommands(this.commands);
    }

}
