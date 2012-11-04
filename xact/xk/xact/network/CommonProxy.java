package xk.xact.network;


import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import xk.xact.TileCrafter;
import xk.xact.TileEncoder;
import xk.xact.TileMachine;
import xk.xact.gui.ContainerCrafter;
import xk.xact.gui.ContainerEncoder;
import xk.xact.gui.GuiCrafter;
import xk.xact.gui.GuiEncoder;

public class CommonProxy implements IGuiHandler {

	public void registerRenderInformation() { }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileMachine machine = (TileMachine) world.getBlockTileEntity(x, y, z);
		if( machine == null )
			return null;
		
		if( machine instanceof TileEncoder ) {
			return new ContainerEncoder((TileEncoder) machine, player);
		}
		if( machine instanceof TileCrafter ) {
			return new ContainerCrafter((TileCrafter) machine, player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileMachine machine = (TileMachine) world.getBlockTileEntity(x, y, z);
		if( machine == null )
			return null;

		if( machine instanceof TileEncoder ) {
			return new GuiEncoder((TileEncoder) machine, player);
		}
		if( machine instanceof TileCrafter ) {
			return new GuiCrafter((TileCrafter) machine, player);
		}
		return null;
	}
}
