package xk.xact.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

/**
 * Renders "ghost" items on the GUI.
 * This allows items to be drawn with dynamic transparency.
 * The majority of the code has been taken from Minecraft's RenderItem.
 *
 * @author Xhamolk_
 */
public class RenderGhostItem extends Render {

	/**
	 * the alpha value to render the stack.
	 */
	private float alpha = 1.0f;

	/**
	 * the stack to be rendered.
	 */
	private ItemStack stack = null;

	private FontRenderer fontRenderer;

	private RenderEngine renderEngine;

	private float zLevel = 180.0F;

	@Override // does nothing. Is never called.
	public void doRender(Entity entity, double d1, double d2, double d3, float f1, float f2) { }


	/**
	 * Sets the transparency value.
	 * Expected values range from 0.0F (totally invisible) to 1.0f (totally opaque).
	 * @param transparency the alpha value used to render the stack.
	 */
	public void setTransparency( float transparency ){
		this.alpha = transparency;
	}

	/**
	 * Configures this Render instance to work with the specified FontRenderer and RenderEngine.
	 * @param fontRenderer the FontRenderer to be used.
	 * @param renderEngine the RenderEngine to be used.
	 */
	public void configure(FontRenderer fontRenderer, RenderEngine renderEngine){
		this.fontRenderer = fontRenderer;
		this.renderEngine = renderEngine;
	}

	/**
	 * Renders the specified stack on the GUI Screen.
	 * @param stack the ItemStack to render.
	 * @param x the x coordinate of the screen.
	 * @param y the y coordinate of the screen.
	 */
	public void renderStackOnGUI(ItemStack stack, int x, int y) {
		if( stack == null )
			return;
		this.stack = stack;

		// draw the item ( the actual item )
		drawItemIntoGui(x, y);

        // draw the "transparency" layer. (the gray layer).
        int grayScale = 139, color = 0;
            // todo Do i want to vary this overlay color?
        color = (color | grayScale) << 4; // red
        color = (color | grayScale) << 4; // green
        color = (color | grayScale) << 4; // blue
        color |= 150;  // alpha
        this.zLevel = 500;
        Gui.drawRect(x, y, x+16, y+16, color);

		// draw the effect ( glow )
		drawItemEffect(x, y);

		// draw the item overlay ( stack size and damage bar )
		drawItemOverlay(x, y);
	}

