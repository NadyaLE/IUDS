package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.digdes.school.ValidatorIUDS.*;
import static java.lang.System.out;

public class JavaSchoolStarter {
    private final List<Map<String, Object>> collection = new ArrayList<>();
    private final Map<String, Class> fieldsAndTypes;

    public JavaSchoolStarter(Map<String, Class> fieldsAndTypes) {
        this.fieldsAndTypes = fieldsAndTypes;
    }

    public JavaSchoolStarter() {
        fieldsAndTypes = new LinkedHashMap<>() {{
            put("id", Long.class);
            put("lastName", String.class);
            put("age", Long.class);
            put("cost", Double.class);
            put("active", Boolean.class);
        }};
    }

    public List<Map<String, Object>> execute(String request) throws Exception {
        Matcher matcher = Pattern.compile(iudsRules).matcher(request);
        if (matcher.find()) {
            String command = matcher.group().replaceAll("(?i)where", "")
                    .toUpperCase().strip().replaceAll("\\s{2,}", " ");
            return switch (command) {
                case "INSERT VALUES" -> insertMap(getData(checkValidity(request)[0]),
                        setKeysInMap(fieldsAndTypes.keySet(), new LinkedHashMap<>()));
                case "UPDATE VALUES", "SELECT" -> selectByCondition(getDataAndCondition(request));
                case "DELETE" -> {
                    List<Map<String, Object>> result = selectByCondition(getDataAndCondition(request));
                    collection.removeAll(result);
                    yield result;
                }
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };
        } else {
            throw new IllegalArgumentException("Command not found!");
        }
    }

    public List<Map<String, Object>> insertMap(Map<String, String> dataString, Map<String, Object> map) {
        if (fillMap(dataString, map).values().stream().allMatch(Objects::isNull)) return null;
        collection.add(map);
        return List.of(map);
    }

