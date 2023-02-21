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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.Operator;
import dev.phomc.expressionseval.VariablesInterface;

public abstract class Expression {
	public abstract Object eval(EvalContext ctx, VariablesInterface variables);

	// Parsing
	public static class Reader {
		private static final Pattern NUMBER = Pattern.compile("^\\s*(?<integer>[LlFfDd]?\\d+)(\\.(?<float>\\d+))?");
		private static final Pattern SYMBOL_START = Pattern.compile("^\\s*(?<symbolName>[A-Za-z][A-Za-z0-9]*)");
		private static final Pattern OPERATOR = Pattern.compile("^\\s*(?<operator>\\*\\*|==|>=|<=|[+\\-*/><&|^])");

		public final String text;
		public int pointer;

		public Reader(String text) {
			this.text = text;
			this.pointer = 0;
		}

		public boolean endOfText() { return pointer >= text.length(); }

		public Number nextNumber() {
			if (endOfText()) return null;
			Matcher matcher = NUMBER.matcher(text).region(pointer, text.length());

			if (matcher.find()) {
				pointer = matcher.end();
				String integer = matcher.group("integer");
				String floating = matcher.group("float");

				if (floating == null) {
					boolean isLong = integer.toLowerCase().startsWith("l");
					boolean isFloat = integer.toLowerCase().startsWith("f");
					boolean isDouble = integer.toLowerCase().startsWith("d");

					if (isLong) return Long.parseLong(integer.substring(1));
					if (isFloat) return Float.parseFloat(integer.substring(1));
					if (isDouble) return Double.parseDouble(integer.substring(1));
					return Integer.parseInt(integer);
				} else {
					boolean isFloat = integer.toLowerCase().startsWith("f");
					boolean isDouble = integer.toLowerCase().startsWith("d");

					if (isDouble) integer = integer.substring(1);
					if (isFloat) {
						integer = integer.substring(1);
						return Float.parseFloat(integer + "." + floating);
					} else {
						return Double.parseDouble(integer + "." + floating);
					}
				}
			}

			return null;
		}

		public String nextSymbolName() {
			if (endOfText()) return null;
			Matcher matcher = SYMBOL_START.matcher(text).region(pointer, text.length());

			if (matcher.find()) {
				pointer = matcher.end();
				String name = matcher.group("symbolName");
				return name;
			}

			return null;
		}

		public Operator nextOperator() {
			if (endOfText()) return null;
			Matcher matcher = OPERATOR.matcher(text).region(pointer, text.length());

			if (matcher.find()) {
				pointer = matcher.end();
				return Operator.parse(matcher.group("operator"));
			}

			return null;
		}

		public boolean nextString(String str) {
			if (endOfText()) return false;

			if (text.indexOf(str, pointer) == pointer) {
				pointer += str.length();
				return true;
			}

			return false;
		}

		public void skipWhitespaces() {
			while (!endOfText() && (text.charAt(pointer) == ' ' || text.charAt(pointer) == '\t')) pointer++;
		}
	}

	protected static class Token {}

	protected static class ConstantToken extends Token {
		public final Object val;
		public ConstantToken(Object val) { this.val = val; }

		@Override
		public String toString() { return Objects.toString(val); }
	}

	protected static class SymbolToken extends Token {
		public final String name;
		public SymbolToken(String name) { this.name = name; }

		@Override
		public String toString() { return name; }
	}

	protected static class OperatorToken extends Token {
		public final Operator operator;
		public OperatorToken(Operator operator) { this.operator = operator; }

		@Override
		public String toString() { return operator.symbol; }
	}

	protected static final Token PARENTHESES_OPEN = new Token() { public String toString() { return "("; } };
	protected static final Token PARENTHESES_CLOSE = new Token() { public String toString() { return ")"; } };
	protected static final Token COMMA = new Token() { public String toString() { return "comma"; } };
	protected static final Token DOT = new Token() { public String toString() { return "."; } };

	// Post processing
	protected static class SymbolsStack extends Token {
		public List<String> stack = new ArrayList<>();

		@Override
		public String toString() { return String.join(".", stack.toArray(String[]::new)); }
	}

	protected static class FunctionToken extends Token {
		public Token target;
		public final List<List<Token>> parameters = new ArrayList<>();
		public FunctionToken(Token target) { this.target = target; }

