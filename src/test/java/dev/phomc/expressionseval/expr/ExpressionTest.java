package dev.phomc.expressionseval.expr;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.Operator;
import dev.phomc.expressionseval.SampleEvalContext;

public class ExpressionTest {
	@Test
	public void testEvalConstants() {
		assertEquals("hello world", new Constant("hello world").eval(null, null));
		assertEquals(123, new Constant(123).eval(null, null));
	}

	@Test
	public void testEvalBinaryOp() {
		EvalContext ctx = new SampleEvalContext();

		// int x float (invalid)
		assertNull(new OperatorExpression(new Constant(123), Operator.AND, new Constant(125.0)).eval(ctx, null));

		// int x int
		assertEquals(0b000111 & 0b001100, new OperatorExpression(new Constant(0b000111), Operator.AND, new Constant(0b001100)).eval(ctx, null));
		assertEquals(0b000111 | 0b001100, new OperatorExpression(new Constant(0b000111), Operator.OR, new Constant(0b001100)).eval(ctx, null));
		assertEquals(0b000111 ^ 0b001100, new OperatorExpression(new Constant(0b000111), Operator.XOR, new Constant(0b001100)).eval(ctx, null));

		// long x int
		assertEquals(0b000111L & 0b001100, new OperatorExpression(new Constant(0b000111L), Operator.AND, new Constant(0b001100)).eval(ctx, null));
		assertEquals(0b000111L | 0b001100, new OperatorExpression(new Constant(0b000111L), Operator.OR, new Constant(0b001100)).eval(ctx, null));
		assertEquals(0b000111L ^ 0b001100, new OperatorExpression(new Constant(0b000111L), Operator.XOR, new Constant(0b001100)).eval(ctx, null));

		// long x long
		assertEquals(0b000111L & 0b001100L, new OperatorExpression(new Constant(0b000111L), Operator.AND, new Constant(0b001100L)).eval(ctx, null));
		assertEquals(0b000111L | 0b001100L, new OperatorExpression(new Constant(0b000111L), Operator.OR, new Constant(0b001100L)).eval(ctx, null));
		assertEquals(0b000111L ^ 0b001100L, new OperatorExpression(new Constant(0b000111L), Operator.XOR, new Constant(0b001100L)).eval(ctx, null));
	}

	@Test
	public void testEvalArithmetic() {
		EvalContext ctx = new SampleEvalContext();

		assertEquals(123 + 456, new OperatorExpression(new Constant(123), Operator.ADD, new Constant(456)).eval(ctx, null));
		assertEquals(123 - 456, new OperatorExpression(new Constant(123), Operator.SUBTRACT, new Constant(456)).eval(ctx, null));
		assertEquals(123 * 456, new OperatorExpression(new Constant(123), Operator.MULTIPLY, new Constant(456)).eval(ctx, null));
		assertEquals(123 / 456, new OperatorExpression(new Constant(123), Operator.DIVIDE, new Constant(456)).eval(ctx, null));

		assertEquals(123 + 456F, new OperatorExpression(new Constant(123), Operator.ADD, new Constant(456F)).eval(ctx, null));
		assertEquals(123 - 456F, new OperatorExpression(new Constant(123), Operator.SUBTRACT, new Constant(456F)).eval(ctx, null));
		assertEquals(123 * 456F, new OperatorExpression(new Constant(123), Operator.MULTIPLY, new Constant(456F)).eval(ctx, null));
		assertEquals(123 / 456F, new OperatorExpression(new Constant(123), Operator.DIVIDE, new Constant(456F)).eval(ctx, null));

		assertEquals(123F + 456D, new OperatorExpression(new Constant(123F), Operator.ADD, new Constant(456D)).eval(ctx, null));
		assertEquals(123F - 456D, new OperatorExpression(new Constant(123F), Operator.SUBTRACT, new Constant(456D)).eval(ctx, null));
		assertEquals(123F * 456D, new OperatorExpression(new Constant(123F), Operator.MULTIPLY, new Constant(456D)).eval(ctx, null));
		assertEquals(123F / 456D, new OperatorExpression(new Constant(123F), Operator.DIVIDE, new Constant(456D)).eval(ctx, null));
	}

	@Test
	public void testEvalComplex() {
		EvalContext ctx = new SampleEvalContext();
		Expression expr = new OperatorExpression(new Constant(12), Operator.MULTIPLY, new OperatorExpression(new Constant(34), Operator.ADD, new Constant(56)));
		assertEquals(12 * (34 + 56), expr.eval(ctx, null));
	}

	@Test
	public void testEvalVirtualMachine() {
		EvalContext ctx = new SampleEvalContext();
		Expression expr = new OperatorExpression(new Constant(12), Operator.MULTIPLY, new OperatorExpression(new Constant(34), Operator.ADD, new Constant(56)));
		VirtualMachineExpression compiled = VirtualMachineExpression.compile(expr);
		assertEquals(expr.eval(ctx, null), compiled.eval(ctx, null));
	}
}
