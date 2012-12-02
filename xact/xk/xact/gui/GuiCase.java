package xk.xact.gui;


import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;

public class GuiCase extends GuiContainer {

	public GuiCase(Container container) {
		super(container);
		this.xSize = 196;
		this.ySize = 191;
	}

	private Slot slot;

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int x, int y) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/chip_case.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		this.slot = findSlotAt(x, y);
		if( slot != null ) {
			// paint the background grid.
			this.drawTexturedModalRect(cornerX+14, cornerY+16, 		197, 9, 	52, 77);
		}
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if( slot != null ) {
			drawRecipe(slot.getStack());
		}
	}


	// the painted grid:
		// position: (14, 16), size (52x77)
		// texture position: (197, 9)

	// painted slots:
		// main: 31, 16
		// first: 13, 40 (slot size 18x18)


	// to paint the items:
		// func_85044_b(ItemStack stack, int x, int y)        // copy this method, as it's private.
	// tooltip: func_74184_a(ItemStack stack, int x, int y)   // this one is protected, so don't copy.


	private Slot findSlotAt(int x, int y) {
		Slot slot = getSlotAt(x, y);
		if( slot != null && slot.getHasStack() ) {
			if( CraftManager.isEncoded(slot.getStack()) ) {
				return slot;
			}
		}
		return null;
	}

	private boolean isMouseOverSlot(Slot slot, int x, int y) {
		return this.func_74188_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y);
	}

	private Slot getSlotAt(int x, int y) {
		for ( int i=0; i<this.inventorySlots.inventorySlots.size(); i++ ) {
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i);
			if ( isMouseOverSlot(slot, x, y) )
				return slot;
		}
		return null;
	}

	protected void drawRecipe(ItemStack chip) {
		if( chip == null )
			return;
		CraftRecipe recipe = CraftManager.decodeRecipe(chip);

		ItemStack result = recipe.getResult();
		drawItem(result, 32, 17);

		ItemStack[] ingredients = recipe.getIngredients();
		for( int i=0; i<3; i++ ){
			for( int e=0; e<3; e++ ){
				int index = i*3 +e;
				drawItem(ingredients[index], e*18 + 14, i*18 +41);
			}
		}
	}

	private void drawItem(ItemStack itemStack, int x, int y) {
		if( itemStack == null )
			return; // I might want to have a "null" image, like background image.

		itemRenderer.zLevel = 100.0F;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, itemStack, x, y);
		itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, itemStack, x, y);
		itemRenderer.zLevel = 0.0F;
	}

    @Override
    public void handleMouseClick(Slot slot, int par2, int par3, int par4) {
        if( slot != null && slot.getHasStack() ){
            ItemStack stackInSlot = slot.getStack();
            if( stackInSlot.itemID == XActMod.itemChipCase.shiftedIndex && stackInSlot.getItemDamage() == 1 ) {
				this.mc.thePlayer.sendChatMessage("Can't move the Chip Case while it's in use.");
                return;
			}
        }
        super.handleMouseClick(slot, par2, par3, par4);
    }

}
