package xk.xact.client.gui;

import invtweaks.api.ContainerGUI;
import invtweaks.api.ContainerSection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xk.xact.XActMod;
import xk.xact.client.GuiUtils;
import xk.xact.client.button.CustomButtons;
import xk.xact.client.button.GuiButtonCustom;
import xk.xact.client.button.ICustomButtonMode;
import xk.xact.client.gui.hooks.DynamicSlotPaintHook;
import xk.xact.core.items.ItemChip;
import xk.xact.core.tileentities.TileCrafter;
import xk.xact.gui.ContainerCrafter;
import xk.xact.network.ClientProxy;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.Textures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContainerGUI
public class GuiCrafter extends GuiCrafting implements DynamicSlotPaintHook {

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
	protected String getBaseTexture() {
		return Textures.GUI_CRAFTER;
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

	// -------------------- Compatibility with Inventory Tweaks --------------------

	@ContainerGUI.ContainerSectionCallback
	@SuppressWarnings({ "unchecked", "unused" })
	public Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		List<Slot> slots = inventorySlots.inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, getSlots( 0, 1, 2, 3, 17 ) ); // output slots
		map.put( ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList( 4, 17 ) ); // crafting grid and chips.
		map.put( ContainerSection.CHEST, slots.subList( 18, 18 + 27 ) ); // the resources buffer
		return map;
	}

	private List<Slot> getSlots(int... indexes) {
		List<Slot> slots = new ArrayList<Slot>();
		for( int index : indexes ) {
			slots.add( inventorySlots.getSlot( index ) );
		}
		return slots;
	}

	// -------------------- DynamicSlotPaintHook --------------------

	@Override
	public boolean shouldDelegateSuperCall(Slot slot) {
		if( 8 <= slot.slotNumber && slot.slotNumber < 18 - 1 ) { // grid slots.
			if( hoveredRecipe != -1 ) // if hovering a recipe, then handle the painting here.
				return false;
		}
		return true;
	}

	public int getSlotUnderlayColor(Slot slot, boolean mouseOver) {
		if( slot.getHasStack() && slot.slotNumber < 4 ) { // output slots.
			int color;
			if( this.mc.thePlayer.capabilities.isCreativeMode ) {
				color = GuiUtils.COLOR_BLUE;
			} else if( crafter.isRedState( slot.getSlotIndex() ) ) {
				color = GuiUtils.COLOR_RED;
			} else {
				color = GuiUtils.COLOR_GREEN;
			}
			color |= TRANSPARENCY; // transparency layer.
			return color;
		}
		return -1;
	}

	@Override
	public int getSlotOverlayColor(Slot slot, boolean mouseOver) {
		if( 8 <= slot.slotNumber && slot.slotNumber < 18 - 1 ) { // grid slots.
			ItemStack itemInSlot = slot.getStack();
			if( itemInSlot != null && itemInSlot.stackSize > 0 ) {
				return -1; // no overlay when the slot contains "real" items.
			}

			int index = slot.slotNumber - 8;
			int color = hoveredRecipe == -1 ?
					(crafter.missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY) :
					(missingIngredients[index] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY);
			return color | TRANSPARENCY;
		}
		return -1;
	}

	@Override
	public ItemStack getStackToPaintInSlot(Slot slot, boolean mouseOver) {
		if( 8 <= slot.slotNumber && slot.slotNumber < 18 - 1 ) { // grid slots.
			if( hoveredRecipe != -1 ) {
				// Only paint the grid's real contents if there is no recipe being hovered.
				// if a recipe is being hovered, paint those ingredients instead.
				return gridContents[slot.slotNumber -8];
			}
		}
		return null;
	}

	@Override
	protected int getSlotDimensions(Slot slot) {
		return slot.slotNumber < 4 ? 22 : 16; // the output slots are a bit bigger.
	}

}
