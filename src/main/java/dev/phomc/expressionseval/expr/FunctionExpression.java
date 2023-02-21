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
