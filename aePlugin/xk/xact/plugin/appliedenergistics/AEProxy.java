package xk.xact.plugin.appliedenergistics;


import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridTileEntity;
import net.minecraftforge.common.MinecraftForge;
import xk.xact.util.Utils;

/**
 * @author Xhamolk_
 */
public class AEProxy implements xk.xact.plugin.AEProxy {

	public void fireTileLoadEvent(IGridTileEntity tile) {
		try {
			MinecraftForge.EVENT_BUS.post( new GridTileLoadEvent( tile, tile.getWorld(), tile.getLocation() ) );
		} catch( Exception e ) {
			Utils.logException( "Unable to fire GridTileLoadEvent. Caused by: " + e.getMessage(), e, false );
		}
	}

	public void fireTileUnloadEvent(IGridTileEntity tile) {
		try {
			MinecraftForge.EVENT_BUS.post( new GridTileUnloadEvent( tile, tile.getWorld(), tile.getLocation() ) );
		} catch( Exception e ) {
			Utils.logException( "Unable to fire GridTileUnloadEvent. Caused by: " + e.getMessage(), e, false );
		}
	}

}
