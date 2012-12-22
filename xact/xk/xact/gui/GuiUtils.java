package xk.xact.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiUtils {

	private static int grayTone = 139;

	public static final int COLOR_RED = 180 << 16;
	public static final int COLOR_GREEN = 180 << 8;
	public static final int COLOR_BLUE = 220;
	public static final int COLOR_GRAY = (grayTone << 16) | (grayTone << 8) | grayTone;

	public static void paintSlotOverlay(Slot slot, int size, int color) {
		if( slot == null )
			return;

		int off = ( size - 16 ) / 2;
		int minX = slot.xDisplayPosition - off;
		int minY = slot.yDisplayPosition - off;

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Gui.drawRect(minX, minY, minX + size, minY + size, color);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public static void paintItem(ItemStack itemStack, int x, int y, Minecraft mc, RenderItem itemRenderer) {
		if( itemStack == null )
			return; // I might want to have a "null" image, like background image.

		itemRenderer.zLevel = 100.0F;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, x, y);
		itemRenderer.zLevel = 0.0F;
	}

	public static void paintGreenEffect(Slot slot, RenderItem itemRenderer) {
		paintEffectOverlay(slot.xDisplayPosition, slot.yDisplayPosition, Minecraft.getMinecraft().renderEngine, itemRenderer, 0.25f, 0.55f, 0.3f, 0.75f);
	}

	public static void paintEffectOverlay( int x, int y, RenderEngine renderEngine, RenderItem itemRenderer, float red, float green, float blue, float alpha) {
		GL11.glDepthFunc(GL11.GL_GREATER);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		renderEngine.bindTexture(renderEngine.getTexture("%blur%/misc/glint.png")); // do I want to change this to something else?

		itemRenderer.zLevel -= 50.0F;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_DST_COLOR);
		GL11.glColor4f(red, green, blue, alpha);
		effect(itemRenderer.zLevel, x - 1, y - 1, 18, 18);

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		itemRenderer.zLevel += 50.0F;
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}

	private static void effect(float zLevel, int x, int y, int width, int height) {

		GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

		for( int i = 0; i < 2; i++ ) {
			float var7 = 0.00390625F;
			float var8 = 0.00390625F;
			float var9 = (float)(Minecraft.getSystemTime() % (long)(3000 + i * 1873)) / (3000.0F + (float)(i * 1873)) * 256.0F;
			float var10 = 0.0F;
			Tessellator var11 = Tessellator.instance;
			float var12 = 4.0F;

			if (i == 1)
				var12 = -1.0F;

			var11.startDrawingQuads();
			var11.addVertexWithUV((double) x, (double)(y + height), (double)zLevel, (double)((var9 + (float)height * var12) * var7), (double)((var10 + (float)height) * var8));
			var11.addVertexWithUV((double)(x + width), (double)(y + height), (double)zLevel, (double)((var9 + (float)width + (float)height * var12) * var7), (double)((var10 + (float)height) * var8));
			var11.addVertexWithUV((double)(x + width), (double) y, (double)zLevel, (double)((var9 + (float)width) * var7), (double)((var10 + 0.0F) * var8));
			var11.addVertexWithUV((double) x, (double) y, (double)zLevel, (double)((var9 + 0.0F) * var7), (double)((var10 + 0.0F) * var8));
			var11.draw();
		}
	}

	public static boolean isShiftKeyPressed() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

}
