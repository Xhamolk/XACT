package xk.xact.network;



import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import xk.xact.TileCrafter;
import xk.xact.TileEncoder;
import xk.xact.TileMachine;
import xk.xact.event.CraftEvent;
import xk.xact.event.EncodeEvent;
import xk.xact.recipes.CraftRecipe;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player packetSender) {

		// Encoder stuff
		if (packet.channel.equals("xact_chan_enc")) {
			try{
				EntityPlayer player = (EntityPlayer)packetSender;
				int[] coords = getCoordinates(packet.data);

				TileMachine machine = getTileAt(player.worldObj, coords[0], coords[1], coords[2]);
				if( machine != null ) {
					TileEncoder encoder = (TileEncoder) machine;
					CraftRecipe recipe = encoder.getCurrentRecipe();
					EncodeEvent event = new EncodeEvent(player, recipe);
					encoder.handleEvent(event);
					encoder.currentMode = TileEncoder.Mode.SUCCESS;
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
			return new int[] {x, y, z};
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


}

