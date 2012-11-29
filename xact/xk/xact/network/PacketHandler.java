package xk.xact.network;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import xk.xact.core.TileEncoder;
import xk.xact.core.TileMachine;
import xk.xact.gui.ContainerChip;
import xk.xact.gui.ContainerPad;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		if( packet.channel.equals("xact_channel") ){
			// Chip doing it's stuff.
			if( packet.data[0] == 0x01 ) {
				int buttonID = packet.data[1];

				ContainerChip chipContainer = (ContainerChip) ((EntityPlayer)packetSender).openContainer;
				chipContainer.handlePacket(buttonID);
				return;
			}

			// CraftPad button click
			if( packet.data[0] == 0x02 ) {
				int buttonID = packet.data[1];

				ContainerPad padContainer = (ContainerPad) ((EntityPlayer)packetSender).openContainer;
				padContainer.handlePacket(buttonID);
			}
		}

		// Encoder stuff (unused)
		if (packet.channel.equals("xact_chan_enc")) {
			try{
				EntityPlayer player = (EntityPlayer)packetSender;
				int[] coords = getCoordinates(packet.data);
				byte nextMode = packet.data[12];

				TileMachine machine = getTileAt(player.worldObj, coords[0], coords[1], coords[2]);
				if( machine != null ) {
					TileEncoder encoder = (TileEncoder) machine;
					encoder.mode = nextMode;
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private static TileMachine getTileAt(World world, int x, int y, int z){
		try{
			return (TileMachine) world.getBlockTileEntity(x, y, z);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int[] getCoordinates(byte[] packetData) {
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packetData));
		try {
			int x = inputStream.readInt();
			int y = inputStream.readInt();
			int z = inputStream.readInt();
			inputStream.close();

			return new int[] {x, y, z};
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


}

