package me.planetguy.util ;

import java.util.Arrays;

public abstract class Debug
{
	public static void dbt(Object o){
		StackTraceElement[] trace=Thread.currentThread().getStackTrace();
		dbg(Arrays.toString(trace)+" >>> "+o);
	}
	
	private static void dbg_delegate(Object o){
		StackTraceElement[] trace=Thread.currentThread().getStackTrace();
		System.out.println(trace[3].getClassName().replaceAll("[a-zA-Z]*\\.", "")+"@"+trace[2].getLineNumber()+"   "+o);
		//if(Configuration.Debug.verbose)	log.debug(m,o);
	}
	
	public static void dbg(Object o){
		dbg_delegate(o);
	}
	
	public static void exception(Throwable t){
		dbg(t.toString());
	}
	
	public void dump(Object o){
		if(o==null){
			dbg_delegate("NULL");
		}else{
			dbg_delegate(o.getClass());
			for(java.lang.reflect.Field f:o.getClass().getFields()){
				try {
					dbg_delegate(f.getName()+":   "+f.get(o));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		
	}
	
}
