package xk.xact.gui;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import xk.xact.api.InteractiveCraftingGui;
import xk.xact.util.CustomPacket;
import xk.xact.util.InventoryUtils;

import java.io.IOException;

// GUI used to set the recipe of a node.
public class GuiRecipe extends GuiContainer implements InteractiveCraftingGui  {

	private EntityPlayer player;

	public GuiRecipe(EntityPlayer player, Container container) {
		super(container);
		this.player = player;
	}

	private ItemStack target = null;

	public void setTarget(ItemStack target) {
		this.target = target;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int texture = this.mc.renderEngine.getTexture("/gfx/xact/gui/recipe.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(cornerX, cornerY, 0, 0, this.xSize, this.ySize);

		if( matching = matchingTarget() ) {
			// draw the "success" button
			this.drawTexturedModalRect(cornerX+117, cornerY+63, 	176, 0, 	14, 14);
		} else {
			// Paint the target.
			paintTarget();
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		// the title
		int xPos = (this.xSize - fontRenderer.getStringWidth("Choose the Recipe")) / 2;
		this.fontRenderer.drawString("Choose the Recipe", xPos, 8, 4210752);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) {
		int cornerX = (this.width - this.xSize) / 2;
		int cornerY = (this.height - this.ySize) / 2;

		if( matching && cornerX+ 117 <= x && x < cornerX+ 117+14 ) {
			if( cornerY+ 63 <= y && y < cornerY+ 63+14 ) {
				this.mc.thePlayer.sendQueue.addToSendQueue( createPacket(1) );
				return;
			}
		}
		super.mouseClicked(x, y, mouseButton);
	}

	protected void keyTyped(char par1, int par2) {
		if( par2 == 1 ) {
			this.mc.thePlayer.sendQueue.addToSendQueue( createPacket(0) );
			return;
		}

		super.keyTyped(par1, par2);
	}


	private boolean matching = false;

	private boolean matchingTarget() {
		Slot outputSlot = player.openContainer.getSlot(0);
		if( target == null ) {
			return outputSlot.getHasStack();
		}
		return outputSlot.getHasStack() && InventoryUtils.similarStacks( outputSlot.getStack(), target );
	}

	private void paintTarget() {
		Slot slot = player.openContainer.getSlot(0);

		if( !matching && target != null ) {
			int x = slot.xDisplayPosition, y = slot.yDisplayPosition;

			this.zLevel = 100.0F;
			itemRenderer.zLevel = 100.0F;
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.renderEngine, target, x, y);
			itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.renderEngine, target, x, y);
			itemRenderer.zLevel = 0.0F;
			this.zLevel = 0.0F;
		}

		int color = 165 << 24 | GuiUtils.COLOR_GRAY;
		GuiUtils.paintSlotOverlay(slot, 16, color);
	}

	private Packet250CustomPayload createPacket(int instruction) {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "xact_channel";
		packet.data = new byte[] { 0x05, (byte) instruction, (byte) this.mc.thePlayer.inventory.currentItem};
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
