package xk.xact.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xk.xact.client.GuiUtils;
import xk.xact.client.gui.hooks.DynamicSlotPaintHook;

/**
 * Base class for all the Gui components in XACT
 *
 * @author Xhamolk_
 */
public abstract class GuiXACT extends GuiContainer {

	public final boolean hasDynamicPaintingHook;

	protected int mouseX = 0;
	protected int mouseY = 0;

	public GuiXACT(Container container) {
		super( container );
		this.hasDynamicPaintingHook = this instanceof DynamicSlotPaintHook;
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
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		drawTitle();
		drawPostForeground( x, y );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
		drawBaseTexture();
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

	// ------------------------- Draw Slot --------------------------

	@Override
	protected void drawSlotInventory(Slot slot) {
		boolean delegateSuperCall = true;
		int underlayColor = -1;
		int overlayColor = 1;
		ItemStack itemToPaint = null;

		if( hasDynamicPaintingHook ) {
			DynamicSlotPaintHook dyn = (DynamicSlotPaintHook) this;
			boolean mouseOver = isMouseOverSlot( slot, mouseX, mouseY );

			delegateSuperCall = dyn.shouldDelegateSuperCall( slot );
			underlayColor = dyn.getSlotUnderlayColor( slot, mouseOver );
			overlayColor = dyn.getSlotOverlayColor( slot, mouseOver );
			itemToPaint = dyn.getStackToPaintInSlot( slot, mouseOver );
		}

		int dim = getSlotDimensions( slot );
		if( underlayColor != -1 ) { // Paint the slot's underlay color
			GuiUtils.paintOverlay( slot.xDisplayPosition, slot.yDisplayPosition, dim, underlayColor );
		}

		if( delegateSuperCall ) { // let the super class handle this.
			super.drawSlotInventory( slot );

		} else if( itemToPaint != null ) { // paint the item
			GuiUtils.paintItem( itemToPaint, slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
		}

		if( overlayColor != -1 ) { // Paint the slot's overlay color
			GuiUtils.paintOverlay( slot.xDisplayPosition, slot.yDisplayPosition, dim, overlayColor );
		}
	}

	protected int getSlotDimensions(Slot slot) {
		return 16;
	}

	// ---------------------------- Util ----------------------------

	public boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
		int dim = getSlotDimensions( slot );
		return this.isPointInRegion( slot.xDisplayPosition, slot.yDisplayPosition, dim, dim, mouseX, mouseY );
	}

	public Slot getSlotAt(int x, int y) {
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ ) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( i );
			if( isMouseOverSlot( slot, x, y ) )
				return slot;
		}
		return null;
	}

}