		@Override
		public String toString() { return target + "(" + String.join(", ", parameters.stream().map(v -> v.toString()).toArray(String[]::new)) + ")"; }
	}

	protected static class GroupToken extends Token {
		public List<Token> children = new ArrayList<>();

		@Override
		public String toString() { return "(g:" + children + ")"; }
	}

	protected static class ProcessedOperatorToken extends Token {
		public Token a, b;
		public Operator operator;

		public ProcessedOperatorToken(Token a, Operator op, Token b) {
			this.a = a;
			this.b = b;
			this.operator = op;
		}

		@Override
		public String toString() { return "{" + a + " " + operator.symbol + " " + b + "}"; }
	}

	public static Expression parse(String exprStr) {
		// 'varName.property'
		// '1 + 2 * (3 / 4 + 5 * (6 * 7) ** 8)'
		// '12 + myFunction(23, math.sin(34) + 45 + (123 + 456 + 789))' -> [12, myFunction, (, 23, math, ., sin, (, 34, ), +, 45, )]
		// tokenize -> reduce -> expression -> compiled expression -> optimized expression

		List<Token> tokens = new ArrayList<>();
		Reader reader = new Reader(exprStr);
		reader.skipWhitespaces();

		while (!reader.endOfText()) {
			Object holder;

			if ((holder = reader.nextNumber()) != null) tokens.add(new ConstantToken(holder));
			else if ((holder = reader.nextSymbolName()) != null) tokens.add(new SymbolToken((String) holder));
			else if ((holder = reader.nextOperator()) != null) tokens.add(new OperatorToken((Operator) holder));
			else if (reader.nextString("(")) tokens.add(PARENTHESES_OPEN);
			else if (reader.nextString(")")) tokens.add(PARENTHESES_CLOSE);
			else if (reader.nextString(",")) tokens.add(COMMA);
			else if (reader.nextString(".")) tokens.add(DOT);
			else throw new RuntimeException("Failed to tokenize: Head is '" + reader.text.substring(reader.pointer, Math.min(reader.pointer + 12, reader.text.length())) + "'");

			reader.skipWhitespaces();
		}

		reduceAll(tokens);
		if (tokens.size() > 1) throw new RuntimeException("Tokens are not reduced to 1 element");
		return parseFromToken(tokens.get(0));
	}

	/**
	 * Compile expression to virtual machine code (not JVM bytecode!), which can be optimized if you want.
	 * @param exprStr Expression string to parse and compile.
	 * @return Compiled expression.
	 */
	public static VirtualMachineExpression compile(String exprStr) {
		return VirtualMachineExpression.compile(parse(exprStr));
	}

	private static void reduceAll(List<Token> tokens) {
		symbolsReduce(tokens);
		functionReduce(tokens);
		groupReduce(tokens);
		operatorsReduce(tokens);
		flatten(tokens);
	}

	private static void symbolsReduce(List<Token> tokens) {
		ListIterator<Token> iter = tokens.listIterator();
		SymbolsStack stack = null;

		while (iter.hasNext()) {
			Token token = iter.next();

			if (token instanceof SymbolToken symbol) {
				iter.remove();

				if (stack == null) {
					stack = new SymbolsStack();
					iter.add(stack);
				}

				stack.stack.add(symbol.name);
			} else if (token == DOT) {
				iter.remove();
			} else if (stack != null) {
				stack = null;
			}
		}
	}

	private static void functionReduce(List<Token> tokens) {
		// Tokens that we are looking for: Symbols Stack, PARENTHESIS_OPEN, ..., PARENTHESIS_CLOSE
		ListIterator<Token> iter = tokens.listIterator();

		while (iter.hasNext()) {
			Token token = iter.next();
			Token target = null;

			if (token instanceof SymbolToken || token instanceof SymbolsStack) {
				target = token;
				Token next = iter.next();
				if (next != PARENTHESES_OPEN) continue;

				// Seems like we are parsing function here...
				iter.remove();
				iter.previous(); iter.remove();
				FunctionToken funcToken = new FunctionToken(target);

				List<Token> currentParameter = new ArrayList<>();
				int currentParenthesisLevel = 0;

				while (iter.hasNext()) {
					token = iter.next();

					if (token == COMMA) {
						reduceAll(currentParameter);
						funcToken.parameters.add(currentParameter);
						currentParameter = new ArrayList<>();
					} else if (token == PARENTHESES_OPEN) {
						currentParameter.add(token);
						currentParenthesisLevel++;
					} else if (token == PARENTHESES_CLOSE) {
						if (currentParenthesisLevel > 0) {
							currentParameter.add(token);
							currentParenthesisLevel--;
						} else {
							reduceAll(currentParameter);
							funcToken.parameters.add(currentParameter);
							iter.remove();
							break;
						}
					} else {
						currentParameter.add(token);
					}

					iter.remove();
				}

				iter.add(funcToken);
			}
		}
	}

