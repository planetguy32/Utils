package me.planetguy.lib.api;

import me.planetguy.core.hook.ChangeBlockHooks;

public class PlanetguyLibRegistry {
	
	public static void register(IChangeBlockHook hook){
		ChangeBlockHooks.registerHook(hook);
	}

}
