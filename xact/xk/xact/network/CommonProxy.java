package xk.xact.network;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import xk.xact.core.*;
import xk.xact.gui.*;

public class CommonProxy implements IGuiHandler {

	public void registerRenderInformation() {
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
	                                  int x, int y, int z) {
		// ID:
		// 0: crafter
		// 1: library
		// 2: vanilla workbench
		// 3: craft pad
		// 4: <none> (client only)
		// 5: recipe

		if( ID == 0 ) { // Crafter
			TileMachine machine = (TileMachine) world.getBlockTileEntity( x, y,
					z );
			if( machine == null )
				return null;

			if( machine instanceof TileCrafter ) {
				return new ContainerCrafter( (TileCrafter) machine, player );
			}
		}

		if( ID == 2 ) {
			TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity(
					x, y, z );
			if( workbench == null )
				return null;

			return new ContainerVanillaWorkbench( workbench, player );
		}

		if( ID == 1 ) { // Chip Case
			ChipCase chipCase = new ChipCase( player.inventory.getCurrentItem() );
			return new ContainerCase( chipCase, player );
		}

		if( ID == 3 ) { // Craft Pad
			CraftPad craftPad = new CraftPad( player.inventory.getCurrentItem(),
					player );
			return new ContainerPad( craftPad, player );
		}

		// no ID == 4. GuiPlan, client-side only.

		if( ID == 5 ) { // Set a recipe
			return new ContainerRecipe( player );
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
	                                  int x, int y, int z) {
		// ID:
		// 0: crafter
		// 1: library
		// 2: vanilla workbench
		// 3: craft pad
		// 4: plan (client only)
		// 5: recipe

		if( ID == 0 ) { // Crafter
			TileMachine machine = (TileMachine) world.getBlockTileEntity( x, y,
					z );
			if( machine == null )
				return null;

			if( machine instanceof TileCrafter ) {
				return new GuiCrafter( (TileCrafter) machine, player );
			}
		}

		if( ID == 1 ) { // Chip Case
			ChipCase chipCase = new ChipCase( player.inventory.getCurrentItem() );
			return new GuiCase( new ContainerCase( chipCase, player ) );
		}

		if( ID == 2 ) {
			TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity(
					x, y, z );
			if( workbench == null )
				return null;

			return new GuiVanillaWorkbench( new ContainerVanillaWorkbench(
					workbench, player ) );
		}

		if( ID == 3 ) { // Craft Pad
			CraftPad craftPad = new CraftPad( player.inventory.getCurrentItem(),
					player );
			return new GuiPad( craftPad, new ContainerPad( craftPad, player ) );
		}

		if( ID == 4 ) { // Open the plan.

			// ItemStack item = player.inventory.getCurrentItem();
			// if( item != null && item.getItem() instanceof ItemPlan ) {
			// CraftingProject project = CraftingProject.readFromNBT(
			// item.getTagCompound() );
			// return new GuiPlan(project);
			// }
		}

		if( ID == 5 ) { // Set a recipe

			// GuiScreen screen = getCurrentScreen();
			// if( screen instanceof GuiPlan ) {
			// GuiPlan plan = (GuiPlan) screen;
			// CraftRecipe recipe = plan.getCurrentRecipe();
			// GuiRecipe gui = new GuiRecipe(player, plan, new
			// ContainerRecipe(player));
			//
			// // Is there a recipe already set?
			// if( recipe != null ) {
			// // Yes, so send the recipe's ingredients to the ContainerRecipe
			// ItemStack[] ingredients = recipe.getIngredients();
			// for( int i = 0; i < 9; i++ ) {
			// try{
			// Packet250CustomPayload packet = new
			// CustomPacket((byte)0x03).add(i + 1, ingredients).toPacket();
			// Minecraft.getMinecraft().getSendQueue().addToSendQueue( packet );
			// }catch (IOException ioe) {
			// FMLCommonHandler.instance().raiseException(ioe,
			// "GuiRecipe: proxy - custom packet", true);
			// }
			// }
			// } else {
			// // Is there a target item to paint?
			// ItemStack target = plan.getTarget();
			// if( target != null )
			// gui.setTarget( target );
			// }
			//
			// return gui;
			// }
		}

		return null;
	}

	public void registerKeyBindings() {
	}
}
