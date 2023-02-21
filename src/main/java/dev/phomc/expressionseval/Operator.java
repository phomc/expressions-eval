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

public enum Operator {
	AND("&"), OR("|"), XOR("^"),
	POWER("**"),
	MULTIPLY("*"), DIVIDE("/"),
	ADD("+"), SUBTRACT("-"),
	EQUALS("=="), GREATER_THAN(">"), LESS_THAN("<"), GREATER_OR_EQU(">="), LESS_OR_EQU("<=");

	public static final Operator[][] ORDER_OF_OPERATIONS = new Operator[][] {
		{ AND, OR, XOR },
		{ POWER },
		{ MULTIPLY, DIVIDE },
		{ ADD, SUBTRACT },
		{ EQUALS, GREATER_OR_EQU, GREATER_THAN, LESS_OR_EQU, LESS_THAN }
	};

	public final String symbol;

	Operator(String symbol) {
		this.symbol = symbol;
	}

	public static Operator parse(String s) {
		if (s == null) return null;
		s = s.trim();
		for (Operator op : values()) if (op.symbol.equals(s)) return op;
		return null;
	}
}
