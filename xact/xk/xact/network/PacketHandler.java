package xk.xact.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.*;
import xk.xact.gui.ContainerPad;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		// 0x01: chip (removed)
		// 0x02: craft pad
		// 0x03: GuiPad sending an ItemStack

		byte action = -1;
		if( packet.channel.equals("xact_channel") ) {
			try {
				DataInputStream packetData = new DataInputStream(new ByteArrayInputStream( packet.data ));
				action = packetData.readByte();

				// CraftPad button click
				if( action == 0x02 ) {
					byte buttonID = packetData.readByte();

					ContainerPad padContainer = (ContainerPad) ((EntityPlayer)packetSender).openContainer;
					padContainer.buttonClicked(buttonID);
					return;
				}

				// GuiPad sending an ItemStack
				if( action == 0x03 ) {
					int slotID = packetData.readByte();
					ContainerPad padContainer = (ContainerPad) ((EntityPlayer)packetSender).openContainer;

					// clear the grid before placing the stacks.
					if( slotID == -1 ) {
						for( int i = 1; i <= 9; i++ ) {
							padContainer.setStack(i, null);
						}
						return;
					}

					// place a recipe on the slot.
					ItemStack stack = getItemStack(packetData);
					padContainer.setStack(slotID, stack);
				}


			} catch (IOException e) {
				FMLCommonHandler.instance().raiseException(e, "XACT Packet Handler: "+action, true);
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

			ItemStack stack = new ItemStack(itemID, stackSize, itemDamage);
			NBTTagCompound stackNbtTag = getNBT(packetData);
			stack.setTagCompound(stackNbtTag);

			return stack;
		} catch (IOException e) {
			return null;
		}
	}

	private NBTTagCompound getNBT(DataInputStream packetData) throws IOException {
		short nbtLength = packetData.readShort();
		if( nbtLength <= 0 )
			return null;

		byte[] byteArray = new byte[nbtLength];
		for( int i=0; i<nbtLength; i++ ) {
			byteArray[i] = packetData.readByte();
		}

		return CompressedStreamTools.decompress(byteArray);
	}

}

