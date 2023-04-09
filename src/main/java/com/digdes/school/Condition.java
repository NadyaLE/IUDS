package com.digdes.school;

import java.util.Arrays;
import java.util.function.BiFunction;

public enum Condition {
    EQUAL("=", (a, b) -> a != null && a.equals(b)),
    NOT_EQUAL("!=", (a, b) -> a == null || !a.equals(b)),
    LIKE("like", (a, b) -> a != null && ((String) a).matches(((String) b).replaceAll("%", ".*"))),
    ILIKE("ilike", (a, b) -> a != null &&((String) a).matches("(?i)" + ((String) b).replaceAll("%", ".*"))),
    MORE(">", (a, b) -> a != null &&((Number) a).doubleValue() > ((Number) b).doubleValue()),
    LESS("<", (a, b) -> a != null &&((Number) a).doubleValue() < ((Number) b).doubleValue()),
    MORE_OR_EQUEAL(">=", (a, b) -> a != null &&((Number) a).doubleValue() >= ((Number) b).doubleValue()),
    LESS_OR_EQUEAL("<=", (a, b) -> a != null &&((Number) a).doubleValue() <= ((Number) b).doubleValue());

    private final String conditionStr;
    private final BiFunction<Object, Object, Boolean> operation;

    Condition(String str, BiFunction<Object, Object, Boolean> operation) {
        this.conditionStr = str;
        this.operation = operation;
    }

    public Boolean compute(Object a, Object b) {
        return operation.apply(a, b);
    }

    public static Condition getOperationByString(String str) throws Exception {
        return Arrays.stream(Condition.values()).filter(e -> e.conditionStr.compareToIgnoreCase(str) == 0)
                .findAny().orElseThrow(() -> new Exception("Operation not found!"));
    }
}
