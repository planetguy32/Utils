package me.planetguy.lib.cc;

import java.lang.reflect.Method;
import java.util.HashMap;

import me.planetguy.lib.api.SPMethod;

public class SPReflection {
	
	public final Class target;
	public final HashMap<String, Method> methodMap=new HashMap<String, Method>();
	
	SPReflection(Class c){
		target=c;
		Method[] methods=c.getMethods();
		for(Method m:methods){
			if(m.getAnnotation(SPMethod.class)!=null){
				methodMap.put(m.getName(), m);
			}
		}
	}

}
