package xk.xact.network;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import xk.xact.gui.ContainerPad;


public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		// 0x01: chip (removed)
		// 0x02: craft pad

		if( packet.channel.equals("xact_channel") ) {

			// CraftPad button click
			if( packet.data[0] == 0x02 ) {
				int buttonID = packet.data[1];

				ContainerPad padContainer = (ContainerPad) ((EntityPlayer)packetSender).openContainer;
				padContainer.handlePacket(buttonID);
			}
		}

	}

}

