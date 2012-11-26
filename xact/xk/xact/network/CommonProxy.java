package xk.xact.network;


import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import xk.xact.core.*;
import xk.xact.gui.*;

public class CommonProxy implements IGuiHandler {

	public void registerRenderInformation() { }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// ID:
			// 0: crafter
			// 1: library
			// 2: chip

		if( ID == 1 ){
			ChipCase chipCase = new ChipCase(player.inventory.getCurrentItem());
			return new ContainerCase(chipCase, player);
		}

		if( ID == 2 ){
			ChipDevice chipDevice = new ChipDevice(player.inventory.getCurrentItem(), player);
			return new ContainerChip(chipDevice, player);
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

		if( ID == 1 ){
			ChipCase chipCase = new ChipCase(player.inventory.getCurrentItem());
			return new GuiCase(new ContainerCase(chipCase, player));
		}

		if( ID == 2 ) {
			ChipDevice chipDevice = new ChipDevice(player.inventory.getCurrentItem(), player);
			return new GuiChip(chipDevice, new ContainerChip(chipDevice, player));
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
