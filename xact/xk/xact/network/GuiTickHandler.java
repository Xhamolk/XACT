package xk.xact.network;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.entity.player.EntityPlayer;
import xk.xact.gui.ContainerXACT;

import java.util.EnumSet;

// Tick handler used to make my Containers able to "tick", both server and client sides.
public class GuiTickHandler implements ITickHandler {

	private GuiTickHandler() { }

	private static GuiTickHandler instance;

	public static GuiTickHandler instance() {
		if( instance == null )
			instance = new GuiTickHandler();
		return instance;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		EntityPlayer player = (EntityPlayer) tickData[0];
//		World world = (World) tickData[1];

		if( player.openContainer == player.inventoryContainer )
			return;

		if( player.openContainer instanceof ContainerXACT ) {
			((ContainerXACT) player.openContainer).onTickUpdate( player );
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of( TickType.PLAYER );
	}

	@Override
	public String getLabel() {
		return "XACT-GUI_TH";
	}
}
