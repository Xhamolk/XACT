package xk.xact.client.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.client.GuiUtils;
import xk.xact.util.Textures;


public class ButtonItem extends GuiButtonCustom {

	public ItemStack item;

	private boolean isSpecial = false;

	ButtonItem(ItemStack item, int posX, int posY) {
		super( posX, posY );
		this.item = item;
	}

	public void moveButton(int xDiff, int yDiff, int boundX, int boundY) {
		super.xPosition += xDiff;
		super.yPosition += yDiff;

		super.drawButton = (xPosition > 0 && xPosition < boundX)
				&& (yPosition > 0 && yPosition < boundY);
	}


	@Override
	protected void onModeSet(ICustomButtonMode mode) {
		this.isSpecial = mode == ICustomButtonMode.ItemModes.SPECIAL;
		super.width = super.height = isSpecial ? 26 : 22;
	}

	@Override
	protected void drawBackgroundLayer(Minecraft mc, int mouseX, int mouseY) {
		GuiUtils.bindTexture( TEXTURE_BUTTONS );
		int textureX = isSpecial ? 0 : 52;
		if( field_82253_i )
			textureX += 22;

		// Draw button.
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.drawTexturedModalRect( this.xPosition, this.yPosition, textureX, 14, this.width, this.height );
	}

	@Override
	protected void drawForegroundLayer(Minecraft mc, int mouseX, int mouseY) {
		// Draw the item
		if( item != null )
			paintItem( mc, item, xPosition, yPosition );
	}

	@Override
	protected boolean isModeValid(ICustomButtonMode mode) {
		return mode instanceof ICustomButtonMode.ItemModes;
	}

	private void paintItem(Minecraft mc, ItemStack item, int x, int y) {
		this.zLevel = 100.0F;

		x += (this.width - 16) / 2;
		y += (this.height - 16) / 2;

		itemRenderer.zLevel = 100.0F;
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		itemRenderer.renderItemAndEffectIntoGUI( mc.fontRenderer, mc.renderEngine, item, x, y );

		// don't paint the item's overlay (stack size and effect)
		// itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);

		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	private static RenderItem itemRenderer = new RenderItem();

}
