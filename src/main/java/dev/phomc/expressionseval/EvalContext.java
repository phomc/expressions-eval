package dev.phomc.expressionseval;

public interface EvalContext {
	Object applyOperator(Object a, Operator op, Object b);

	/**
	 * Obtain the property from source object.
	 * @param src Source object. {@code null} if you are obtaining globally defined object or value from
	 * defined variable.
	 * @param name Name of property in object.
	 * @param variables An interface to obtain variable values.
	 * @return An object.
	 */
	Object propertyOf(Object src, String name, VariablesInterface variables);

	Object functionCall(Object function, Object[] parameters);
}
