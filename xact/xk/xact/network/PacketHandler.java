package xk.xact.network;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.client.gui.GuiRecipe;
import xk.xact.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		// 0x01: Opening GUIs.
		// 0x02: Sending multiple ItemStacks
		// 0x03: Importing recipe from NEI: Gui sending an ItemStack to Container.

		byte action = -1;
		if( packet.channel.equals( "xact_channel" ) ) {
			try {
				DataInputStream packetData = new DataInputStream( new ByteArrayInputStream( packet.data ) );
				action = packetData.readByte();

				if( action == 0x01 ) {
					EntityPlayer player = (EntityPlayer) packetSender;
					byte guiID = packetData.readByte();
					short meta = packetData.readShort();
					int x = packetData.readInt();
					int y = packetData.readInt();
					int z = packetData.readInt();

					player.openGui( XActMod.instance, (meta << 8) | guiID, player.worldObj, x, y, z );
					return;
				}

				if( action == 0x02 ) {
					InteractiveCraftingContainer container = (InteractiveCraftingContainer) ((EntityPlayer) packetSender).openContainer;

					int stacks = packetData.readByte();
					int offSet = packetData.readByte();

					for( int i = 0; i < stacks; i++ ) {
						ItemStack item = getItemStack( packetData );
						container.setStack( offSet++, item );
					}
					return;
				}

				// Gui sending an ItemStack to Container.
				if( action == 0x03 ) {
					int slotID = packetData.readByte();
					InteractiveCraftingContainer container = (InteractiveCraftingContainer) ((EntityPlayer) packetSender).openContainer;

					// clear the grid before placing the stacks.
					if( slotID == -1 ) {
						container.setStack( -1, null );
						return;
					}

					// place a recipe on the slot.
					ItemStack stack = getItemStack( packetData );
					container.setStack( slotID, stack );
					return;
				}

				// GuiPlan requesting a recipe (server side)
				if( action == 0x04 ) {
					// must open the recipe gui.
					EntityPlayer player = (EntityPlayer) packetSender;
					player.openGui( XActMod.instance, 5, player.worldObj, 0, 0, 0 );
					return;
				}

				// ContainerRecipe notifying GuiRecipe that the recipe has changed (client side)
				if( action == 0x05 ) {
					GuiScreen screen = ClientProxy.getCurrentScreen();
					if( screen instanceof GuiRecipe ) {
						GuiRecipe gui = (GuiRecipe) screen;
						// gui.buttonID = packetData.readByte(); // todo: recheck this.
					}
					return;
				}

				Utils.logError( "Packet Unhandled: " + action );
			} catch( IOException e ) {
				Utils.logException( "Packet Handler: " + action, e, true );
			}
		}

	}


	private ItemStack getItemStack(DataInputStream packetData) {
		try {
			short itemID = packetData.readShort();
			if( itemID <= 0 )
				return null;

			byte stackSize = packetData.readByte();
			short itemDamage = packetData.readShort();

			ItemStack stack = new ItemStack( itemID, stackSize, itemDamage );
			NBTTagCompound stackNbtTag = getNBT( packetData );
			stack.setTagCompound( stackNbtTag );

			return stack;
		} catch( IOException e ) {
			return null;
		}
	}

	private NBTTagCompound getNBT(DataInputStream packetData) throws IOException {
		short nbtLength = packetData.readShort();
		if( nbtLength <= 0 )
			return null;

		byte[] byteArray = new byte[nbtLength];
		for( int i = 0; i < nbtLength; i++ ) {
			byteArray[i] = packetData.readByte();
		}

		return CompressedStreamTools.decompress( byteArray );
	}

}

