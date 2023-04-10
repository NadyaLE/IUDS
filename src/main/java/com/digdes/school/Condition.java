package com.digdes.school;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;

public enum Condition {
    EQUAL("=", Object::equals),
    NOT_EQUAL("!=", (a, b) -> a == null || !a.equals(b)),
    LIKE("like", (a, b) -> ((String) a).matches(((String) b).replaceAll("%", ".*"))),
    ILIKE("ilike", (a, b) -> ((String) a).toLowerCase()
            .matches(((String) b).toLowerCase().replaceAll("%", ".*"))),
    MORE(">", (a, b) -> ((Number) a).doubleValue() > ((Number) b).doubleValue()),
    LESS("<", (a, b) -> ((Number) a).doubleValue() < ((Number) b).doubleValue()),
    MORE_OR_EQUEAL(">=", (a, b) -> ((Number) a).doubleValue() >= ((Number) b).doubleValue()),
    LESS_OR_EQUEAL("<=", (a, b) -> ((Number) a).doubleValue() <= ((Number) b).doubleValue()),
    AND("and", (a, b) -> ((Collection) a).retainAll((Collection<?>) b)),
    OR("or", (a, b) -> {
        ((Collection) b).removeIf(elem -> ((Collection<?>) a).contains(elem));
        return ((Collection) a).addAll((Collection<?>) b);
    });


    private final String conditionStr;
    private final BiFunction<Object, Object, Object> operation;

    public String getCondition() {
        return conditionStr;
    }

    Condition(String str, BiFunction<Object, Object, Object> operation) {
        this.conditionStr = str;
        this.operation = operation;
    }

    public Boolean compute(Object a, Object b) {
        return a == null ? this.compareTo(Condition.NOT_EQUAL) == 0 :
                (Boolean) operation.apply(a, b);
    }

    public static Condition getOperationByString(String str) throws Exception {
        return Arrays.stream(Condition.values()).filter(e -> e.conditionStr.compareToIgnoreCase(str.strip()) == 0)
                .findAny().orElseThrow(() -> new Exception("Operation not found!"));
    }
}
