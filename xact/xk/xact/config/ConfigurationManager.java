package xk.xact.config;

import net.minecraftforge.common.Configuration;
import xk.xact.XActMod;
import xk.xact.core.blocks.BlockMachine;
import xk.xact.core.blocks.BlockVanillaWorkbench;
import xk.xact.core.items.*;

import java.io.File;

/**
 * @author Xhamolk_
 */
public class ConfigurationManager {

	private static Configuration config;

	public static void loadConfiguration(File configFile) {
		config = new Configuration( configFile );
		config.load();

		machineID = config.getBlock( "machineID", 3919 ).getInt();
		blankChipID = config.getItem( "blankChip", 9100 ).getInt();
		encodedChipID = config.getItem( "encodedChip", 9101 ).getInt();
		caseID = config.getItem( "chipCase", 9102 ).getInt();
		padID = config.getItem( "craftPad", 9103 ).getInt();

		ENABLE_MPS_PLUGIN = config.get( "Plug-ins", "enableModularPowerSuitsPlugin", true,
				"If true, XACT will try to initialize the plug-in for Modular PowerSuits. \n" +
						"This plug-in let's you install the Craft Pad into the MPS Power Fist." )
				.getBoolean( true );

		ENABLE_BETTER_STORAGE_PLUGIN = config.get( "Plug-ins", "enableBetterStoragePlugin", true,
				"If true, XACT will try to initialize the plug-in for the Better Storage mod. \n" +
						"This plug-in enables the XACT Crafter to pull resources from adjacent crates (from Better Storage)." )
				.getBoolean( true );

		ENABLE_AE_PLUGIN = config.get( "Plug-ins", "enableAppliedEnergisticsPlugin", true,
				"If true, XACT will try to initialize the plug-in for the Applied Energistics mod. \n" +
						"This plug-in enables the XACT Crafter to pull resources from adjacent ME Interfaces, \n" +
						"which acts as a access point to that particular ME Network." )
				.getBoolean( true );


		REPLACE_WORKBENCH = config.get( "Miscellaneous", "addWorkbenchTileEntity", true,
				"If true, XACT will make the vanilla workbench able to keep it's contents on the grid after the GUI is closed. \n" +
						"Make sure you clear the workbench's grid before setting this to false, or you will lose your items." )
				.getBoolean( true );

		config.save();
	}

	public static void initItems() {
		XActMod.itemRecipeBlank = new ItemChip( blankChipID, false );
		XActMod.itemRecipeEncoded = new ItemChip( encodedChipID, true );
		XActMod.itemChipCase = new ItemCase( caseID );
		XActMod.itemCraftPad = new ItemPad( padID );
	}

	public static void initBlocks() {
		XActMod.blockMachine = new BlockMachine( machineID );
		if( REPLACE_WORKBENCH )
			XActMod.blockWorkbench = BlockVanillaWorkbench.createNew();
	}


	public static int machineID;
	public static int blankChipID;
	public static int encodedChipID;
	public static int caseID;
	public static int padID;

	public static boolean REPLACE_WORKBENCH;

	public static boolean ENABLE_MPS_PLUGIN;

	public static boolean ENABLE_BETTER_STORAGE_PLUGIN;

	public static boolean ENABLE_AE_PLUGIN;

	// debugging information.
	public static boolean DEBUG_MODE = false;

}
