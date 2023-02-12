package dev.phomc.expressionseval.expr;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.VariablesInterface;

public class Constant extends Expression {
	public final Object val;

	public Constant(Object val) {
		this.val = val;
	}

	@Override
	public Object eval(EvalContext ctx, VariablesInterface variables) {
		return val;
	}

	@Override
	public String toString() {
		return "const(" + val + ")";
	}
}
