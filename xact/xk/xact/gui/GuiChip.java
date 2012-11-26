package xk.xact.gui;


import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Packet250CustomPayload;
import org.lwjgl.opengl.GL11;
import xk.xact.core.ChipDevice;

public class GuiChip extends GuiContainer {


	private ChipDevice device;

	public GuiChip(ChipDevice device, Container container){
		super(container);
		this.ySize = 180;
		this.device = device;
		this.device.updateContents();
	}


	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/chip.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		// draw the button
		if( device.buttonID != -1 )
			this.drawTexturedModalRect(cornerX+117, cornerY+63, 	176, device.buttonID*14, 	14, 14);

		// draw the text box?

		// todo: paint a red overlay if can't craft the items, or something like that.
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the title
		this.fontRenderer.drawString("Recipe Chip", 56, 6, 4210752);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) {
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;

		if( cornerX+ 117 <= x && x < cornerX+ 117+14 ) {
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
		packet.data = new byte[] { 0x01, (byte) device.buttonID, (byte) this.mc.thePlayer.inventory.currentItem};
		packet.length = packet.data.length;

		return packet;
	}

	// title: (43,8) size: 88x12

	// button position: 117, 63. size: 14x14
		// button texture: (14*i +0,  176)


}
