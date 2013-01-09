package xk.xact.gui;


import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.core.CraftPad;
import xk.xact.core.ItemChip;
import xk.xact.gui.button.CustomButtons;
import xk.xact.gui.button.GuiButtonCustom;
import xk.xact.gui.button.ICustomButtonMode;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;

public class GuiPad extends GuiContainer implements InteractiveCraftingGui {

	private CraftPad craftPad;

	private boolean[] missingIngredients = new boolean[9];


	public GuiPad(CraftPad pad, Container container){
		super(container);
		this.ySize = 180;
		this.craftPad = pad;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		super.controlList.clear();
		this.button = CustomButtons.createdDeviceButton( this.guiLeft + 97, this.guiTop + 63 );
		button.id = 0;
		controlList.add( button );
		invalidated = true;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/pad_1.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the titles
		int xPos = 11 + (112 - fontRenderer.getStringWidth("Craft Pad")) / 2;
		this.fontRenderer.drawString("Craft Pad", xPos, 8, 4210752);

		xPos = 126 + (40 - fontRenderer.getStringWidth("Chip")) /2;
		this.fontRenderer.drawString("Chip", xPos, 23, 4210752);

		// Paint the grid's overlays.
		paintSlotOverlays();
	}

	@Override
	protected void drawSlotInventory(Slot slot) {
		if( GuiUtils.isShiftKeyPressed() && slot.getHasStack() ) {
			ItemStack stack = slot.getStack();
			if( CraftManager.isEncoded(stack) ) {
				CraftRecipe recipe = RecipeUtils.getRecipe(stack, this.mc.theWorld);
				if( recipe != null ) {
					GuiUtils.paintItem( recipe.getResult(), slot.xDisplayPosition, slot.yDisplayPosition, this.mc, itemRenderer );
					GuiUtils.paintGreenEffect( slot, itemRenderer );
					return;
				}
			}
		}
		super.drawSlotInventory( slot );
	}

	// title: (43,8) size: 88x12

	// button position: 97, 63. size: 14x14
		// button texture: (14*i +0,  176)

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		if( ingredients == null ) {
			GuiUtils.sendItemToServer( this.mc.getSendQueue(), (byte) -1, null);
			return;
		}
		for( int i = 0; i < ingredients.length; i ++ ) {
			GuiUtils.sendItemToServer( this.mc.getSendQueue(), (byte)(i +1), ingredients[i]);
		}
		GuiUtils.sendItemsToServer( this.mc.getSendQueue(), ingredients );
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		ContainerPad pad = (ContainerPad) this.mc.thePlayer.openContainer;
		if( pad.player.inventory.inventoryChanged || craftPad.inventoryChanged ) {
			pad.craftPad.getRecipe(0); // update the recipe.
			this.missingIngredients = pad.craftPad.getMissingIngredients();
			pad.player.inventory.inventoryChanged = false;
			craftPad.inventoryChanged = false;
		}

		if( pad.contentsChanged || invalidated ) {

			for( int i = 0; i < 4; i++ ) {
				ItemStack chip = craftPad.chipInv.getStackInSlot(0);
				if( chip == null ) {
					button.setMode( ICustomButtonMode.DeviceModes.INACTIVE );
					continue;
				}

				if( chip.getItem() instanceof ItemChip) {
					if( !((ItemChip) chip.getItem()).encoded ) {
						CraftRecipe mainRecipe = craftPad.getRecipe( 0 ); // the recipe on the grid
						if( mainRecipe != null && mainRecipe.isValid() ) {
							button.setMode( ICustomButtonMode.DeviceModes.SAVE );
							continue;
						}
						button.setMode( ICustomButtonMode.DeviceModes.INACTIVE );
						continue;
					}
					button.setMode( ICustomButtonMode.DeviceModes.CLEAR );
				}
			}
			invalidated = false;
			pad.contentsChanged = false;
		}

	}

	@Override
	protected void keyTyped(char par1, int key) {
		if( key == Keyboard.KEY_DOWN ) {
			GuiUtils.sendItemsToServer( this.mc.getSendQueue(), null );
			return;
		}
		if( key == Keyboard.KEY_DELETE ) {

		}
		super.keyTyped(par1, key);
	}

	private void paintSlotOverlays() {

		// Items overlay: (alpha 50%)
			// normal = gray
			// missing = red

		int transparency = 128 << 24;

		int gray =  transparency | GuiUtils.COLOR_GRAY;
		int red = transparency | GuiUtils.COLOR_RED;

		for( int index = 1; index <= 9; index++ ) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( index );
			if( slot == null )
				continue;

			int color = missingIngredients[index-1] ? red : gray;

			GuiUtils.paintSlotOverlay(slot, 16, color);
		}

		// todo: paint the overlay on the output slot.

	}

	///////////////
	///// Buttons

	private GuiButtonCustom button;

	private boolean invalidated = true;

	@Override
	protected void actionPerformed(GuiButton button) {
		if( button instanceof GuiButtonCustom ) {
			int action = ((GuiButtonCustom) button).getAction();

			if( action == 1 ) { // SAVE
				ItemStack stack = CraftManager.encodeRecipe( craftPad.getRecipe(0) );
				GuiUtils.sendItemToServer( this.mc.getSendQueue(), (byte)(button.id +10), stack);
				return;
			}
			if( action == 3 ) { // CLEAR
				GuiUtils.sendItemToServer(this.mc.getSendQueue(), (byte)(button.id +10), new ItemStack(XActMod.itemRecipeBlank));
			}
		}
	}

}