	private static void groupReduce(List<Token> tokens) {
		ListIterator<Token> iter = tokens.listIterator();

		while (iter.hasNext()) {
			Token token = iter.next();

			if (token == PARENTHESES_OPEN) {
				GroupToken group = new GroupToken();
				iter.remove();
				iter.add(group);

				int currentParenthesisLevel = 0;
				while (iter.hasNext()) {
					token = iter.next();

					if (token == PARENTHESES_OPEN) {
						currentParenthesisLevel++;
						group.children.add(token);
					} else if (token == PARENTHESES_CLOSE) {
						if (currentParenthesisLevel != 0) {
							currentParenthesisLevel--;
							group.children.add(token);
						} else {
							iter.remove();
							break;
						}
					} else {
						group.children.add(token);
					}

					iter.remove();
				}

				reduceAll(group.children);
			}
		}
	}

	private static void operatorsReduce(List<Token> tokens) {
		for (Operator[] operations : Operator.ORDER_OF_OPERATIONS) operatorsReduce(tokens, operations);
	}

	private static void operatorsReduce(List<Token> tokens, Operator[] operations) {
		ListIterator<Token> iter = tokens.listIterator();
		if (!iter.hasNext()) return;
		iter.next();

		while (iter.hasNext()) {
			Token currentToken = iter.next();

			if (currentToken instanceof OperatorToken op) {
				boolean isIncluded = false;

				for (Operator allowedOperation : operations) {
					if (allowedOperation == op.operator) {
						isIncluded = true;
						break;
					}
				}

				if (!isIncluded) {
					iter.next();
					continue;
				}

				iter.previous();
				Token prevToken = iter.previous(); iter.remove();
				iter.next(); iter.remove();
				Token nextToken = iter.next(); iter.remove();

				ProcessedOperatorToken processed = new ProcessedOperatorToken(prevToken, op.operator, nextToken);
				iter.add(processed);
			}
		}
	}

	private static Token flatten(Token token) {
		if (token instanceof GroupToken group) {
			if (group.children.size() == 1) return group.children.get(0);
			return token;
		}

		if (token instanceof FunctionToken func) {
			func.target = flatten(func.target);

			for (List<Token> param : func.parameters) {
				for (int i = 0; i < param.size(); i++) param.set(i, flatten(param.get(i)));
			}
			return func;
		}

		if (token instanceof ProcessedOperatorToken op) {
			op.a = flatten(op.a);
			op.b = flatten(op.b);
			return op;
		}

		return token;
	}

	private static void flatten(List<Token> tokens) {
		for (int i = 0; i < tokens.size(); i++) {
			tokens.set(i, flatten(tokens.get(i)));
		}
	}

	private static Expression parseFromToken(Token token) {
		if (token instanceof ProcessedOperatorToken operator) return parseFromToken(operator);
		else if (token instanceof ConstantToken constant) return new Constant(constant.val);
		else if (token instanceof FunctionToken func) return parseFromToken(func);
		else if (token instanceof SymbolsStack symbols) return parseFromToken(symbols);
		throw new RuntimeException("Cannot parse token with type " + token.getClass().getCanonicalName());
	}

	private static OperatorExpression parseFromToken(ProcessedOperatorToken token) {
		return new OperatorExpression(parseFromToken(token.a), token.operator, parseFromToken(token.b));
	}

	private static FunctionExpression parseFromToken(FunctionToken token) {
		Expression[] parameters = new Expression[token.parameters.size()];
		for (int i = 0; i < parameters.length; i++) parameters[i] = parseFromToken(token.parameters.get(i).get(0));
		return new FunctionExpression(parseFromToken(token.target), parameters);
	}

	private static Symbol parseFromToken(SymbolsStack token) {
		Symbol expr = new Symbol(new Constant(null), token.stack.get(0));
		for (int i = 1; i < token.stack.size(); i++) expr = new Symbol(expr, token.stack.get(i));
		return expr;
	}
}
