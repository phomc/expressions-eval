package dev.phomc.expressionseval.expr;

import java.util.stream.Stream;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.VariablesInterface;

public class FunctionExpression extends Expression {
	public final Expression target;
	public final Expression[] parameters;

	public FunctionExpression(Expression target, Expression[] parameters) {
		this.target = target;
		this.parameters = parameters;
	}

	@Override
	public Object eval(EvalContext ctx, VariablesInterface variables) {
		Object[] params = new Object[parameters.length];
		for (int i = 0; i < params.length; i++) params[i] = parameters[i].eval(ctx, variables);
		return ctx.functionCall(target.eval(ctx, variables), params);
	}

	@Override
	public String toString() {
		return "call[" + target + "]: (" + String.join(", ", Stream.of(parameters).map(v -> v.toString()).toArray(String[]::new)) + ")";
	}
}
