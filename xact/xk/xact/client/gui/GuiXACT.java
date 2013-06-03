package xk.xact.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xk.xact.client.gui.tab.TabBase;
import xk.xact.client.gui.tab.TabbedGui;

import java.util.List;

/**
 * Base class for all the Gui components in XACT
 *
 * @author Xhamolk_
 */
public abstract class GuiXACT extends GuiContainer {

	public final boolean isTabbedGui;
	protected int mouseX = 0;
	protected int mouseY = 0;

	public GuiXACT(Container container) {
		super( container );
		this.isTabbedGui = this instanceof TabbedGui;
	}

	@Override
	public void handleMouseInput() {
		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		mouseX = x - guiLeft;
		mouseY = y - guiTop;
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) {
		super.mouseClicked( x, y, mouseButton );
		if( isTabbedGui ) {
			TabBase tab = TabBase.getTabAt( (TabbedGui) this, mouseX, mouseY );

			if( tab != null ) {
				List<TabBase> tabs = ((TabbedGui) this).getTabs();
				for( TabBase other : tabs ) {
					if( other != tab && other.side == tab.side && other.open ) {
						other.toggleOpen();
					}
				}
				tab.toggleOpen();
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		drawTitle();
		drawPostForeground( x, y );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
		drawBaseTexture();
		drawTabs();
		drawToolTip();
	}

	public String getGuiTitle() {
		return null;
	}

	protected abstract String getBaseTexture();

	protected void drawBaseTexture() {
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.mc.renderEngine.bindTexture( getBaseTexture() );
		this.drawTexturedModalRect( guiLeft, guiTop, 0, 0, this.xSize, this.ySize );
	}

	protected void drawTabs() {
		if( !isTabbedGui )
			return;
		List<TabBase> tabs = ((TabbedGui) this).getTabs();

		int yPosRight = 4;
		int yPosLeft = 4;

		for( TabBase tab : tabs ) {
			tab.update();
			if( tab.side == TabBase.TabSide.LEFT ) {
				tab.draw( guiLeft, guiTop + yPosLeft );
				yPosLeft += tab.currentHeight;
			} else if( tab.side == TabBase.TabSide.RIGHT ) {
				tab.draw( guiLeft + xSize, guiTop + yPosRight );
				yPosRight += tab.currentHeight;
			}
		}
	}

	protected void drawPostBackground(int x, int y) {
	}

	protected void drawPostForeground(int x, int y) {
	}

	protected void drawTitle() {
		String title = getGuiTitle();
		if( title != null && !title.isEmpty() ) {
			int xPos = (this.xSize - fontRenderer.getStringWidth( title )) / 2;
			this.fontRenderer.drawString( title, xPos, 8, 4210752 );
		}
	}

	protected void drawToolTip() {}
}
