package xk.xact.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.*;
import xk.xact.XActMod;
import xk.xact.api.InteractiveCraftingContainer;
import xk.xact.gui.ContainerPad;
import xk.xact.gui.GuiRecipe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		// 0x01: chip (removed)
		// 0x02: craft pad
		// 0x03: GuiPad/GuiRecipe sending an ItemStack
		// 0x04: GuiPlan: requesting a recipe.
		// 0x05: ContainerRecipe notifying the client, the recipe has changed.

		byte action = -1;
		if( packet.channel.equals("xact_channel") ) {
			try {
				DataInputStream packetData = new DataInputStream(new ByteArrayInputStream( packet.data ));
				action = packetData.readByte();

				// CraftPad button click (server side)
				if( action == 0x02 ) {
					byte buttonID = packetData.readByte();

					ContainerPad padContainer = (ContainerPad) ((EntityPlayer)packetSender).openContainer;
					padContainer.buttonClicked(buttonID);
					return;
				}

				// GuiPad/GuiRecipe sending an ItemStack (server side)
				if( action == 0x03 ) {
					int slotID = packetData.readByte();
					InteractiveCraftingContainer container = (InteractiveCraftingContainer) ((EntityPlayer)packetSender).openContainer;

					// special command used to clear the grid.
					if( slotID == -1 ) {
						for( int i = 1; i <= 9; i++ ) {
							container.setStack(i, null);
						}
						return;
					}

					// place a recipe on the slot.
					ItemStack stack = getItemStack(packetData);
					container.setStack(slotID, stack);
					return;
				}

				// GuiPlan requesting a recipe (server side)
				if ( action == 0x04 ) {
					// must open the recipe gui.
					EntityPlayer player = (EntityPlayer) packetSender;
					player.openGui(XActMod.instance, 5, player.worldObj, 0, 0, 0);
					return;
				}

				// ContainerRecipe notifying GuiRecipe that the recipe has changed (client side)
				if ( action == 0x05 ) {
					GuiScreen screen = CommonProxy.getCurrentScreen();
					if( screen instanceof GuiRecipe ) {
						GuiRecipe gui = (GuiRecipe) screen;
						gui.buttonID = packetData.readByte(); // todo: recheck this.
					}
					return;
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

