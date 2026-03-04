package top.gregtao.concerto.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public class GuiHelper {

    public static void blit(MatrixStack matrixStack, ResourceLocation pTextureId, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
        Minecraft.getInstance().getTextureManager().bind(pTextureId);
        AbstractGui.blit(matrixStack, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
    }

}
