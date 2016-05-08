package me.planetguy.lib.util;

import me.planetguy.lib.PlanetguyLib;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class Lang {

	public static String translate(String s) {
		String res= LanguageRegistry.instance().getStringLocalization(s);
		if(res.equals("")){
			//Should we be anal about missing translations?
			if(PlanetguyLib.doPLLogging)
				throw new RuntimeException("Failed to translate "+s);
			return s;
		}else{
			return res;
		}
	}

}
