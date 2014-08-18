package me.planetguy.core.sl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;

public class SLSort {

	//rather dumb way to sort classes with SLLoad annotations. 
	public static void sort(Class[] moduleClasses) {
		HashSet<String> seenModules=new HashSet<String>();
		Queue<Class> modules=new ArrayDeque<Class>();
		for(Class c:moduleClasses){
			modules.add(c);
		}
		int index=0;
		while(!modules.isEmpty()){
			Class c=modules.poll();
			String name=SLLoadUtils.getModuleName(c);
			String[] deps=SLLoadUtils.getDependencies(c);
			boolean allDepsDone=true;
			for(String s:deps){
				if(!seenModules.contains(s)){
					allDepsDone=false;
				}
			}
			if(allDepsDone){
				seenModules.add(name);
				moduleClasses[index]=c;
				index++;
			}else{
				modules.add(c);
			}
		}
		
	}

}
