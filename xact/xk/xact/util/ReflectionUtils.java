package xk.xact.util;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

	/**
	 * Tries to get the requested field from the specified class.
	 *
	 * @param clazz the class where the field is declared.
	 * @param fieldName the name of the field.
	 * @return the declared Field, or null if it couldn't be found.
	 */
	public static Field getField(Class clazz, String fieldName) {
		try {
			Field field = clazz.getField( fieldName );
			field.setAccessible( true );
			return field;
		} catch( NoSuchFieldException e ) {
			return null;
		}
	}

	/**
	 * Tries to get the requested instance field from the specified class.
	 *
	 * @param clazz the class where the field is declared.
	 * @param fieldName the name of the field.
	 * @param fieldType the class of the field.
	 * @param instance the instance from where to read the field.
	 * @param <T> the return type, described by <code>fieldType</code>
	 * @return the declared field (cast to the appropriate type), or null if it couldn't be found.
	 */
	public static <T> T getFieldAs(Class clazz, String fieldName, Class<? extends T> fieldType, Object instance) {
		Field field = getField( clazz, fieldName );
		if( field != null ) {
			if( fieldType.isAssignableFrom( field.getType() ) ) { // check types
				try {
					return fieldType.cast( field.get( instance ) );
				} catch( IllegalAccessException e ) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Tries to get the requested static field from the specified class.
	 *
	 * @param clazz the class where the field is declared.
	 * @param fieldName the name of the field.
	 * @param fieldType the class of the field.
	 * @param <T> the return type.
	 * @return the declared field (cast to the appropriate type), or null if it couldn't be found.
	 */
	public static <T> T getStaticFieldAs(Class clazz, String fieldName, Class<? extends T> fieldType) {
		Field field = getField( clazz, fieldName );
		if( field != null ) {
			if( fieldType.isAssignableFrom( field.getType() ) ) { // check types
				try {
					return fieldType.cast( field.get( null ) );
				} catch( IllegalAccessException e ) {
					return null;
				}
			}
		}
		return null;
	}


	/**
	 * Tries to get the requested method from the specified class.
	 *
	 * @param clazz the class where the field is declared.
	 * @param methodName the name of the method.
	 * @param parameters the type of the arguments this method takes.
	 * @return the declared Method, or null if it couldn't be found.
	 */
	@SuppressWarnings( "unchecked" )
	public static Method getMethod(Class clazz, String methodName, Class[] parameters) {
		try {
			return clazz.getMethod( methodName, parameters );
		} catch( NoSuchMethodException e ) {
			return null;
		}
	}

	/**
	 * Tries to invoke the specified instance method, passing the specified arguments into it.
	 *
	 * @param clazz the class where the field is declared.
	 * @param methodName the name of the method.
	 * @param parameters the type of the arguments this method takes.
	 * @param instance the instance from which the method is invoked
	 * @param args the arguments to be passed into the method.
	 * @return the value returned from the method call.
	 */
	public static Object invokeMethod(Class clazz, String methodName, Class[] parameters, Object instance, Object... args) {
		Method method = getMethod( clazz, methodName, parameters );
		if( method != null ) {
			try {
				method.setAccessible( true );
				return method.invoke( instance, args );
			} catch( IllegalAccessException e ) {
			} catch( InvocationTargetException e ) {
			}
		}
		return null;
	}

	/**
	 * Tries to invoke the specified static method, passing the specified arguments into it.
	 *
	 * @param clazz the class where the field is declared.
	 * @param methodName the name of the method.
	 * @param parameters the type of the arguments this method takes.
	 * @param args the arguments to be passed into the method.
	 * @return the value returned from the method call.
	 */
	public static Object invokeStaticMethod(Class clazz, String methodName, Class[] parameters, Object... args) {
		Method method = getMethod( clazz, methodName, parameters );
		if( method != null ) {
			try {
				method.setAccessible( true );
				return method.invoke( null, args );
			} catch( IllegalAccessException e ) {
			} catch( InvocationTargetException e ) {
			}
		}
		return null;
	}

}
