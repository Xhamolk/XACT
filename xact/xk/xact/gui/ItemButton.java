package xk.xact.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ItemButton extends GuiButton {



	public ItemStack item;

	private boolean isSpecial = false;

	public ItemButton( int id, ItemStack item, int posX, int posY, boolean isSpecial) {
		super( id, posX, posY, isSpecial ? 26 : 22, isSpecial ? 26 : 22, "");
		this.isSpecial = isSpecial;
		this.item = item;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if( this.drawButton ) {

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/plan_1.png"));

			int textureX = isSpecial ? 0 : 26;

			// is hovering?
			this.field_82253_i = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				// todo: if( hovering ) textureX += 48;

			// Draw button.
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, textureX, 202, this.width, this.height);

			// Draw the item
			if( item != null )
				paintItem(mc, item, xPosition,yPosition);

			// FontRenderer fontRenderer = mc.fontRenderer;
			// this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, textColor);
		}
	}

	public void moveButton(int xDiff, int yDiff, int boundX, int boundY) {
		super.xPosition += xDiff;
		super.yPosition += yDiff;

		super.drawButton = ( xPosition > 0 && xPosition < boundX )
			&& ( yPosition > 0 && yPosition < boundY );
	}

	private void paintItem(Minecraft mc, ItemStack item, int x, int y) {
		this.zLevel = 100.0F;

		x += (this.width - 16) / 2;
		y += (this.height - 16) / 2;

		itemRenderer.zLevel = 100.0F;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);

		// I don't think I want the item's overlay, at least right now.
		// itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);

		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	private static RenderItem itemRenderer = new RenderItem();

}
