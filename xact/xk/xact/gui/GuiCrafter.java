package xk.xact.gui;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.core.TileCrafter;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class GuiCrafter extends GuiMachine {

	private TileCrafter crafter;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player){
		super(new ContainerCrafter(crafter, player));
		this.crafter = crafter;
		this.ySize = 220;
	}

	public void onInit() {
		crafter.updateRecipes();
		crafter.updateStates();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int xPos = (this.xSize - fontRenderer.getStringWidth("X.A.C.T. Crafter")) / 2;
		this.fontRenderer.drawString("X.A.C.T. Crafter", xPos, 6, 4210752);
		this.fontRenderer.drawString("Player's Inventory", 8, this.ySize - 94, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/crafter_2.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		if( slot.getHasStack() ) {
			// output slots.
			if( slot.slotNumber < 4 ) {
				// paint slot's colored underlay.
				GuiUtils.paintSlotOverlay(slot, 24, getColorFor( slot.getSlotIndex() ));
			}
			else if( slot.slotNumber >= 8 && GuiUtils.isShiftKeyPressed() ) {
				ItemStack stack = slot.getStack();
				if( CraftManager.isEncoded(stack) ) {
					// paint chip's recipe's result
					CraftRecipe recipe = RecipeUtils.getRecipe(stack, this.mc.theWorld);
					if( recipe != null ) {
						GuiUtils.paintItem( recipe.getResult(), slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
						GuiUtils.paintGreenEffect( slot, itemRenderer );
						return;
					}
				}
			}
		}

		super.drawSlotInventory(slot);
	}

	private int getColorFor(int recipeIndex) {
		int color;
		if( this.mc.thePlayer.capabilities.isCreativeMode ) {
			color = GuiUtils.COLOR_BLUE;
		} else if( crafter.isRedState( recipeIndex )) {
			color = GuiUtils.COLOR_RED;
		} else {
			color = GuiUtils.COLOR_GREEN;
		}
		color |= ( 128 << 24 ); // transparency layer.

		return color;
	}

}
