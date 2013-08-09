package xk.xact.client.gui;


import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import xk.xact.client.GuiUtils;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Textures;

public class GuiCase extends GuiXACT {

	private static final ResourceLocation guiTexture = new ResourceLocation( Textures.GUI_CASE );

	public GuiCase(Container container) {
		super( container );
		this.xSize = 196;
		this.ySize = 191;
	}

	private Slot slot;

	@Override
	protected ResourceLocation getBaseTexture() {
		return guiTexture;
	}

	@Override
	protected void drawPostForeground(int x, int y) {
		if( slot != null ) {
			drawRecipe( slot.getStack() );
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
		super.drawGuiContainerBackgroundLayer( partial, x, y );

		this.slot = findSlotAt( x, y );
		if( slot != null ) {
			// paint the background grid.
			this.drawTexturedModalRect( guiLeft + 14, guiTop + 16, 197, 9, 52, 77 );
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
		Slot slot = getSlotAt( x, y );
		if( slot != null && slot.getHasStack() ) {
			if( CraftManager.isEncoded( slot.getStack() ) ) {
				return slot;
			}
		}
		return null;
	}

	protected void drawRecipe(ItemStack chip) {
		if( chip == null )
			return;
		CraftRecipe recipe = RecipeUtils.getRecipe( chip, this.mc.thePlayer.worldObj );
		if( recipe == null )
			return;

		ItemStack result = recipe.getResult();
		GuiUtils.paintItem( result, 32, 17, this.mc, itemRenderer );

		ItemStack[] ingredients = recipe.getIngredients();
		for( int i = 0; i < 3; i++ ) {
			for( int e = 0; e < 3; e++ ) {
				int index = i * 3 + e;
				GuiUtils.paintItem( ingredients[index], e * 18 + 14, i * 18 + 41, this.mc, itemRenderer );
			}
		}
	}

}
