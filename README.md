# ExpressionsEval
Evaluate expressions with your custom config. Originally made for Tensai VFX engine.

## Using ExpressionsEval
```xml
<!-- pom.xml -->
<dependency>
    <groupId>dev.phomc</groupId>
    <artifactId>expressions-eval</artifactId>
    <version>0.0.1-SNAPSHOT</versi
</dependency>
```

```java
Expression expr = Expression.parse("(1 + 2 * 3 + 4) / 5");
expr.eval(new SimpleEvalContext() {}, null); // The expected result is 2 in integer type

Expression expr2 = Expression.parse("x * 1.2");
expr.eval(new SimpleEvalContext() {}, VariablesInterface.of(Map.of("x", 123.456)));
```

### Using virtual machine to call ``eval()`` repeatedly
If you are willing to call ``eval()`` multiple times, it is a good idea to compile it to virtual machine code with ``Expression.compile()`` or ``VirtualMachineExpression.compile()`` and use ``eval()`` on it:

```java
Expression expr = Expression.compile("x * 1.2");

// ...
expr.eval(evalContext, variables);
```

### Custom evaluate context
You can create your own evaluate context to accepts custom types and provide global variables to all expressions:

```java
public class MyEvalContext implements SimpleEvalContext {
    @Override
    public Object applyOperator(Object a, Operator op, Object b) {
        if (a instanceof Vector3 va && b instanceof Vector3 vb) {
            return switch (op) {
            case ADD -> va.add(vb);
            case SUBTRACT -> va.sub(vb);
            case MULTIPLY -> va.mul(vb);
            case DIVIDE -> va.div(vb);
            default -> null;
            };
        }

        return super.applyOperator();
    }
}
```
