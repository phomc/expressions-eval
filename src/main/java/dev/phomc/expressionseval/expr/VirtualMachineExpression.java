package dev.phomc.expressionseval.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import dev.phomc.expressionseval.EvalContext;
import dev.phomc.expressionseval.Operator;
import dev.phomc.expressionseval.VariablesInterface;

/**
 * Expression implemented as virtual machine to (potentially) reduce RAM usage and CPU cycles with
 * big expressions.
 * @author nahkd
 *
 */
public class VirtualMachineExpression extends Expression {
	// TODO: Expression optimizer

	public final List<Instruction> code = new ArrayList<>();

	@Override
	public Object eval(EvalContext ctx, VariablesInterface variables) {
		Stack<Object> stack = new Stack<>();
		for (Instruction instr : code) instr.execute(ctx, variables, stack);
		return stack.empty()? null : stack.pop();
	}

	public static VirtualMachineExpression compile(Expression expr) {
		VirtualMachineExpression vm = new VirtualMachineExpression();
		compile(expr, vm);
		return vm;
	}

	private static void compile(Expression expr, VirtualMachineExpression vm) {
		if (expr instanceof Constant c) {
			vm.code.add(new PushConstInstruction(c.val));
			return;
		}

		if (expr instanceof OperatorExpression op) {
			compile(op.a, vm);
			compile(op.b, vm);
			vm.code.add(new ArithmeticInstruction(op.operator));
			return;
		}

		if (expr instanceof Symbol sym) {
			compile(sym.target, vm);
			vm.code.add(new PushConstInstruction(sym.name));
			vm.code.add(SymbolInstruction.INSTR);
		}

		if (expr instanceof FunctionExpression func) {
			// push function, push param, push param..., push length, call
			compile(func.target, vm); // push function
			for (Expression paramExpr : func.parameters) compile(paramExpr, vm);
			vm.code.add(new PushConstInstruction(func.parameters.length));
			vm.code.add(FunctionInstruction.INSTR);
		}
	}

	// Instructions
	protected static abstract class Instruction {
		// TODO: implement loading expression from bytes
		// public abstract int getInstructionId();
		// public abstract void writeToStream(DataOutput stream) throws IOException;
		public abstract void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack);
	}

	protected static class PushConstInstruction extends Instruction {
		public final Object val;

		public PushConstInstruction(Object val) {
			this.val = val;
		}

		@Override
		public void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack) {
			stack.push(val);
		}
	}

	protected static class PushVariableInstruction extends Instruction {
		public final String name;

		public PushVariableInstruction(String name) {
			this.name = name;
		}

		@Override
		public void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack) {
			stack.push(variables.get(name));
		}
	}

	protected static class ArithmeticInstruction extends Instruction {
		public final Operator operator;

		public ArithmeticInstruction(Operator operator) {
			this.operator = operator;
		}

		@Override
		public void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack) {
			Object b = stack.pop();
			Object a = stack.pop();
			stack.push(ctx.applyOperator(a, operator, b));
		}
	}

	protected static class SymbolInstruction extends Instruction {
		public static final SymbolInstruction INSTR = new SymbolInstruction();

		@Override
		public void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack) {
			String symbol = (String) stack.pop();
			Object target = stack.pop();
			stack.push(ctx.propertyOf(target, symbol, variables));
		}
	}

	protected static class FunctionInstruction extends Instruction {
		public static final FunctionInstruction INSTR = new FunctionInstruction();

		@Override
		public void execute(EvalContext ctx, VariablesInterface variables, Stack<Object> stack) {
			int length = (int) stack.pop();
			Object[] parameters = new Object[length];
			for (int i = length - 1; i >= 0; i--) parameters[i] = stack.pop();
			Object function = stack.pop();
			stack.push(ctx.functionCall(function, parameters));
		}
	}
}
