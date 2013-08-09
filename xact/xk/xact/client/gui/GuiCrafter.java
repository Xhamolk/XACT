package xk.xact.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import xk.xact.XActMod;
import xk.xact.client.GuiUtils;
import xk.xact.client.button.CustomButtons;
import xk.xact.client.button.GuiButtonCustom;
import xk.xact.client.button.ICustomButtonMode;
import xk.xact.core.items.ItemChip;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.gui.ContainerCrafter;
import xk.xact.network.ClientProxy;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.Textures;
import xk.xact.util.Utils;

public class GuiCrafter extends GuiCrafting {

	private static final ResourceLocation guiTexture = new ResourceLocation( Textures.GUI_CRAFTER );

	private TileCrafter crafter;
	private ContainerCrafter container;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player) {
		super( new ContainerCrafter( crafter, player ) );
		this.crafter = crafter;
		this.container = (ContainerCrafter) super.inventorySlots;
		this.ySize = 256;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		updateGhostContents( -1 );
		/* Buttons:
			 *  42, 21.     120, 21
			 *  42, 65.     120, 65
		 */
		buttonList.clear();

		for( int i = 0; i < 4; i++ ) {
			int x = (i % 2 == 0 ? 42 : 120) + this.guiLeft;
			int y = (i / 2 == 0 ? 21 : 65) + this.guiTop;

			GuiButtonCustom button = CustomButtons.createdDeviceButton( x, y );
			button.id = i;
			buttonList.add( buttons[i] = button );
		}
		invalidated = true;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if( invalidated || crafter.recentlyUpdated ) {

			for( int i = 0; i < 4; i++ ) {
				ItemStack chip = crafter.circuits.getStackInSlot( i );
				if( chip == null ) {
					buttons[i].setMode( ICustomButtonMode.DeviceModes.INACTIVE );
					continue;
				}

				if( chip.getItem() instanceof ItemChip ) {
					if( !((ItemChip) chip.getItem()).encoded ) {
						CraftRecipe mainRecipe = crafter.getRecipe( 4 ); // the recipe on the grid
						if( mainRecipe != null && mainRecipe.isValid() ) {
							buttons[i].setMode( ICustomButtonMode.DeviceModes.SAVE );
							continue;
						}
						buttons[i].setMode( ICustomButtonMode.DeviceModes.INACTIVE );
						continue;
					}
					buttons[i].setMode( ICustomButtonMode.DeviceModes.CLEAR );
				}
			}
			invalidated = false;
			crafter.recentlyUpdated = false;
		}
	}

	@Override
	protected void drawTitle() {
		int xPos = (this.xSize - fontRenderer.getStringWidth( "X.A.C.T. Crafter" )) / 2;
		this.fontRenderer.drawString( "X.A.C.T. Crafter", xPos, 6, 4210752 );
		this.fontRenderer.drawString( "Player's Inventory", 8, this.ySize - 94, 4210752 );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer( var1, mouseX, mouseY );
		// Draw crafting grid
		int currentRecipe = getHoveredRecipe( mouseX, mouseY );
		if( hoveredRecipe != currentRecipe ) {
			updateGhostContents( currentRecipe );
		}
	}

	@Override
	protected ResourceLocation getBaseTexture() {
		return guiTexture;
	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		// grid's contents.
		if( 8 <= slot.slotNumber && slot.slotNumber < 18 - 1 ) {
			int index = slot.slotNumber - 8;

			// only paint the grid's real contents if there is no recipe being hovered.
			if( hoveredRecipe == -1 ) {
				super.drawSlotInventory( slot );
			}
			// If a recipe is being hovered, paint those ingredients instead.
			else {
				// Paint the "ghost item"
				ItemStack itemToPaint = gridContents[index];
				GuiUtils.paintItem( itemToPaint, slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
			}

			// Paint the item's overlay.
			int color = getColorForGridSlot( slot );
			if( color != -1 )
				GuiUtils.paintOverlay( slot.xDisplayPosition, slot.yDisplayPosition, 16, color );
			return;
		}

		if( slot.getHasStack() && slot.slotNumber < 4 ) { // output slots.
			// paint slot's colored underlay if the slot is hovered.
			if( slot.slotNumber == hoveredRecipe )
				GuiUtils.paintSlotOverlay( slot, 22, getColorForOutputSlot( slot.getSlotIndex() ) );
		}

		super.drawSlotInventory( slot );
	}

	private int getColorForOutputSlot(int recipeIndex) {
		int color;
		if( this.mc.thePlayer.capabilities.isCreativeMode ) {
			color = GuiUtils.COLOR_BLUE;
		} else if( Utils.anyOf( container.recipeStates[recipeIndex] ) ) {
			color = GuiUtils.COLOR_RED;
		} else {
			color = GuiUtils.COLOR_GREEN;
		}
		color |= TRANSPARENCY; // transparency layer.

		return color;
	}

	private int getColorForGridSlot(Slot slot) {
//		ItemStack itemInSlot = slot.getStack();
//		if( itemInSlot != null && itemInSlot.stackSize > 0 ) {
//			return -1; // no overlay when the slot contains "real" items.
//		}
		int index = slot.slotNumber - 8;
		boolean[] missingIngredients = container.recipeStates[hoveredRecipe == -1 ? 4 : hoveredRecipe];
		int color = missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY;
		return color | TRANSPARENCY;
	}

	private int getHoveredRecipe(int mouseX, int mouseY) {
		for( int i = 0; i < 4; i++ ) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( i );

			if( slot != null && slot.getHasStack() ) {
				if( isPointInRegion( slot.xDisplayPosition - 3, slot.yDisplayPosition - 3, 22, 22, mouseX, mouseY ) ) {
					return i;
				}
			}
		}
		return -1;
	}

	private int hoveredRecipe = -1;
	private final ItemStack[] emptyGrid = new ItemStack[9];
	public ItemStack[] gridContents = emptyGrid;
	private static final int TRANSPARENCY = 128 << 24; // 50%

	private void updateGhostContents(int newIndex) {
		this.hoveredRecipe = newIndex == -1 ? -1 : newIndex % 4;
		if( hoveredRecipe != -1 ) {
			CraftRecipe recipe = crafter.getRecipe( hoveredRecipe );
			gridContents = recipe == null ? emptyGrid : recipe.getIngredients();
		}
	}


	// -------------------- InteractiveCraftingGui --------------------

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if( ingredients == null ) {
			GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) -1, null );
			return;
		}
		GuiUtils.sendItemsToServer( ClientProxy.getNetClientHandler(), ingredients, 8 );
	}

	// -------------------- Buttons --------------------

	private GuiButtonCustom[] buttons = new GuiButtonCustom[4];

	private boolean invalidated = true;

	@Override
	protected void actionPerformed(GuiButton button) {
		if( button instanceof GuiButtonCustom ) {
			int action = ((GuiButtonCustom) button).getAction();

			if( action == 1 ) { // SAVE
				ItemStack stack = CraftManager.encodeRecipe( crafter.getRecipe( 4 ) );
				GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) (4 + button.id), stack );
				return;
			}
			if( action == 3 ) { // CLEAR
				GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) (4 + button.id), new ItemStack( XActMod.itemRecipeBlank ) );
			}
		}
	}

}
