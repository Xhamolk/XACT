package xk.xact.util;


import net.minecraft.entity.player.EntityPlayer;

public class Utils {

	public static void notifyPlayer(EntityPlayer player, String message) {
		if( player.worldObj.isRemote ) {
			player.sendChatToPlayer( message );
		}
	}
}
