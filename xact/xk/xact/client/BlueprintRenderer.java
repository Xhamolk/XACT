package xk.xact.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import xk.xact.XActMod;
import xk.xact.util.Textures;

public class BlueprintRenderer implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.FIRST_PERSON_MAP;  // Todo: check the item
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
		/*
		 * EntityPlayer player - The player holding the map
         * RenderEngine engine - The RenderEngine instance
         * MapData mapData - The map data
		* */
		EntityPlayer player = (EntityPlayer) data[0];
		RenderEngine renderEngine = (RenderEngine) data[1];
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

		renderBackground( renderEngine );

		renderContents(renderEngine, fontRenderer, player, itemStack);
	}

	private void renderContents(RenderEngine renderEngine, FontRenderer fontRenderer, EntityPlayer player, ItemStack itemStack) {
		// todo: draw the stuff.
	}

	private void renderBackground(RenderEngine renderEngine) {
		renderEngine.bindTexture( Textures.BLUEPRINT_BG );
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		byte off = 7;
		tessellator.addVertexWithUV((double)(0 - off), (double)(128 + off), 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)(128 + off), (double)(128 + off), 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)(128 + off), (double)(0 - off), 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV((double)(0 - off), (double)(0 - off), 0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}
}
