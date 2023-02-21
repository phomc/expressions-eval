/*
 * This file is part of tensai, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2023 PhoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.phomc.expressionseval;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SimpleEvalContext extends EvalContext {
	@Override
	default Object applyOperator(Object a, Operator op, Object b) {
		if (op == Operator.EQUALS) return Objects.equals(a, b);
		if (op == Operator.GREATER_THAN || op == Operator.GREATER_OR_EQU || op == Operator.LESS_THAN || op == Operator.LESS_OR_EQU) return comparison(a, op, b);
		if (op == Operator.AND || op == Operator.OR || op == Operator.XOR) return binaryOp(a, op, b);
		if (op == Operator.ADD || op == Operator.SUBTRACT || op == Operator.MULTIPLY || op == Operator.DIVIDE) return arithmetic(a, op, b);

		if (op == Operator.POWER) {
			if (!(a instanceof Number na)) return null;
			if (!(b instanceof Number nb)) return null;

			double base = na.doubleValue();
			double exp = nb.doubleValue();
			return Math.pow(base, exp);
		}

		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object comparison(Object a, Operator op, Object b) {
		if (!(a instanceof Comparable ca)) return null;
		if (!(b instanceof Comparable cb)) return null;
		int compare = ca.compareTo(cb);

		return switch (op) {
		case GREATER_THAN -> compare > 0? a : b;
		case GREATER_OR_EQU -> compare >= 0? a : b;
		case LESS_THAN -> compare < 0? a : b;
		case LESS_OR_EQU -> compare <= 0? a : b;
		default -> null;
		};
	}

	private static Object binaryOp(Object a, Operator op, Object b) {
		long la = 0, lb = 0;
		int ia = 0, ib = 0;
		boolean isIntA = false, isIntB = false;

		if (a instanceof Integer) {
			ia = (int) a;
			isIntA = true;
		} else if (a instanceof Long) {
			la = (long) a;
		} else {
			return null;
		}

		if (b instanceof Integer) {
			ib = (int) b;
			isIntB = true;
		} else if (b instanceof Long) {
			lb = (long) b;
		} else {
			return null;
		}

		if (isIntA && isIntB) return switch (op) {
		case AND -> ia & ib;
		case OR -> ia | ib;
		case XOR -> ia ^ ib;
		default -> null;
		};

		if (isIntA && !isIntB) la = ia;
		if (!isIntA && isIntB) lb = ib;

		return switch (op) {
		case AND -> la & lb;
		case OR -> la | lb;
		case XOR -> la ^ lb;
		default -> null;
		};
	}

	public static final Map<Class<? extends Number>, Integer> INTEGER_TYPE_LEVELS = Map.of(
			Byte.class, 0,
			Short.class, 1,
			Integer.class, 2,
			Long.class, 3);
	public static final List<Class<? extends Number>> REVERSE_INTEGER_TYPE_LEVELS = Arrays.asList(
			Byte.class,
			Short.class,
			Integer.class,
			Long.class);

	private static Object integersArithmetic(Number na, Operator op, Number nb) {
		int levelA = INTEGER_TYPE_LEVELS.get(na.getClass()), levelB = INTEGER_TYPE_LEVELS.get(nb.getClass());
		Class<? extends Number> highestType = REVERSE_INTEGER_TYPE_LEVELS.get(Math.max(levelA, levelB));

		if (highestType == Byte.class) return switch (op) {
		case ADD -> na.byteValue() + nb.byteValue();
		case SUBTRACT -> na.byteValue() - nb.byteValue();
		case MULTIPLY -> na.byteValue() * nb.byteValue();
		case DIVIDE -> na.byteValue() / nb.byteValue();
		default -> null;
		};

		if (highestType == Short.class) return switch (op) {
		case ADD -> na.shortValue() + nb.shortValue();
		case SUBTRACT -> na.shortValue() - nb.shortValue();
		case MULTIPLY -> na.shortValue() * nb.shortValue();
		case DIVIDE -> na.shortValue() / nb.shortValue();
		default -> null;
		};

		if (highestType == Integer.class) return switch (op) {
		case ADD -> na.intValue() + nb.intValue();
		case SUBTRACT -> na.intValue() - nb.intValue();
		case MULTIPLY -> na.intValue() * nb.intValue();
		case DIVIDE -> na.intValue() / nb.intValue();
		default -> null;
		};

		if (highestType == Long.class) return switch (op) {
		case ADD -> na.longValue() + nb.longValue();
		case SUBTRACT -> na.longValue() - nb.longValue();
		case MULTIPLY -> na.longValue() * nb.longValue();
		case DIVIDE -> na.longValue() / nb.longValue();
		default -> null;
		};

		return null;
	}

	private static Object floatArithmetic(Number na, Operator op, Number nb) {
		int levelA = (na instanceof Long || na instanceof Double)? 1 : 0, levelB = (nb instanceof Long || nb instanceof Double)? 1 : 0;
		int highestLevel = Math.max(levelA, levelB);

		if (highestLevel == 0) return switch (op) {
		case ADD -> na.floatValue() + nb.floatValue();
		case SUBTRACT -> na.floatValue() - nb.floatValue();
		case MULTIPLY -> na.floatValue() * nb.floatValue();
		case DIVIDE -> na.floatValue() / nb.floatValue();
		default -> null;
		};

		if (highestLevel == 1) return switch (op) {
		case ADD -> na.doubleValue() + nb.floatValue();
		case SUBTRACT -> na.doubleValue() - nb.doubleValue();
		case MULTIPLY -> na.doubleValue() * nb.doubleValue();
		case DIVIDE -> na.doubleValue() / nb.doubleValue();
		default -> null;
		};

		return null;
	}

	private static Object arithmetic(Object a, Operator op, Object b) {
		if (!(a instanceof Number na)) return null;
		if (!(b instanceof Number nb)) return null;

		boolean isFloatA = false, isFloatB = false;
		if (a instanceof Float || a instanceof Double) isFloatA = true;
		if (b instanceof Float || b instanceof Double) isFloatB = true;

		if (isFloatA || isFloatB) return floatArithmetic(na, op, nb);
		return integersArithmetic(na, op, nb);
	}

	@Override
	default Object propertyOf(Object src, String name, VariablesInterface variables) {
		if (src == null) {
			if (variables != null) return variables.get(name);
			return null;
		}

		if (src instanceof Map<?, ?> map) return map.get(name);
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	default Object functionCall(Object function, Object[] parameters) {
		if (function instanceof Runnable run) run.run();
		if (function instanceof Consumer con && parameters.length >= 1) con.accept(parameters[0]);
		if (function instanceof BiConsumer con && parameters.length >= 2) con.accept(parameters[0], parameters[1]);

		if (function instanceof Supplier sup) return sup.get();
		if (function instanceof Function fun && parameters.length >= 1) return fun.apply(parameters[0]);
		if (function instanceof BiFunction fun && parameters.length >= 2) return fun.apply(parameters[0], parameters[1]);

		return null;
	}
}
