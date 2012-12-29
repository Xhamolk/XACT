package xk.xact.gui;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.core.TileCrafter;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.CustomPacket;

import java.io.IOException;

public class GuiCrafter extends GuiMachine {

	private TileCrafter crafter;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player){
		super(new ContainerCrafter(crafter, player));
		this.crafter = crafter;
		this.ySize = 256;
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
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/crafter_4.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		// Draw crafting grid
		int currentRecipe = getHoveredRecipe( mouseX, mouseY );
		if( hoveredRecipe != currentRecipe ) {
			updateGhostContents( currentRecipe );
		}

	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		// grid's contents.
		if( 8 <= slot.slotNumber && slot.slotNumber < 18-1 ) {
			int index = slot.slotNumber - 8;

			// only paint the grid's real contents if there is no recipe being hovered.
			if( hoveredRecipe == -1 ) {
				super.drawSlotInventory( slot );
			}
			// If a recipe is being hovered, paint those ingredients instead.
			else {
				ItemStack itemToPaint = gridContents[index];

				// Paint the "ghost item"
				GuiUtils.paintItem( itemToPaint, slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
			}

			// Paint the item's overlay.
			int color = missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY;
			color |= TRANSPARENCY;
			GuiUtils.paintOverlay( slot.xDisplayPosition, slot.yDisplayPosition, 16, color );

			return;
		}

		if( slot.getHasStack() ) {
			// output slots.
			if( slot.slotNumber < 4 ) {
				// paint slot's colored underlay.
				GuiUtils.paintSlotOverlay(slot, 22, getColorFor( slot.getSlotIndex() ));
			}
			// show the chip's recipe's output when holding shift.
			else if( slot.slotNumber >= 18 && GuiUtils.isShiftKeyPressed() ) {
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
		color |= TRANSPARENCY; // transparency layer.

		return color;
	}

	private int getHoveredRecipe(int mouseX, int mouseY) {
		for( int i = 0; i < 4; i++ ) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( i );

			if( slot != null && slot.getHasStack() ) {
				if( func_74188_c(slot.xDisplayPosition - 3, slot.yDisplayPosition - 3, 22, 22, mouseX, mouseY) ) {
					return i;
				}
			}
		}
		return -1;
	}

	private int hoveredRecipe = -1;
	public ItemStack[] gridContents = new ItemStack[9];
	public boolean[] missingIngredients = new boolean[9];
	private static final int TRANSPARENCY = 128 << 24; // 50%

	private void updateGhostContents( int newIndex ) {
		this.hoveredRecipe = newIndex == -1 ? -1 : newIndex % 4;
		gridContents = new ItemStack[9];
		missingIngredients = new boolean[9];

		// request the update from the server.
		try {
			CustomPacket cPacket = new CustomPacket((byte) 0x08).add( (byte) hoveredRecipe );
			this.mc.getSendQueue().addToSendQueue( cPacket.toPacket() );
		} catch (IOException e) {
			FMLCommonHandler.instance().raiseException(e, "XACT: Custom Packet, 0x08", true);
		}
	}

}
