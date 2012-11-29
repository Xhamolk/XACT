package xk.xact.gui;


import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import xk.xact.XActMod;
import xk.xact.core.CraftPad;

public class GuiPad extends GuiContainer {


	private CraftPad craftPad;

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

		// draw the text box?

		// todo: paint a red overlay if can't craft the items, or something like that.
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the title
		int xPos = 11 + (112 - fontRenderer.getStringWidth("Craft Pad")) / 2;
		this.fontRenderer.drawString("Craft Pad", xPos, 8, 4210752);

		xPos = 126 + (40 - fontRenderer.getStringWidth("Chip")) /2;
		this.fontRenderer.drawString("Chip", xPos, 23, 4210752);
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
	protected void handleMouseClick(Slot slot, int par2, int par3, int flag) {
		if( slot != null && slot.getHasStack() ){
			ItemStack stackInSlot = slot.getStack();
			if( stackInSlot.itemID == XActMod.itemCraftPad.shiftedIndex && stackInSlot.getItemDamage() == 1 ){
				this.mc.thePlayer.sendChatMessage("Can't move the Craft Pad while it's in use.");
				return;
			}
		}
		super.handleMouseClick(slot, par2, par3, flag);
	}

}
