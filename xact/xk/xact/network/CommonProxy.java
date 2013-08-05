package xk.xact.network;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;
import xk.xact.core.ChipCase;
import xk.xact.core.CraftPad;
import xk.xact.core.tileentities.TileMachine;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.gui.*;

public class CommonProxy implements IGuiHandler {

	/**
	 * Register client-side rendering stuff.
	 */
	public void registerRenderInformation() { }

	/**
	 * Register side-sensitive handlers, like TickHandlers, Key Bindings, etc.
	 */
	public void registerHandlers() {
		TickRegistry.registerTickHandler( GuiTickHandler.instance(), Side.SERVER );
	}

	@Override
	public Object getServerGuiElement(int GuiID, EntityPlayer player, World world, int x, int y, int z) {
		int ID = (GuiID & 0xFF);
		int meta = (GuiID >> 8) & 0xFFFF;

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
			int invSlot = meta == 0 ? player.inventory.currentItem : meta - 1;
			ItemStack item = player.inventory.mainInventory[invSlot];
			item.setItemDamage( 1 );
			CraftPad craftPad = new CraftPad( item, player );
			return new ContainerPad( craftPad, player, invSlot );
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
		return new FakePlayer( world, "[XACT]" );
	}

}
