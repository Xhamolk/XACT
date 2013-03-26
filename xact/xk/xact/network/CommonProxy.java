package xk.xact.network;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import xk.xact.core.ChipCase;
import xk.xact.core.CraftPad;
import xk.xact.core.tileentities.TileMachine;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.gui.*;

public class CommonProxy implements IGuiHandler {

	public void registerRenderInformation() { }

	public void registerKeyBindings() { }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// ID:
		// 0: machines
		// 1: library
		// 2: vanilla workbench
		// 3: craft pad
		// 4: <none> (client only)
		// 5: recipe

		if( ID == 0 ) { // Machines
			TileMachine machine = (TileMachine) world.getBlockTileEntity( x, y, z );
			if( machine == null )
				return null;

			return machine.getContainerFor( player );
		}

		if( ID == 2 ) {
			TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity( x, y, z );
			if( workbench == null )
				return null;

			return new ContainerVanillaWorkbench( workbench, player );
		}

		if( ID == 1 ) { // Chip Case
			ChipCase chipCase = new ChipCase( player.inventory.getCurrentItem() );
			return new ContainerCase( chipCase, player );
		}

		if( ID == 3 ) { // Craft Pad
			CraftPad craftPad = new CraftPad( player.inventory.getCurrentItem(), player );
			return new ContainerPad( craftPad, player );
		}

		// no ID == 4. GuiPlan, client-side only.

		if( ID == 5 ) { // Set a recipe
			return new ContainerRecipe( player );
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	private EntityPlayer fakePlayer;

	public EntityPlayer getFakePlayer(World world, int x, int y, int z) {
		if( fakePlayer == null ) {
			fakePlayer = createFakePlayer( world );
		}
		fakePlayer.worldObj = world;
		fakePlayer.posX = x;
		fakePlayer.posY = y;
		fakePlayer.posZ = z;
		return fakePlayer;
	}

	public static boolean isFakePlayer(EntityPlayer player) {
		return player.username.equals( "[XACT]" );
	}

	private EntityPlayer createFakePlayer(World world) {
		EntityPlayer player = new EntityPlayer( world ) {

			@Override
			public void sendChatToPlayer(String var1) {
			}

			@Override
			public boolean canCommandSenderUseCommand(int var1, String var2) {
				return false;
			}

			@Override
			public ChunkCoordinates getPlayerCoordinates() {
				return null;
			}

		};
		player.username = "[XACT]";
		return player;
	}

}
