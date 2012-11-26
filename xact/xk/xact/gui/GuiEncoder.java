package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet250CustomPayload;
import org.lwjgl.opengl.GL11;
import xk.xact.core.TileEncoder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class GuiEncoder extends GuiMachine {

	protected TileEncoder encoder;
	protected RenderGhostItem ghostRenderer;

	public GuiEncoder(TileEncoder encoder, EntityPlayer player){
		super(new ContainerEncoder(encoder, player));
		this.encoder = encoder;
		this.ghostRenderer = new RenderGhostItem();
	}

	@Override
	public void onInit() {
		ghostRenderer.setTransparency(0.85f);
		encoder.updateRecipe();
	}

	private boolean configured = false;

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString("X.A.C.T. Encoder", 50, 6, 4210752);
		this.fontRenderer.drawString("Player's Inventory", 8, this.ySize - 94, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

		//  Draw GUI
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/encoder.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		if( !configured ) {
			ghostRenderer.configure(this.fontRenderer, this.mc.renderEngine);
			configured = true;
		}

		// Draw the button
		int buttonIndex = getButtonIndex();
		if( buttonIndex != -1 ) { // Paint the mode button.
			// paint at (133,19), from (176, 14*buttonIndex), size is (14, 14).
			this.drawTexturedModalRect(cornerX+ 133, cornerY+ 19, 176, 14*buttonIndex, 14, 14);
		}

	}

	@Override
	protected void mouseClicked(int x, int y, int par3) {
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;

		
		if( cornerX+ 134 <= x && x < cornerX+ 148 ) {
			if( cornerY+ 19 <= y && y < cornerY+ 33 ) {
				fireEvent();
				return;
			}
		}
		super.mouseClicked(x, y, par3);
	}

	// used by drawGuiContainerBackgroundLayer to get the button texture.
	private int getButtonIndex() {
		switch (encoder.mode) {
			case TileEncoder.MODE_ENCODE:
				return 0;
			case TileEncoder.MODE_CLEAR:
				return 2;
		}
		return -1;
	}

	private void fireEvent() {
		byte nextMode = -1;
		switch (encoder.mode) {
			case TileEncoder.MODE_ENCODE:
				nextMode = TileEncoder.MODE_CLEAR;
				break;
			case TileEncoder.MODE_CLEAR:
				nextMode = TileEncoder.MODE_ENCODE;
				break;
		}

		if( nextMode == -1 )
			return;

		encoder.mode = nextMode;
		this.mc.thePlayer.sendQueue.addToSendQueue(newPacket(nextMode));
	}

	private Packet250CustomPayload newPacket(byte nextMode) {

		// Send the packet to the server.
		int x = encoder.xCoord;
		int y = encoder.yCoord;
		int z = encoder.zCoord;

		ByteArrayOutputStream bos = new ByteArrayOutputStream(13); // 3 integers(12) + 1 byte.
		DataOutputStream outputStream = new DataOutputStream(bos);
		try {
			outputStream.writeInt(x);
			outputStream.writeInt(y);
			outputStream.writeInt(z);
			outputStream.writeByte(nextMode);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "xact_chan_enc";
		packet.data = bos.toByteArray();
		packet.length = bos.size();

		return packet;
	}

}
