package xk.xact.client.render;


import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import xk.xact.client.GuiUtils;
import xk.xact.util.Textures;

import static xk.xact.client.GuiUtils.itemRender;

public class BlueprintRenderer implements IItemRenderer {

	private RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.EQUIPPED_FIRST_PERSON;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	public static float bgScale = 1.0f / 64f; // it was 1/64

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		/*
		 * RenderBlocks render - The RenderBlocks instance
         * EntityLiving entity - The entity holding this item
		* */
		Minecraft mc = Minecraft.getMinecraft();
		RenderBlocks renderBlocks = (RenderBlocks) data[0];
		EntityPlayer player = mc.thePlayer;
		RenderEngine renderEngine = mc.renderEngine;
		FontRenderer fontRenderer = mc.fontRenderer;

		float partialTicks = mc.timer.renderPartialTicks;
		float equipProcess = getEquipProcess(mc.entityRenderer.itemRenderer, partialTicks);

		GL11.glPopMatrix(); // clear matrix before starting over.
		GL11.glPushMatrix();
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glScalef( 2.5F, 2.5F, 2.5F );

		float var7 = 0.8F;

		float swingProgress = player.getSwingProgress( partialTicks );
		float var21 = MathHelper.sin( swingProgress * swingProgress * (float)Math.PI );
		float var10 = MathHelper.sin( MathHelper.sqrt_float( swingProgress ) * (float) Math.PI );
		GL11.glRotatef( -var10 * 80.0F, -1.0F, 0.0F, 0.0F );
		GL11.glRotatef( -var10 * 20.0F, 0.0F, 0.0F, -1.0F );
		GL11.glRotatef( -var21 * 20.0F, 0.0F, -1.0F, 0.0F );
		GL11.glRotatef( 45.0F, 0.0F, -1.0F, 0.0F );
		GL11.glTranslatef( -0.7F * var7, -(-0.65F * var7 - (1.0F - equipProcess) * 0.6F), 0.9F * var7 );

		GL11.glPushMatrix();

		// get sky light
		int var18 = mc.theWorld.getLightBrightnessForSkyBlocks( MathHelper.floor_double( player.posX ), MathHelper.floor_double( player.posY ), MathHelper.floor_double( player.posZ ), 0 );
		int var8 = var18 % 65536;
		int var9 = var18 / 65536;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var8 / 1.0F, var9 / 1.0F );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Render Arms
		renderEngine.bindTexture( mc.thePlayer.getTexture() );
		renderArms( mc.thePlayer, partialTicks, swingProgress, equipProcess, var7 );

		// More transformations...
		var21 = player.getSwingProgress( partialTicks );
		var10 = MathHelper.sin( var21 * var21 * 3.141593F );
		float var11 = MathHelper.sin( MathHelper.sqrt_float( var21 ) * 3.141593F );
		GL11.glRotatef( -var10 * 20.0F, 0.0F, 1.0F, 0.0F );
		GL11.glRotatef( -var11 * 20.0F, 0.0F, 0.0F, 1.0F );
		GL11.glRotatef( -var11 * 80.0F, 1.0F, 0.0F, 0.0F );
		float var12 = 0.38F;
		GL11.glScalef( var12, var12, var12 );
		GL11.glRotatef( 90.0F, 0.0F, 1.0F, 0.0F );
		GL11.glRotatef( 180.0F, 0.0F, 0.0F, 1.0F );
		GL11.glTranslatef( -1.0F, -1.0F, 0.0F );

		// Render background.
		float var13 = 1.0f / 64f;
		GL11.glScalef( var13, var13, var13 );
		renderBackground( renderEngine );

		// Render the blueprint's contents
		renderContents( renderEngine, fontRenderer, player, item );

		GL11.glPopMatrix();
	}

	private void renderArms(EntityClientPlayerMP player, float partialTicks, float swingProgress, float equipProcess, float var7) {
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

		float var21 = MathHelper.sin( swingProgress * 3.141593F );
		float var10 = MathHelper.sin( MathHelper.sqrt_float( swingProgress ) * 3.141593F );

		GL11.glTranslatef( -var10 * 0.4F, MathHelper.sin( MathHelper.sqrt_float( swingProgress ) * 3.141593F * 2.0F ) * 0.2F, -var21 * 0.2F );
		float otherFloatPitch = 1.0F - pitch / 45.0F + 0.1F;

		if( otherFloatPitch < 0.0F ) otherFloatPitch = 0.0F;
		if( otherFloatPitch > 1.0F ) otherFloatPitch = 1.0F;

		otherFloatPitch = -MathHelper.cos( otherFloatPitch * 3.141593F ) * 0.5F + 0.5F;
		GL11.glTranslatef( 0.0F, 0.0F * var7 - (1.0F - equipProcess) * 1.2F - otherFloatPitch * 0.5F + 0.04F, -0.9F * var7 );
		GL11.glRotatef( 90.0F, 0.0F, 1.0F, 0.0F );
		GL11.glRotatef( otherFloatPitch * -85.0F, 0.0F, 0.0F, 1.0F );
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );

		for( int i = 0; i < 2; i++ ) {
			int side = i * 2 - 1;
			GL11.glPushMatrix();
			GL11.glTranslatef( -0.0F, -0.6F, 1.1F * side );
			GL11.glRotatef( -45 * side, 1.0F, 0.0F, 0.0F );
			GL11.glRotatef( -90.0F, 0.0F, 0.0F, 1.0F );
			GL11.glRotatef( 59.0F, 0.0F, 0.0F, 1.0F );
			GL11.glRotatef( -65 * side, 0.0F, 1.0F, 0.0F );
			Render var24 = RenderManager.instance.getEntityRenderObject( player );
			RenderPlayer var26 = (RenderPlayer) var24;
			float var13 = 1.0F;
			GL11.glScalef( var13, var13, var13 );
			var26.renderFirstPersonArm( player );
			GL11.glPopMatrix();
		}
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
		GL11.glPushMatrix();
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glNormal3f( 0.0F, 0.0F, -1.0F );

		renderEngine.bindTexture( Textures.BLUEPRINT_BG );
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		byte off = 7;
		tessellator.addVertexWithUV( (double) (0 - off), (double) (128 + off), 0.0D, 0.0D, 1.0D );
		tessellator.addVertexWithUV( (double) (128 + off), (double) (128 + off), 0.0D, 1.0D, 1.0D );
		tessellator.addVertexWithUV( (double) (128 + off), (double) (0 - off), 0.0D, 1.0D, 0.0D );
		tessellator.addVertexWithUV( (double) (0 - off), (double) (0 - off), 0.0D, 0.0D, 0.0D );
		tessellator.draw();

		GL11.glDisable( GL11.GL_BLEND );
		GL11.glPopMatrix();
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

	private float getEquipProcess(ItemRenderer itemRenderer, float partialTick) {
		float previousEquipProcess = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, 3);
		float currentEquipProcess = (Float) ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, itemRenderer, 2);

		return previousEquipProcess + (currentEquipProcess - previousEquipProcess) * partialTick;
	}

}
