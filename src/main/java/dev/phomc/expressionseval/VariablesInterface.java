package dev.phomc.expressionseval;

import java.util.Map;

@FunctionalInterface
public interface VariablesInterface {
	Object get(String varName);

	static VariablesInterface of(Map<String, Object> map) {
		return map::get;
	}
}
