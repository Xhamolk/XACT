package xk.xact.network;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import xk.xact.XActMod;
import xk.xact.client.KeyBindingHandler;
import xk.xact.client.render.BlueprintRenderer;
import xk.xact.client.render.ChipRenderer;
import xk.xact.client.gui.GuiCase;
import xk.xact.client.gui.GuiPad;
import xk.xact.client.gui.GuiVanillaWorkbench;
import xk.xact.core.ChipCase;
import xk.xact.core.CraftPad;
import xk.xact.core.tileentities.TileMachine;
import xk.xact.core.tileentities.TileWorkbench;
import xk.xact.gui.ContainerCase;
import xk.xact.gui.ContainerPad;
import xk.xact.gui.ContainerVanillaWorkbench;

public class ClientProxy extends CommonProxy {


	@SideOnly(Side.CLIENT)
	public static GuiScreen getCurrentScreen() {
		return Minecraft.getMinecraft().currentScreen;
	}

	@Override
	public void registerRenderInformation() {
		// Custom IItemRenderer
		MinecraftForgeClient.registerItemRenderer( XActMod.itemRecipeEncoded.itemID, new ChipRenderer() );
		MinecraftForgeClient.registerItemRenderer( XActMod.itemBlueprint.itemID, new BlueprintRenderer() );
	}

	@Override
	public void registerHandlers() {
		super.registerHandlers();

		// Register KeyBindingHandler
		KeyBindingRegistry.registerKeyBinding( new KeyBindingHandler() );

		// Register TickHandler
		TickRegistry.registerTickHandler( GuiTickHandler.instance(), Side.CLIENT );
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// ID:
		// 0: machines
		// 1: library
		// 2: vanilla workbench
		// 3: craft pad
		// 4: plan (client only)
		// 5: recipe

		if( ID == 0 ) { // Machines
			TileMachine machine = (TileMachine) world.getBlockTileEntity( x, y, z );
			if( machine == null )
				return null;

			return machine.getGuiContainerFor( player );
		}

		if( ID == 1 ) { // Chip Case
			ChipCase chipCase = new ChipCase( player.inventory.getCurrentItem() );
			return new GuiCase( new ContainerCase( chipCase, player ) );
		}

		if( ID == 2 ) { // Vanilla Workbench
			TileWorkbench workbench = (TileWorkbench) world.getBlockTileEntity( x, y, z );
			if( workbench == null )
				return null;

			return new GuiVanillaWorkbench( new ContainerVanillaWorkbench( workbench, player ) );
		}

		if( ID == 3 ) { // Craft Pad
			CraftPad craftPad = new CraftPad( player.inventory.getCurrentItem(), player );
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

	public static NetClientHandler getNetClientHandler() {
		return Minecraft.getMinecraft().getNetHandler();
	}
}
