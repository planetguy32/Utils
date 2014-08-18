package me.planetguy.core;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class ASMFixesModContainer extends DummyModContainer{

	public ASMFixesModContainer(){
            super(new ModMetadata());
            ModMetadata md = super.getMetadata();
            md.modId = "planetguy_CoreLoadingPlugin";
            md.name = "Planetguy Core";
            md.version = "0.1";
            md.authorList = Arrays.asList(new String[]{"planetguy"});
            md.url = "https://github.com/planetguy32/Utils";
            md.description = "Discovers classes for SimpleLoader.";
    }
	
	@Override
	public boolean registerBus(EventBus var1, LoadController var2){
		var1.register(this);
		return true;
	}
}
