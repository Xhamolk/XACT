package xk.xact.plugin;

import appeng.api.me.tiles.IGridTileEntity;

/**
 * Proxy used for AE.
 * @author Xhamolk_
 */
public interface AEProxy {

	public void fireTileLoadEvent(IGridTileEntity tile);

	public void fireTileUnloadEvent(IGridTileEntity tile);

}
