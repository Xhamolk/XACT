package xk.xact.client;


import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xk.xact.network.ClientProxy;
import xk.xact.util.CustomPacket;
import xk.xact.util.Utils;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiUtils {

	private static int grayTone = 139;

	public static final int COLOR_RED = 180 << 16;
	public static final int COLOR_GREEN = 180 << 8;
	public static final int COLOR_BLUE = 220;
	public static final int COLOR_GRAY = (grayTone << 16) | (grayTone << 8) | grayTone;

	public static final RenderItem itemRender = new RenderItem();

	public static void paintSlotOverlay(Slot slot, int size, int color) {
		if( slot == null )
			return;

		int off = (size - 16) / 2;
		int minX = slot.xDisplayPosition - off;
		int minY = slot.yDisplayPosition - off;

		paintOverlay( minX, minY, size, color );
	}

	public static void paintIcon(Gui gui, Icon icon, int x, int y) {
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
		gui.drawTexturedModelRectFromIcon( x, y, icon, 16, 16 );
	}

	@SideOnly(Side.CLIENT)
	public static void paintOverlay(int x, int y, int size, int color) {
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		Gui.drawRect( x, y, x + size, y + size, color );
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
	}

	public static void paintItem(ItemStack itemStack, int x, int y, Minecraft mc, RenderItem itemRenderer) {
		if( itemStack == null )
			return; // I might want to have a "null" image, like background image.

		itemRenderer.zLevel = 100.0F;
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		itemRenderer.renderItemAndEffectIntoGUI( mc.fontRenderer, mc.renderEngine, itemStack, x, y );
		itemRenderer.renderItemOverlayIntoGUI( mc.fontRenderer, mc.renderEngine, itemStack, x, y );
		itemRenderer.zLevel = 0.0F;
	}

	private static final ResourceLocation GLINT = new ResourceLocation( "textures/misc/enchanted_item_glint.png" );

	public static void paintEffectOverlay(int x, int y,RenderItem itemRenderer, float red, float green, float blue, float alpha) {
		GL11.glDepthFunc( GL11.GL_GREATER );
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDepthMask( false );
		bindTexture( GLINT ); // do I want to change this to something else?

		itemRenderer.zLevel -= 50.0F;
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_DST_COLOR, GL11.GL_DST_COLOR );
		GL11.glColor4f( red, green, blue, alpha );
		effect( itemRenderer.zLevel, x - 1, y - 1, 18, 18 );

		GL11.glDisable( GL11.GL_BLEND );
		GL11.glDepthMask( true );
		itemRenderer.zLevel += 50.0F;
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glDepthFunc( GL11.GL_LEQUAL );
	}

	private static void effect(float zLevel, int x, int y, int width, int height) {

		GL11.glBlendFunc( GL11.GL_SRC_COLOR, GL11.GL_ONE );

		for( int i = 0; i < 2; i++ ) {
			float var7 = 0.00390625F;
			float var8 = 0.00390625F;
			float var9 = (float) (Minecraft.getSystemTime() % (long) (3000 + i * 1873)) / (3000.0F + (float) (i * 1873)) * 256.0F;
			float var10 = 0.0F;
			Tessellator var11 = Tessellator.instance;
			float var12 = 4.0F;

			if( i == 1 )
				var12 = -1.0F;

			var11.startDrawingQuads();
			var11.addVertexWithUV( (double) x, (double) (y + height), (double) zLevel, (double) ((var9 + (float) height * var12) * var7), (double) ((var10 + (float) height) * var8) );
			var11.addVertexWithUV( (double) (x + width), (double) (y + height), (double) zLevel, (double) ((var9 + (float) width + (float) height * var12) * var7), (double) ((var10 + (float) height) * var8) );
			var11.addVertexWithUV( (double) (x + width), (double) y, (double) zLevel, (double) ((var9 + (float) width) * var7), (double) ((var10 + 0.0F) * var8) );
			var11.addVertexWithUV( (double) x, (double) y, (double) zLevel, (double) ((var9 + 0.0F) * var7), (double) ((var10 + 0.0F) * var8) );
			var11.draw();
		}
	}

	@SideOnly(Side.CLIENT)
	public static boolean isShiftKeyPressed() {
		return Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );
	}

	@SideOnly(Side.CLIENT)
	public static boolean isRevealKeyPressed() {
		return Keyboard.isKeyDown( FMLClientHandler.instance().getClient().gameSettings.keyBindSneak.keyCode );
	}

	@SideOnly(Side.CLIENT)
	public static void sendItemToServer(NetClientHandler sendQueue, byte slotID, ItemStack item) {
		if( sendQueue == null )
			return;

		try {
			Packet250CustomPayload packet = new CustomPacket( (byte) 0x03 ).add( slotID, item ).toPacket();
			sendQueue.addToSendQueue( packet );
		} catch ( IOException ioe ) {
			Utils.logException( "ICG-Custom Packet: Sending item to server. (0x03)", ioe, false );
		}
	}

	@SideOnly(Side.CLIENT)
	public static void sendItemsToServer(NetClientHandler sendQueue, ItemStack[] items, int offset) {
		if( sendQueue == null )
			return;

		if( items == null ) {
			sendItemToServer( sendQueue, (byte) -1, null );
			return;
		}

		try {
			CustomPacket customPacket = new CustomPacket( (byte) 0x02 ).add( (byte) items.length, (byte) offset );
			for( ItemStack item : items ) {
				customPacket.add( item );
			}
			sendQueue.addToSendQueue( customPacket.toPacket() );
		} catch ( IOException ioe ) {
			Utils.logException( "ICG-Custom Packet: Sending items to server. (0x02)", ioe, false );
		}
	}

	@SideOnly(Side.CLIENT)
	public static int getMouseX(Minecraft minecraft) {
		ScaledResolution resolution = new ScaledResolution( minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight );
		int width = resolution.getScaledWidth();
		return Mouse.getX() * width / minecraft.displayWidth;
	}

	@SideOnly(Side.CLIENT)
	public static int getMouseY(Minecraft minecraft) {
		ScaledResolution resolution = new ScaledResolution( minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight );
		int height = resolution.getScaledHeight();
		return height - Mouse.getY() * height / minecraft.displayHeight - 1;
	}

	@SideOnly(Side.CLIENT)
	public static Slot getHoveredSlot(int guiLeft, int guiTop) {
		GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
		if( gui == null )
			return null;

		Container container = Minecraft.getMinecraft().thePlayer.openContainer;
		int mouseX = getMouseX( Minecraft.getMinecraft() ) - guiLeft;
		int mouseY = getMouseY( Minecraft.getMinecraft() ) - guiTop;

		return getHoveredSlot( container, mouseX, mouseY );
	}

	public static Slot getHoveredSlot(Container container, int mouseX, int mouseY) {
		Utils.debug( "Getting slot at: (%s, %s)", mouseX, mouseY );

		for( int i = 0; i < container.inventorySlots.size(); i++ ) {
			Slot slot = container.getSlot( i );
			if( slot != null ) {
				if( isMouseOverSlot( slot, mouseX, mouseY ) ) {
					return slot;
				}
			}
		}
		return null;
	}

	public static boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
		if( slot == null )
			return false;
		int xMin = slot.xDisplayPosition;
		int yMin = slot.yDisplayPosition;
		return mouseX >= xMin - 1 && mouseX < xMin + 16 + 1 && mouseY >= yMin - 1 && mouseY < yMin + 16 + 1;
	}

	@SideOnly(Side.CLIENT)
	public static World getWorld() {
		return Minecraft.getMinecraft().thePlayer.worldObj;
	}

	public static void bindTexture(String texture) {
		bindTexture( new ResourceLocation( texture ) );
	}

	public static void bindTexture(ResourceLocation resource) {
		Minecraft.getMinecraft().renderEngine.bindTexture( resource );
	}

	/**
	 * Sends a packet to the server to open the specified GUI.
	 *
	 * @param guiID the ID of the GUI. Truncated to byte.
	 * @param meta the additional information that the GUI Handler might need. Truncated to short.
	 */
	@SideOnly(Side.CLIENT)
	public static void openGui(int guiID, int meta) {
		try {
			ClientProxy.getNetClientHandler().addToSendQueue( CustomPacket.openGui( guiID, meta, 0, 0, 0 ).toPacket() );
		} catch( IOException e ) {
			Utils.logException( "Problem opening GUI (id: "+ guiID+", meta: "+meta+")", e, false );
		}
	}
}
