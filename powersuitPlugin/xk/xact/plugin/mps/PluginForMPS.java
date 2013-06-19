package xk.xact.plugin.mps;


import cpw.mods.fml.common.Loader;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.IPowerModule;
import xk.xact.api.plugin.XACTPlugin;
import xk.xact.util.ReflectionUtils;
import xk.xact.util.Utils;

public class PluginForMPS implements XACTPlugin {

	private static Class mpsClass;

	private static void init() {
		mpsClass = ReflectionUtils.getClassByName( "net.machinemuse.powersuits.common.ModularPowersuits" );
		Class moduleManager = ReflectionUtils.getClassByName( "net.machinemuse.api.ModuleManager" );
		ReflectionUtils.invokeStaticMethod( moduleManager, "addModule", new Class[] { IPowerModule.class }, new CraftPadModule() );
	}

	@Override
	public void initialize() {
		if( Loader.isModLoaded( "mmmPowersuits" ) ) {
			Utils.log( "ModularPowerSuits detected. Initializing plug-in for MPS." );
			init();
		} else {
			Utils.log( "ModularPowerSuits not detected. Plug-in not initialized." );
		}
	}

	public static enum ModularItems {
		TOOL( "powerTool" ),
		BOOTS( "powerArmorFeet" ),
		LEGS( "powerArmorLegs" ),
		CHEST( "powerArmorTorso" ),
		HELMET( "powerArmorHead" );

		private final String fieldName;

		ModularItems(String fieldName) {
			this.fieldName = fieldName;
		}

		public IModularItem getItem() {
			if( mpsClass != null ) {
				return ReflectionUtils.getStaticFieldAs( mpsClass, fieldName, IModularItem.class );
			}
			return null;
		}
	}

}
