package me.planetguy.core.simpleLoader;

public class SLLoadUtils {

	/**Utility method to get the module name of a class
	 * 
	 * @param c class to get module name from
	 * @return name of module as declared in its @SLLoad annotation
	 */
	
	public static String getModuleName(Class c){
		return SLLoadUtils.getSLL(c).name();
	}

	public static String[] getDependencies(Class c){
		return SLLoadUtils.getSLL(c).dependencies();
	}
	
	public static SLLoad getSLL(Class c){
		return (SLLoad) c.getAnnotation(SLLoad.class);
	}

}
