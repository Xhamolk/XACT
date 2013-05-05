package xk.xact.client.gui;

import invtweaks.api.ContainerGUI;
import invtweaks.api.ContainerSection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.client.GuiUtils;
import xk.xact.client.button.CustomButtons;
import xk.xact.client.button.GuiButtonCustom;
import xk.xact.client.button.ICustomButtonMode;
import xk.xact.core.CraftPad;
import xk.xact.core.items.ItemChip;
import xk.xact.network.ClientProxy;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.util.Textures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContainerGUI
public class GuiPad extends CraftingGui {

	private CraftPad craftPad;

	private boolean[] missingIngredients = new boolean[9];

	public GuiPad(CraftPad pad, Container container) {
		super( container );
		this.ySize = 180;
		this.craftPad = pad;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		super.buttonList.clear();
		this.button = CustomButtons.createdDeviceButton( this.guiLeft + 97, this.guiTop + 63 );
		button.id = 0;
		buttonList.add( button );
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		this.mc.renderEngine.bindTexture( Textures.GUI_PAD );
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect( cornerX, cornerY, 0, 0, this.xSize,
				this.ySize );
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the titles
		int xPos = 11 + (112 - fontRenderer.getStringWidth( "Craft Pad" )) / 2;
		this.fontRenderer.drawString( "Craft Pad", xPos, 8, 4210752 );

		xPos = 126 + (40 - fontRenderer.getStringWidth( "Chip" )) / 2;
		this.fontRenderer.drawString( "Chip", xPos, 23, 4210752 );

		// Paint the grid's overlays.
		paintSlotOverlays();
	}

	// title: (43,8) size: 88x12

	// button position: 97, 63. size: 14x14
	// button texture: (14*i +0, 176)

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if( ingredients == null ) {
			GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) -1, null );
			return;
		}
		GuiUtils.sendItemsToServer( ClientProxy.getNetClientHandler(), ingredients, 1 );
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if( craftPad.recentlyUpdated ) {
			// Update the missing ingredients
			missingIngredients = craftPad.getMissingIngredients();

			// Update the buttons for the chips
			ItemStack chip = craftPad.chipInv.getStackInSlot( 0 );
			if( chip == null ) {
				button.setMode( ICustomButtonMode.DeviceModes.INACTIVE );

			} else if( chip.getItem() instanceof ItemChip ) {
				if( !((ItemChip) chip.getItem()).encoded ) {
					CraftRecipe mainRecipe = craftPad.getRecipe( 0 ); // the recipe on the grid
					if( mainRecipe != null && mainRecipe.isValid() ) {
						button.setMode( ICustomButtonMode.DeviceModes.SAVE );

					} else {
						button.setMode( ICustomButtonMode.DeviceModes.INACTIVE );
					}
				} else {
					button.setMode( ICustomButtonMode.DeviceModes.CLEAR );
				}
			}
			craftPad.recentlyUpdated = false;
		}

	}

	private void paintSlotOverlays() {

		// Items overlay: (alpha 50%)
		// normal = gray
		// missing = red

		int transparency = 128 << 24;

		int gray = transparency | GuiUtils.COLOR_GRAY;
		int red = transparency | GuiUtils.COLOR_RED;

		for( int index = 1; index <= 9; index++ ) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( index );
			if( slot == null )
				continue;

			int color = missingIngredients[index - 1] ? red : gray;

			GuiUtils.paintSlotOverlay( slot, 16, color );
		}

		// todo: paint the overlay on the output slot.

	}

	///////////////
	///// Buttons

	private GuiButtonCustom button;

	@Override
	protected void actionPerformed(GuiButton button) {
		if( button instanceof GuiButtonCustom ) {
			int action = ((GuiButtonCustom) button).getAction();

			if( action == 1 ) { // SAVE
				ItemStack stack = CraftManager.encodeRecipe( craftPad.getRecipe( 0 ) );
				GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) (button.id + 10), stack );
				return;
			}
			if( action == 3 ) { // CLEAR
				GuiUtils.sendItemToServer( ClientProxy.getNetClientHandler(), (byte) (button.id + 10), new ItemStack( XActMod.itemRecipeBlank ) );
			}
		}
	}

	// Compatibility with Inventory Tweaks.
	@ContainerGUI.ContainerSectionCallback
	@SuppressWarnings({ "unchecked", "unused" })
	public Map<ContainerSection, List<Slot>> getContainerSections() {
		Map<ContainerSection, List<Slot>> map = new HashMap<ContainerSection, List<Slot>>();
		int i = 0;
		List<Slot> slots = inventorySlots.inventorySlots;

		map.put( ContainerSection.CRAFTING_OUT, slots.subList( i, i += 1 ) ); // output slot
		map.put( ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList( i, i += 9 ) ); // crafting grid.
		map.put( ContainerSection.CHEST, slots.subList( i, i += 1 ) ); // chip slot
		return map;
	}

}
