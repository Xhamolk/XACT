package xk.xact.client.gui.tab;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.Icon;
import org.lwjgl.opengl.GL11;
import xk.xact.client.GuiUtils;
import xk.xact.client.gui.hooks.TabbedGui;

public abstract class TabBase {

	protected int posX;
	protected int posY;

	protected int sizeX;
	protected int sizeY;

	public int texW = 256;
	public int texH = 256;

	////////////////////////

	public boolean open = false;
	public final TabSide side;
	public final GuiContainer gui;

	public int backgroundColor = 0xffffff;

	public int currentShiftX = 0;
	public int currentShiftY = 0;

	public int minWidth = 22;
	public int maxWidth = 124;
	public int currentWidth = minWidth;

	public int minHeight = 22;
	public int maxHeight = 22;
	public int currentHeight = minHeight;

	public TabBase(GuiContainer gui, TabSide side) {
		this.gui = gui;
		this.side = side;
	}

	public void update() {
		if (open && currentWidth < maxWidth) {
			currentWidth += 8;
		} else if (!open && currentWidth > minWidth) {
			currentWidth -= 8;
		}

		if (currentWidth > maxWidth) {
			currentWidth = maxWidth;
		} else if (currentWidth < minWidth) {
			currentWidth = minWidth;
		}

		if (open && currentHeight < maxHeight) {
			currentHeight += 8;
		} else if (!open && currentHeight > minHeight) {
			currentHeight -= 8;
		}

		if (currentHeight > maxHeight) {
			currentHeight = maxHeight;
		} else if (currentHeight < minHeight) {
			currentHeight = minHeight;
		}

		if (open && currentWidth == maxWidth && currentHeight == maxHeight) {
			setFullyOpen();
		}
	}

	public boolean intersectsWith(int mouseX, int mouseY, int shiftY) {
		switch( side ) {
			case LEFT:
				return mouseX <= 0 && mouseX >= -currentWidth && mouseY >= shiftY && mouseY <= currentHeight;
			case RIGHT:
				return mouseX >= gui.xSize && mouseX <= gui.xSize + currentWidth && mouseY >= shiftY && mouseY <= shiftY + currentHeight;
		}
		return false;
	}


	public void draw(int x, int y) {
		posX = x;
		posY = y;
		drawBackground();
		drawTabIcon( getTabIcon() );
	}

	public boolean isFullyOpened() {
		return currentWidth >= maxWidth;
	}

	public void setFullyOpen() {
		open = true;
		currentWidth = maxWidth;
		currentHeight = maxHeight;
	}

	public void toggleOpen() {
		if (open) {
			open = false;
			side.openTab = null;
		} else {
			open = true;
			side.openTab = this.getClass();
		}
	}

	protected abstract Icon getTabIcon();

	protected void drawBackground() {

		float colorR = (backgroundColor >> 16 & 255) / 255.0F;
		float colorG = (backgroundColor >> 8 & 255) / 255.0F;
		float colorB = (backgroundColor & 255) / 255.0F;

		GL11.glColor4f( colorR, colorG, colorB, 1.0F );

		GuiUtils.bindTexture( side.baseTexture );
		switch( side ) {
			case LEFT:
				gui.drawTexturedModalRect(posX - currentWidth, posY + 4, 0, 256 - currentHeight + 4, 4, currentHeight - 4);
				gui.drawTexturedModalRect(posX - currentWidth + 4, posY, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
				gui.drawTexturedModalRect(posX - currentWidth, posY, 0, 0, 4, 4);
				gui.drawTexturedModalRect(posX - currentWidth + 4, posY + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);
				break;
			case RIGHT:
				gui.drawTexturedModalRect(posX, posY, 0, 256 - currentHeight, 4, currentHeight);
				gui.drawTexturedModalRect(posX + 4, posY, 256 - currentWidth + 4, 0, currentWidth - 4, 4);
				gui.drawTexturedModalRect(posX, posY, 0, 0, 4, 4);
				gui.drawTexturedModalRect(posX + 4, posY + 4, 256 - currentWidth + 4, 256 - currentHeight + 4, currentWidth - 4, currentHeight - 4);
				break;

		}
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
	}

	protected void drawTabIcon(Icon icon) {
		int offsetX = side == TabSide.LEFT ? 4 - currentWidth : 2;
		GuiUtils.paintIcon( gui, icon, posX + offsetX, posY + 3 );
	}

	public static TabBase getTabAt(TabbedGui gui, int mouseX, int mouseY) {
		int yShift = 4;

		for( TabBase tab : gui.getTabs() ) {
			if( tab.side != TabSide.LEFT )
				continue;

			tab.currentShiftY = yShift;
			if( tab.intersectsWith( mouseX, mouseY, yShift ) ) {
				return tab;
			}
			yShift += tab.currentHeight;
		}
		yShift = 4;

		for( TabBase tab : gui.getTabs() ) {
			if( tab.side != TabSide.RIGHT )
				continue;

			tab.currentShiftY = yShift;
			if( tab.intersectsWith( mouseX, mouseY, yShift ) ) {
				return tab;
			}
			yShift += tab.currentHeight;
		}
		return null;
	}

	public static enum TabSide {  // todo: set the texture files.
		LEFT("tabLeft"), RIGHT("tabRight");

		final String baseTexture;
		TabSide(String baseTexture) {
			this.baseTexture = "" + baseTexture +".png";
		}

		Class<? extends TabBase> openTab = null;
	}
}
