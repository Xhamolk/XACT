package xk.xact.gui;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.core.CraftPad;
import xk.xact.recipes.CraftManager;
import xk.xact.recipes.CraftRecipe;
import xk.xact.recipes.RecipeUtils;
import xk.xact.util.CustomPacket;

import java.io.IOException;

public class GuiPad extends GuiContainer implements InteractiveCraftingGui {

	private CraftPad craftPad;

	public boolean updateScheduled = true;

	private boolean[] missingIngredients = new boolean[9];


	public GuiPad(CraftPad pad, Container container){
		super(container);
		this.ySize = 180;
		this.craftPad = pad;
	}


	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/pad.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		// draw the button
		if( craftPad.buttonID != -1 )
			this.drawTexturedModalRect(cornerX+97, cornerY+63, 	176, craftPad.buttonID*14, 	14, 14);
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
	protected void mouseClicked(int x, int y, int mouseButton) {
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;

		if( cornerX+ 97 <= x && x < cornerX+ 97+14 ) {
			if( cornerY+ 63 <= y && y < cornerY+ 63+14 ) {
				this.mc.thePlayer.sendQueue.addToSendQueue(createPacket());
				return;
			}
		}
		super.mouseClicked(x, y, mouseButton);
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

	private Packet250CustomPayload createPacket() {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "xact_channel";
		packet.data = new byte[] { 0x02, (byte) craftPad.buttonID, (byte) this.mc.thePlayer.inventory.currentItem};
		packet.length = packet.data.length;

		return packet;
	}

	// title: (43,8) size: 88x12

	// button position: 97, 63. size: 14x14
		// button texture: (14*i +0,  176)


	@Override
	public void handleMouseClick(Slot slot, int par2, int par3, int flag) {
		if( slot != null && slot.getHasStack() ){
			ItemStack stackInSlot = slot.getStack();
			if( stackInSlot.itemID == XActMod.itemCraftPad.shiftedIndex && stackInSlot.getItemDamage() == 1 ){
				this.mc.thePlayer.sendChatToPlayer("Can't move the Craft Pad while it's in use.");
				return;
			}
		}
		super.handleMouseClick(slot, par2, par3, flag);
	}

	@Override
	public void sendGridIngredients(ItemStack[] ingredients) {
		NetClientHandler sendQueue = this.mc.getSendQueue();
		if( sendQueue == null )
			return;

		for( int index = 0; index<ingredients.length; index++ ) {

			ItemStack stack = ingredients[index];
			byte slotID = (byte) (index +1);

			try {
				Packet250CustomPayload packet = new CustomPacket((byte)0x03).add(slotID, stack).toPacket();
				sendQueue.addToSendQueue(packet);
			}catch(IOException ioe) {
				FMLCommonHandler.instance().raiseException(ioe, "GuiPad: custom packet", true);
			}
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if( updateScheduled ) {
			missingIngredients = craftPad.getMissingIngredients();
			updateScheduled = false;
		}
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

}
