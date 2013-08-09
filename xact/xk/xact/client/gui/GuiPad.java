package xk.xact.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

public class GuiPad extends GuiCrafting {

	private static final ResourceLocation guiTexture = new ResourceLocation( Textures.GUI_PAD );

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
	protected ResourceLocation getBaseTexture() {
		return guiTexture;
	}

	@Override
	protected void drawTitle() {
		int xPos = 11 + (112 - fontRenderer.getStringWidth( "Craft Pad" )) / 2;
		this.fontRenderer.drawString( "Craft Pad", xPos, 8, 4210752 );

		xPos = 126 + (40 - fontRenderer.getStringWidth( "Chip" )) / 2;
		this.fontRenderer.drawString( "Chip", xPos, 23, 4210752 );
	}

	@Override
    protected void drawSlotInventory(Slot slot) {
        super.drawSlotInventory( slot );

        int slotIndex = slot.slotNumber;
        if( 0 < slotIndex && slotIndex <= 9 ) { // grid slots
            int color = missingIngredients[slotIndex - 1] ? GuiUtils.COLOR_RED : GuiUtils.COLOR_GRAY;
            color |= 128 << 24; // transparency

            GuiUtils.paintSlotOverlay( slot, 16, color );
        }
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

}
