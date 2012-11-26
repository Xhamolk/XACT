package xk.xact.network;


import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import xk.xact.core.TileCrafter;
import xk.xact.core.TileEncoder;
import xk.xact.core.TileMachine;
import xk.xact.gui.*;
import xk.xact.core.ChipDevice;

public class CommonProxy implements IGuiHandler {

	public void registerRenderInformation() { }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// ID:
			// 0: crafter
			// 1: library
			// 2: chip

		if( ID == 2 ){
			ChipDevice state = new ChipDevice(player.inventory.getCurrentItem(), player);
			return new ContainerChip(state, player);
		}

		// todo: remove the encoder.

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
		// ID:
		// 0: crafter
		// 1: library
		// 2: chip

		// todo: remove the encoder.

		if( ID == 2 ) {
			ChipDevice state = new ChipDevice(player.inventory.getCurrentItem(), player);
			return new GuiChip(state, new ContainerChip(state, player));
		}

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
