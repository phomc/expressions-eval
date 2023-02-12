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
