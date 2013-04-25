package xk.xact.util;


public class ReflectionUtils {

	/**
	 * Whether if the object's class's simple name matches with the specified parameter.
	 *
	 * @param o         the object to compare.
	 * @param className the simple name of the class.
	 */
	private static boolean classMatches(Object o, String className) {
		return o != null && o.getClass().getSimpleName().equals( className );
	}

	/**
	 * Tries to get a Class object based on the class' full name (includes package).
	 *
	 * @param classFullName the name and package of the class, separated by dots.
	 * @return the Class object, or null if it couldn't be found.
	 */
	public static Class getClassByName(String classFullName) {
		try {
			return Class.forName( classFullName );
		} catch( ClassNotFoundException e ) {
			return null;
		}
	}

	/**
	 * Tries to create a new instance for the Class specified.
	 * The class must have a parameter-less constructor for this to work.
	 *
	 * @param clazz The Class from which this will create an instance.
	 * @return the new instance of Class, or null if this failed.
	 */
	public static Object newInstanceOf(Class clazz) {
		try {
			return clazz.newInstance();
		} catch( InstantiationException e ) {
			e.printStackTrace( System.err );
		} catch( IllegalAccessException e ) {
			e.printStackTrace( System.err );
		}
		return null;
	}

	/**
	 * Whether if the object is instance of the class.
	 *
	 * @param o     the object
	 * @param clazz the class.
	 */
	public static boolean isObjectInstanceOf(Object o, Class clazz) {
		return clazz.isInstance( o );
	}

	/**
	 * Whether if <code>clas1</code> inherits from <code>clas2</code>
	 *
	 * @param clas1 the expected subclass.
	 * @param clas2 the expected superclass.
	 */
	@SuppressWarnings("unchecked")
	public static boolean isSubclassOf(Class clas1, Class clas2) {
		return clas2.isAssignableFrom( clas1 ) && !clas2.equals( clas1 );
	}
}