	/**
	 * Handles the drawing of the actual item.
	 * @param x the x coordinate of the screen.
	 * @param y the y coordinate of the screen.
	 */
	protected void drawItemIntoGui(int x, int y) {
		int itemID = stack.itemID;
		int itemDamage = stack.getItemDamage();
		int iconIndex = stack.getIconIndex();

		// let forge handle the rendering?
		if (ForgeHooksClient.renderInventoryItem(renderBlocks, renderEngine, stack, true, zLevel, (float) x, (float) y))
			return; // done by a mod's custom renderer.

		// draw the image
		rendering: {

			// item blocks.
			if (Item.itemsList[itemID] instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.blocksList[itemID].getRenderType())) {
				int texture = renderEngine.getTexture(Block.blocksList[itemID].getTextureFile());
				renderEngine.bindTexture(texture);
				Block var15 = Block.blocksList[itemID];
				GL11.glPushMatrix();
				GL11.glTranslatef((float)(x - 2), (float)(y + 3), -3.0F + this.zLevel);
				GL11.glScalef(10.0F, 10.0F, 10.0F);
				GL11.glTranslatef(1.0F, 0.5F, 1.0F);
				GL11.glScalef(1.0F, 1.0F, -1.0F);
				GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				int colorFromDamage = Item.itemsList[itemID].func_82790_a(stack, 0); // getColorFromDamage
				float red = (float)(colorFromDamage >> 16 & 255) / 255.0F;
				float green = (float)(colorFromDamage >> 8 & 255) / 255.0F;
				float blue = (float)(colorFromDamage & 255) / 255.0F;

				GL11.glColor4f(red, green, blue, alpha);

				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
				this.renderBlocks.useInventoryTint = true;
				this.renderBlocks.renderBlockAsItem(var15, itemDamage, 1.0F);
				GL11.glPopMatrix();

				break rendering;
			}

			// multiple-passes rendered items
			if (Item.itemsList[itemID].requiresMultipleRenderPasses()) {
				GL11.glDisable(GL11.GL_LIGHTING);
				renderEngine.bindTexture(renderEngine.getTexture(Item.itemsList[itemID].getTextureFile()));
				int renderPasses = Item.itemsList[itemID].getRenderPasses(itemDamage);

				for (int i = 0; i <= renderPasses; ++i) {
					int iconFromDamage = Item.itemsList[itemID].getIconFromDamageForRenderPass(itemDamage, i);
					int colorFromDamage = Item.itemsList[itemID].func_82790_a(stack, 0); // getColorFromDamage
					float var11 = (float)(colorFromDamage >> 16 & 255) / 255.0F;
					float var12 = (float)(colorFromDamage >> 8 & 255) / 255.0F;
					float var13 = (float)(colorFromDamage & 255) / 255.0F;

					GL11.glColor4f(var11, var12, var13, alpha);

					this.renderTexturedQuad(x, y, iconFromDamage % 16 * 16, iconFromDamage / 16 * 16, 16, 16);
				}
				GL11.glEnable(GL11.GL_LIGHTING);
				break rendering;
			}

			// normal items
			if (iconIndex >= 0) {
				GL11.glDisable(GL11.GL_LIGHTING);

				renderEngine.bindTexture(renderEngine.getTexture(Item.itemsList[itemID].getTextureFile()));

				int colorFromDamage = Item.itemsList[itemID].func_82790_a(stack, 0); // getColorFromDamage
				float red = (float)(colorFromDamage >> 16 & 255) / 255.0F;
				float green = (float)(colorFromDamage >> 8 & 255) / 255.0F;
				float blue = (float)(colorFromDamage & 255) / 255.0F;

				GL11.glColor4f(red, green, blue, alpha);

				this.renderTexturedQuad(x, y, (iconIndex % 16) * 16, (iconIndex / 16) * 16, 16, 16);
				GL11.glEnable(GL11.GL_LIGHTING);
			}

		}
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	/**
	 * Handles the drawing of the effect of the item (if any).
	 * @param x the x coordinate of the screen.
	 * @param y the y coordinate of the screen.
	 */
	protected void drawItemEffect(int x, int y) {
		if( stack.hasEffect() ){
			GL11.glDepthFunc(GL11.GL_GREATER);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			renderEngine.bindTexture(renderEngine.getTexture("%blur%/misc/glint.png"));
			this.zLevel -= 50.0F;
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_DST_COLOR);
			GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);

//			this.func_77018_a(x * 431278612 + y * 32178161, x - 2, y - 2, 20, 20);
			renderEffect(x - 2, y - 2, 20, 20);

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthMask(true);
			this.zLevel += 50.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		}
	}

	/**
	 * Draws the overlay of the item.
	 * Includes the stack's size and the damage bar (when they apply).
	 * @param x the x coordinate of the screen.
	 * @param y the y coordinate of the screen.
	 */
	protected void drawItemOverlay(int x, int y) {

		// paint the stack size
		if (stack.stackSize > 1) {
			String stackSize = "" + stack.stackSize;
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			fontRenderer.drawStringWithShadow(stackSize, x + 17 - fontRenderer.getStringWidth(stackSize), y + 6 + 3, 16777215);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		// paint the damage bar
		if (stack.isItemDamaged()) {
			int var11 = (int)Math.round(13.0D - (double)stack.getItemDamageForDisplay() * 13.0D / (double)stack.getMaxDamage());
			int var7 = (int)Math.round(255.0D - (double)stack.getItemDamageForDisplay() * 255.0D / (double)stack.getMaxDamage());
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Tessellator tessellator = Tessellator.instance;
			int var9 = 255 - var7 << 16 | var7 << 8;
			int var10 = (255 - var7) / 4 << 16 | 16128;
			this.renderQuad(tessellator, x + 2, y + 13, 13, 2, 0);
			this.renderQuad(tessellator, x + 2, y + 13, 12, 1, var10);
			this.renderQuad(tessellator, x + 2, y + 13, var11, 1, var9);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	///////////////
	///// the methods that do the dirty job (all of them were stolen from RenderItem)

	// stolen
	private void renderItemIcon(int textureIndex) {
		Tessellator tessellator = Tessellator.instance;
		float startX = (float)(textureIndex % 16 * 16 + 0) / 256.0F;
		float lastX = (float)(textureIndex % 16 * 16 + 16) / 256.0F;
		float startY = (float)(textureIndex / 16 * 16 + 0) / 256.0F;
		float lastY = (float)(textureIndex / 16 * 16 + 16) / 256.0F;

		float var8 = 1.0F;
		float var9 = 0.5F;
		float var10 = 0.25F;

		GL11.glPushMatrix();

		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV((double) (0.0F - var9), (double) (0.0F - var10), 0.0D, (double) startX, (double) lastY);
		tessellator.addVertexWithUV((double) (var8 - var9), (double) (0.0F - var10), 0.0D, (double) lastX, (double) lastY);
		tessellator.addVertexWithUV((double) (var8 - var9), (double) (1.0F - var10), 0.0D, (double) lastX, (double) startY);
		tessellator.addVertexWithUV((double) (0.0F - var9), (double) (1.0F - var10), 0.0D, (double) startX, (double) startY);
		tessellator.draw();
		GL11.glPopMatrix();
	}

	// stolen
	private void renderEffect( int x, int y, int width, int height ){
		for (int i = 0; i < 2; ++i) {

			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

			float var7 = 0.00390625F;
			float var8 = 0.00390625F;
			float var9 = (float)(Minecraft.getSystemTime() % (long)(3000 + i * 1873)) / (3000.0F + (float)(i * 1873)) * 256.0F;
			float var10 = 0.0F;
			Tessellator tessellator = Tessellator.instance;
			float var12 = 4.0F;

			if (i == 1)
				var12 = -1.0F;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV((double) (x + 0), (double) (y + height), (double) this.zLevel, (double) ((var9 + (float) height * var12) * var7), (double) ((var10 + (float) height) * var8));
			tessellator.addVertexWithUV((double) (x + width), (double) (y + height), (double) this.zLevel, (double) ((var9 + (float) width + (float) height * var12) * var7), (double) ((var10 + (float) height) * var8));
			tessellator.addVertexWithUV((double) (x + width), (double) (y + 0), (double) this.zLevel, (double) ((var9 + (float) width) * var7), (double) ((var10 + 0.0F) * var8));
			tessellator.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) this.zLevel, (double) ((var9 + 0.0F) * var7), (double) ((var10 + 0.0F) * var8));
			tessellator.draw();
		}
	}

	// stolen
	private void renderQuad(Tessellator tessellator, int par2, int par3, int par4, int par5, int color){
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(color);
		tessellator.addVertex((double) (par2 + 0), (double) (par3 + 0), 0.0D);
		tessellator.addVertex((double) (par2 + 0), (double) (par3 + par5), 0.0D);
		tessellator.addVertex((double) (par2 + par4), (double) (par3 + par5), 0.0D);
		tessellator.addVertex((double) (par2 + par4), (double) (par3 + 0), 0.0D);
		tessellator.draw();
	}

	// stolen
	private void renderTexturedQuad(int par1, int par2, int par3, int par4, int par5, int par6) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) (par1 + 0), (double) (par2 + par6), (double) this.zLevel, (double) ((float) (par3 + 0) * var7), (double) ((float) (par4 + par6) * var8));
		tessellator.addVertexWithUV((double) (par1 + par5), (double) (par2 + par6), (double) this.zLevel, (double) ((float) (par3 + par5) * var7), (double) ((float) (par4 + par6) * var8));
		tessellator.addVertexWithUV((double) (par1 + par5), (double) (par2 + 0), (double) this.zLevel, (double) ((float) (par3 + par5) * var7), (double) ((float) (par4 + 0) * var8));
		tessellator.addVertexWithUV((double) (par1 + 0), (double) (par2 + 0), (double) this.zLevel, (double) ((float) (par3 + 0) * var7), (double) ((float) (par4 + 0) * var8));
		tessellator.draw();
	}

}

