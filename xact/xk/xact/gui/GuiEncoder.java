package xk.xact.gui;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet250CustomPayload;
import org.lwjgl.opengl.GL11;
import xk.xact.TileEncoder;
import xk.xact.event.EncodeEvent;

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
		this.fontRenderer.drawString("XACT Encoder", 50, 6, 4210752);
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
		
		// Draw the current recipe.
		if( encoder.currentRecipe != null ){
			// 16x16 slot -> (98, 21);
			// 18x18 slot -> (97, 20);
			ghostRenderer.renderStackOnGUI(encoder.currentRecipe, cornerX+ 98, cornerY+ 21);
		}

	}

	@Override
	protected void mouseClicked(int x, int y, int par3) {
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;

		
		if( cornerX+ 134 <= x && x < cornerX+ 148 ) {
			if( cornerY+ 19 <= y && y < cornerY+ 33 ) {
				if( encoder.currentMode == TileEncoder.Mode.NONE || encoder.currentMode == TileEncoder.Mode.SUCCESS )
					return; // do nothing.

				fireEvent();
				return;
			}
		}
		super.mouseClicked(x, y, par3);
	}

	// used by drawGuiContainerBackgroundLayer to get the button texture.
	private int getButtonIndex() {
		switch (encoder.currentMode) {
			case READY:
				return 0;
			case SUCCESS:
				return 1;
			case CLEAR:
				return 2;
		}
		return -1;
	}

	private void fireEvent() {
		// Client Side:
        EncodeEvent event = new EncodeEvent(mc.thePlayer, encoder.getCurrentRecipe());
		encoder.handleEvent(event);
		this.inventorySlots.getSlotFromInventory(encoder.circuitInv, 0).onSlotChanged();
        encoder.currentMode = TileEncoder.Mode.SUCCESS;


        // Send the packet to the server.
		int x = encoder.xCoord;
		int y = encoder.yCoord;
		int z = encoder.zCoord;

		ByteArrayOutputStream bos = new ByteArrayOutputStream(12);
		DataOutputStream outputStream = new DataOutputStream(bos);
		try {
			outputStream.writeInt(x);
			outputStream.writeInt(y);
			outputStream.writeInt(z);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "xact_chan_enc";
		packet.data = bos.toByteArray();
		packet.length = bos.size();


		this.mc.thePlayer.sendQueue.addToSendQueue(packet);
	}


}