    public List<Map<String, Object>> selectByCondition(Map<String, Map<String, String>> stringMap) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>(collection);
        if (stringMap.containsKey(null) && stringMap.containsValue(null)) throw new Exception("Key and value - null!");
        if (stringMap.containsKey(null)) {
            for (Map<String, String> data : stringMap.values()) resultList.forEach(map -> fillMap(data, map));
            return resultList;
        }
        if (stringMap.containsValue(null)) {
            return selectMap(String.join("", stringMap.keySet()));
        }
        resultList = selectMap(String.join("", stringMap.keySet()));
        for (Map<String, String> data : stringMap.values()) resultList.forEach(map -> fillMap(data, map));
        return resultList;
    }

    public List<Map<String, Object>> selectMap(String condition) throws Exception {
        return selectMap(fieldsAndTypes, collection, condition);
    }

    public static List<Map<String, Object>> selectMap(Map<String, Class> fieldsAndTypes, List<Map<String, Object>> collection, String condition) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>(collection);
        if (condition.isBlank()) {
            return result;
        }
        String[] parts = splitCondition(condition);
        if (parts.length == 1)
            return result.stream().filter(e -> filterMap(fieldsAndTypes, e, parts[0])).collect(Collectors.toList());

        for (String part : parts)
            condition = condition.replace(part, "");

        List<Map<String, Object>> intermediate = new ArrayList<>(collection);
        Matcher matcher = Pattern.compile("(?<=\\()\\s*" + expression + "(?=\\s*\\))").matcher(parts[0]);
        while (matcher.find()) {
            result = orAndMatcher(fieldsAndTypes, collection, result, matcher.group());
            System.out.println(parts[0] + " - " + matcher.group());
            System.out.println(result);
            parts[0] = parts[0].replace(matcher.group(), "");
            matcher.reset(parts[0]);
        }
        while (matcher.reset(parts[1]).find()) {
            intermediate = orAndMatcher(fieldsAndTypes, collection, intermediate, matcher.group());
            System.out.println(parts[1] + " - " + matcher.group());
            System.out.println(intermediate);
            parts[1] = parts[1].replace(matcher.group(), "");
        }
        result = orAndMatcher(fieldsAndTypes, intermediate, result, parts[0] + condition + parts[1]);
        return result;
    }

    public static List<Map<String, Object>> orAndMatcher(Map<String, Class> fieldsAndTypes, List<Map<String, Object>> collection,
                                                         List<Map<String, Object>> resultMatching, String shortExpr) throws Exception {
        List<Map<String, Object>> intermediate = new ArrayList<>(collection);
        String[] conditions = shortExpr.strip().split(or_and);
        for (String data : conditions)
            shortExpr = shortExpr.replace(data, "");
        Condition enumOrAnd = findOperation(shortExpr);
        if (Arrays.stream(conditions).allMatch(e -> e.matches("\\(\\s*\\)"))) {
            enumOrAnd.compute(resultMatching, intermediate);
            return resultMatching;
        }
        if (Arrays.stream(conditions).noneMatch(e -> e.matches("\\(\\s*\\)"))) {
            resultMatching = resultMatching.stream().filter(e -> filterMap(fieldsAndTypes, e, conditions[0])).collect(Collectors.toList());
        }
        intermediate = intermediate.stream().filter(e -> filterMap(fieldsAndTypes, e,
                !conditions[1].matches("\\(\\s*\\)") ? conditions[1] : conditions[0])).collect(Collectors.toList());
        enumOrAnd.compute(resultMatching, intermediate);
        return resultMatching;
    }

    public static Condition findOperation(String condition) throws Exception {
        Matcher matcher = Pattern.compile("\\s*([<>!]?=|[<>]|(?i)(like|ilike|or|and)(?i))\\s*").matcher(condition.strip());
        if (!matcher.find()) throw new Exception("Condition not found!");
        String operation = matcher.group();
        return Condition.getOperationByString(operation);
    }

    public static void main(String[] args) {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        try {
            List<Map<String, Object>> col = new ArrayList<>(cl.execute("INSErT  VALUES 'LASTName' ='lull', 'aGe' = null, 'coST' = 4.56 "));
            col.addAll(cl.execute("INSErT  VAluES 'LasTName' = 'Lister', 'iD'=1, 'aGe' = 25, 'cost' = 4.20 "));
            col.addAll(cl.execute("INSErT  vaLUES 'LasTName' = 'Connor', 'iD'=2, 'aGe' = 31, 'cost' = 4.75 "));
            cl.printTable();
            out.println("\nResult update:");
            printTable(cl.execute("UPDate  VALUes 'active' = false"));
            out.println("\nResult update:");
            printTable(cl.execute("UPDate  VALUES 'LASTName' ='sos', 'id' = null where ('lastname' ilike 'l%' or 'id'!=2) and 'cost' > 4.2"));
            out.println("\nResult select(all):");
            printTable(cl.execute("SELECT"));
            out.println("\nResult delete where id=2:");
            printTable(cl.execute("DELETE where 'Id' = 2"));
//            printTable(col);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println("\n\nAfter iuds commands:");
        cl.printTable();
    }

    public static String[] splitCondition(String condition) {
        return condition.strip().startsWith("(") ?
                condition.split("(?<=\\))\\s*(?i)(or|and)(?i)\\s*(?=\\(|[^()]+\\s*$)") :
                condition.split("\\s*(?i)(or|and)(?i)\\s*", 2);
    }

    public Map<String, Object> fillMap(Map<String, String> dataString, Map<String, Object> map) {
        try {
            for (String field : dataString.keySet()) {
                String correctName = getCorrectField(field, fieldsAndTypes);
                Class cls = fieldsAndTypes.get(correctName);
                String smth = dataString.get(field);
                Object obj = null;
                if (cls == String.class && !smth.matches("(['‘].*['’]|null)")) {
                    throw new Exception("The string must be enclosed in single quotes or to be null!");
                }
                if (!smth.equals("null")) {
                    obj = cls.getConstructor(String.class).newInstance(smth.replaceAll("['‘’]", ""));
                }
                map.put(correctName, obj);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean filterMap(Map<String, Object> row, String condition) {
        return filterMap(fieldsAndTypes, row, condition);
    }

    public static boolean filterMap(Map<String, Class> fieldsAndTypes, Map<String, Object> row, String condition) {
        try {
            Condition conditionEnum = findOperation(condition);
            String[] fieldValue = condition.strip().split("\\s*" + conditionEnum.getCondition() + "\\s*");
            String correctName = getCorrectField(fieldValue[0].replaceAll("['‘’]", ""), fieldsAndTypes);
            Class cls = (((cls = fieldsAndTypes.get(correctName)) == Long.class) && fieldValue[1].contains(".")) ? Double.class : cls;
            if (cls == String.class && !fieldValue[1].matches("(['‘].*['’])"))
                throw new Exception("The string must be enclosed in single quotes!");
            Object obj = cls.getConstructor(String.class).newInstance(fieldValue[1].replaceAll("['‘’]", ""));
            return conditionEnum.compute(row.get(correctName), obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getCorrectField(String field, Map<String, Class> fieldsAndTypes) throws Exception {
        return fieldsAndTypes.keySet().stream()
                .filter(e -> e.compareToIgnoreCase(field.strip()) == 0).findAny()
                .orElseThrow(() -> new Exception("Invalid field found in request!"));
    }

    public static <K, V> Map<K, V> setKeysInMap(Collection<K> keys, Map<K, V> map) {
        keys.forEach(key -> map.put(key, null));
        return map;
    }

    public void printTable() {
        printTable(collection);
    }

    public static void printTable(List<Map<String, Object>> collection) {
        if (collection == null || collection.isEmpty()) return;
        for (String key : collection.get(0).keySet()) out.printf("%-15s", key);
        out.println();
        for (Map<String, Object> map : collection) {
            for (Object value : map.values()) {
                out.printf("%-15s", value == null ? "" : value);
            }
            out.println();
        }
    }
}



