package xk.xact.client;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import xk.xact.util.Textures;

import static xk.xact.client.GuiUtils.itemRender;

public class BlueprintRenderer implements IItemRenderer {

	private RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.FIRST_PERSON_MAP;
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

		renderContents( renderEngine, fontRenderer, player, itemStack );
	}

	private void renderContents(RenderEngine renderEngine, FontRenderer fontRenderer, EntityPlayer player, ItemStack itemStack) {
		GL11.glPushMatrix();
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glDisable( GL11.GL_ALPHA_TEST );
		float rescale = 0.8f;
		GL11.glScalef( rescale, rescale, rescale );

		// This is just a test.
		for( int i = 0; i < 9; i++ ) {
			ItemStack item = player.inventory.getStackInSlot( i );
			if( item != null ) {
				renderIn2D( item, renderEngine, fontRenderer, 0, i * 16 );
			}
		}

		GL11.glScalef( 1.0f / rescale, 1.0f / rescale, 1.0f / rescale ); // not needed?
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glPopMatrix();
	}

	private void renderBackground(RenderEngine renderEngine) {
		renderEngine.bindTexture( Textures.BLUEPRINT_BG );
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		byte off = 7;
		tessellator.addVertexWithUV( (double) (0 - off), (double) (128 + off), 0.0D, 0.0D, 1.0D );
		tessellator.addVertexWithUV( (double) (128 + off), (double) (128 + off), 0.0D, 1.0D, 1.0D );
		tessellator.addVertexWithUV( (double) (128 + off), (double) (0 - off), 0.0D, 1.0D, 0.0D );
		tessellator.addVertexWithUV( (double) (0 - off), (double) (0 - off), 0.0D, 0.0D, 0.0D );
		tessellator.draw();
	}

	private void renderIn2D(ItemStack itemStack, RenderEngine renderEngine, FontRenderer fontRenderer, int xPos, int yPos) {
		float prevZ = GuiUtils.itemRender.zLevel;
		if( isBlock( itemStack ) ) {
			renderBlock( itemStack, renderEngine, fontRenderer, xPos, yPos );
		} else {
			renderItem( itemStack, renderEngine, fontRenderer, xPos, yPos );
		}
		itemRender.renderItemOverlayIntoGUI( fontRenderer, renderEngine, itemStack, xPos, yPos );
		itemRender.zLevel = prevZ;
	}

	private void renderItem(ItemStack item, RenderEngine renderEngine, FontRenderer fontRenderer, int xPos, int yPos) {
		itemRender.zLevel = (float) -0.009999999776482582D;
		itemRender.renderItemAndEffectIntoGUI( fontRenderer, renderEngine, item, xPos, yPos );
	}

	private boolean isBlock(ItemStack item) {
		return item != null && item.getItemSpriteNumber() == 0 && RenderBlocks.renderItemIn3d( Block.blocksList[item.itemID].getRenderType() );
	}

	private void renderBlock(ItemStack item, RenderEngine renderEngine, FontRenderer fontRenderer, int x, int y) {
		GL11.glPushMatrix();
		renderEngine.bindTexture( "/terrain.png" );
		GL11.glTranslatef( (float) (x - 2), (float) (y + 3), -11.0f );
		GL11.glScalef( 10.0F, 10.0F, 10.0F );
		GL11.glTranslatef( 1.0F, 0.5F, 1.0F );
		GL11.glScalef( 1.0F, 1.0F, 0.0F );
		GL11.glRotatef( 210.0F, 1.0F, 0.0F, 0.0F );
		GL11.glRotatef( 45.0F, 0.0F, 1.0F, 0.0F );
		GL11.glRotatef( -90.0F, 0.0F, 1.0F, 0.0F );
		RenderHelper.enableStandardItemLighting();

		int color = Item.itemsList[item.itemID].getColorFromItemStack( item, 0 );
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		GL11.glColor4f( red, green, blue, 1.0F );

		Block block = Block.blocksList[item.itemID];
		renderBlocks.useInventoryTint = true;
		renderBlocks.renderBlockAsItem( block, item.getItemDamage(), 1.0F );

		RenderHelper.disableStandardItemLighting();
		GL11.glEnable( GL11.GL_CULL_FACE );
		GL11.glPopMatrix();
	}

}
