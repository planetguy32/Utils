package me.planetguy.lib.util;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class Lang {

	public static String translate(String s) {
		String res= LanguageRegistry.instance().getStringLocalization(s);
		if(res.equals("")){
			throw new RuntimeException("Failed to translate "+s);
		}else{
			return res;
		}
	}

}
