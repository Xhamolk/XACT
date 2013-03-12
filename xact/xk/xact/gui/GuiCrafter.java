package xk.xact.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.config.Textures;
import xk.xact.core.ItemChip;
import xk.xact.core.TileCrafter;
import xk.xact.gui.button.CustomButtons;
import xk.xact.gui.button.GuiButtonCustom;
import xk.xact.gui.button.ICustomButtonMode;
import xk.xact.network.ClientProxy;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class GuiCrafter extends CraftingGui {

	private TileCrafter crafter;

	public GuiCrafter(TileCrafter crafter, EntityPlayer player) {
		super( new ContainerCrafter( crafter, player ) );
		this.crafter = crafter;
		this.ySize = 256;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		crafter.updateRecipes();
		crafter.updateStates();
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

		if( invalidated || crafter.contentsChanged ) {

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
							buttons[i] .setMode( ICustomButtonMode.DeviceModes.SAVE );
							continue;
						}
						buttons[i] .setMode( ICustomButtonMode.DeviceModes.INACTIVE );
						continue;
					}
					buttons[i].setMode( ICustomButtonMode.DeviceModes.CLEAR );
				}
			}
			invalidated = false;
			crafter.contentsChanged = false;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int xPos = (this.xSize - fontRenderer .getStringWidth( "X.A.C.T. Crafter" )) / 2;
		this.fontRenderer.drawString( "X.A.C.T. Crafter", xPos, 6, 4210752 );
		this.fontRenderer.drawString( "Player's Inventory", 8, this.ySize - 94, 4210752 );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY) {
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.mc.renderEngine.func_98187_b( Textures.GUI_CRAFTER ); // bind texture
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect( cornerX, cornerY, 0, 0, this.xSize, this.ySize );

		// Draw crafting grid
		int currentRecipe = getHoveredRecipe( mouseX, mouseY );
		if( hoveredRecipe != currentRecipe ) {
			updateGhostContents( currentRecipe );
		}

	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		// grid's contents.
		if( 8 <= slot.slotNumber && slot.slotNumber < 18 - 1 ) {
			int index = slot.slotNumber - 8;
			int color;

			// only paint the grid's real contents if there is no recipe being
			// hovered.
			if( hoveredRecipe == -1 ) {
				super.drawSlotInventory( slot );
				color = crafter.missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY;
			}
			// If a recipe is being hovered, paint those ingredients instead.
			else {
				ItemStack itemToPaint = gridContents[index];

				// Paint the "ghost item"
				GuiUtils.paintItem( itemToPaint, slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
				color = missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY;
			}

			// Paint the item's overlay.
			color |= TRANSPARENCY;
			GuiUtils.paintOverlay( slot.xDisplayPosition, slot.yDisplayPosition, 16, color );

			return;
		}

		if( slot.getHasStack() ) {
			// output slots.
			if( slot.slotNumber < 4 ) {
				// paint slot's colored underlay if the slot is hovered.
				if( slot.slotNumber == hoveredRecipe )
					GuiUtils.paintSlotOverlay( slot, 22, getColorFor( slot.getSlotIndex() ) );
			}
		}

		super.drawSlotInventory( slot );
	}

	private int getColorFor(int recipeIndex) {
		int color;
		if( this.mc.thePlayer.capabilities.isCreativeMode ) {
			color = GuiUtils.COLOR_BLUE;
		} else if( crafter.isRedState( recipeIndex ) ) {
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
				if( isPointInRegion( slot.xDisplayPosition - 3, slot.yDisplayPosition - 3, 22, 22, mouseX, mouseY ) ) {
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

	private void updateGhostContents(int newIndex) {
		this.hoveredRecipe = newIndex == -1 ? -1 : newIndex % 4;
		doGhostUpdateLocally();
	}

	private void doGhostUpdateLocally() {
		ContainerCrafter container = (ContainerCrafter) this.inventorySlots;
		TileCrafter crafter = container.crafter;

		CraftRecipe recipe;
		if( hoveredRecipe == -1 ) {
			recipe = RecipeUtils.getRecipe( crafter.craftGrid.getContents(), crafter.worldObj );
		} else if( hoveredRecipe < crafter.getRecipeCount() ) {
			recipe = crafter.getRecipe( hoveredRecipe );
		} else {
			throw new IllegalStateException( "XACT-GuiCrafter: invalid hoveredRecipe index" );
		}

		gridContents = recipe == null ? new ItemStack[9] : recipe.getIngredients();
		missingIngredients = hoveredRecipe == -1 ? crafter.missingIngredients : crafter.getHandler().getMissingIngredientsArray( recipe );
	}


	///////////////
	///// InteractiveCraftingGui

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if( ingredients == null ) {
			GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) -1, null );
			return;
		}
		GuiUtils.sendItemsToServer( ClientProxy.getNetClientHandler(), ingredients, 8 );
	}

	///////////////
	///// Buttons

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
