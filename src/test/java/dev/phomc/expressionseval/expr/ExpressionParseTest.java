package dev.phomc.expressionseval.expr;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import dev.phomc.expressionseval.SampleEvalContext;
import dev.phomc.expressionseval.VariablesInterface;

public class ExpressionParseTest {
	@Test
	public void testReadNumber() {
		assertEquals(1234, new Expression.Reader("1234").nextNumber());
		assertEquals(1234L, new Expression.Reader("L1234").nextNumber());
		assertEquals(1234L, new Expression.Reader("l1234").nextNumber());
		assertEquals(1234.2345F, new Expression.Reader("F1234.2345").nextNumber());
		assertEquals(1234.2345F, new Expression.Reader("f1234.2345").nextNumber());
		assertEquals(1234.2345D, new Expression.Reader("1234.2345").nextNumber());
		assertEquals(5000F, new Expression.Reader("F5000").nextNumber());
		assertEquals(5000D, new Expression.Reader("D5000").nextNumber());

		Expression.Reader reader = new Expression.Reader("  f1234.2345  5123  ");
		assertEquals(1234.2345F, reader.nextNumber());
		assertEquals(12, reader.pointer);

		assertEquals(5123, reader.nextNumber());
		assertEquals(18, reader.pointer);

		assertNull(new Expression.Reader("F 12.3").nextNumber());
	}

	@Test
	public void testParser() {
		Expression expr = Expression.parse("12 + myFunction(23, math.sin(34) + 45 + (123 + 456 * 789))");
		double result = ((Number) expr.eval(new SampleEvalContext(), VariablesInterface.of(Map.of(
				"myFunction", (BiFunction<Object, Object, Object>) (t, u) -> {
					return ((Number) t).doubleValue() + ((Number) u).doubleValue();
				},
				"math", Map.of(
						"sin", (Function<Object, Object>) t -> Math.sin(((Number) t).doubleValue())
						))))).doubleValue();
		assertTrue(359987 < result && result < 359988);

		assertEquals(1 + 2 * (3 - 4 * 5 * (6 + 7 * 8)), Expression.parse("1 + 2 * (3 - 4 * 5 * (6 + 7 * 8))").eval(new SampleEvalContext(), null));
		assertEquals(1 + 2 * (3 - 4 * 5 * (6 + 7 * 8)), Expression.compile("1 + 2 * (3 - 4 * 5 * (6 + 7 * 8))").eval(new SampleEvalContext(), null));
	}
}
