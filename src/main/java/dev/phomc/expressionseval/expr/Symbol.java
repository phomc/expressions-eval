package dev.phomc.expressionseval.expr;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.VariablesInterface;

public class Symbol extends Expression {
	public final Expression target;
	public final String name;

	public Symbol(Expression target, String name) {
		this.target = target;
		this.name = name;
	}

	@Override
	public Object eval(EvalContext ctx, VariablesInterface variables) {
		return ctx.propertyOf(target.eval(ctx, variables), name, variables);
	}

	@Override
	public String toString() {
		return target.toString() + "." + name;
	}
}
