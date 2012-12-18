package xk.xact.network;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import xk.xact.util.CustomPacket;

import java.io.IOException;

public class EventHandler {

	@ForgeSubscribe
	public void onItemPickup(EntityItemPickupEvent event) {
		// send packet to set GuiPad.updateScheduled = true.
		try {
			Packet250CustomPayload packet = new CustomPacket((byte)0x07).toPacket();
			PacketDispatcher.sendPacketToPlayer(packet, (Player) event.entityPlayer);
		} catch (IOException e) {
			FMLCommonHandler.instance().raiseException(e, "XACT: Custom Packet, 0x07, item pickup", true);
		}
	}

}
