package dev.phomc.expressionseval.expr;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.Operator;
import dev.phomc.expressionseval.VariablesInterface;

public class OperatorExpression extends Expression {
	public final Expression a, b;
	public final Operator operator;

	public OperatorExpression(Expression a, Operator op, Expression b) {
		this.a = a;
		this.b = b;
		this.operator = op;
	}

	@Override
	public Object eval(EvalContext ctx, VariablesInterface variables) {
		Object objA = a.eval(ctx, variables);
		Object objB = b.eval(ctx, variables);
		return ctx.applyOperator(objA, operator, objB);
	}

	@Override
	public String toString() {
		return "(" + a + " " + operator.symbol + " " + b + ")";
	}
}
