package top.gregtao.concerto.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.concerto.screen.InGameHudRenderer;

@Mixin(ForgeIngameGui.class)
public class InGameGuiMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void renderInject(MatrixStack matrixStack, float delta, CallbackInfo ci) {
        InGameHudRenderer.render(matrixStack, 0, 0, delta);
    }

}
