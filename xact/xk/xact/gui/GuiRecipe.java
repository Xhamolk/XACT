package xk.xact.gui;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.util.CustomPacket;

import java.io.IOException;

// GUI used to set the recipe of a node.
public class GuiRecipe extends GuiContainer implements InteractiveCraftingGui  {

	// todo: remember which was the last
	public GuiRecipe(Container container) {
		super(container);
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
//		if( device.buttonID != -1 )
//			this.drawTexturedModalRect(cornerX+117, cornerY+63, 	176, device.buttonID*14, 	14, 14);

		// todo: paint the slot's overlays.
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the title
		int xPos = (this.xSize - fontRenderer.getStringWidth("Recipe Chip")) / 2;
		this.fontRenderer.drawString("Recipe Chip", xPos, 8, 4210752);
	}

	// todo: when pressing "escape", return to the blueprint's GUI.

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
		// packet.data = new byte[] { 0x01, (byte) device.buttonID, (byte) this.mc.thePlayer.inventory.currentItem};
		packet.length = packet.data.length;

		return packet;
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
				FMLCommonHandler.instance().raiseException(ioe, "GuiRecipe: custom packet", true);
			}
		}
	}
}
