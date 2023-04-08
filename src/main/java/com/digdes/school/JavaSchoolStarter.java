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
                case "INSERT VALUES" -> insertMap(getDataINSERT(request),
                        setKeysInMap(fieldsAndTypes.keySet(), new LinkedHashMap<>()));
                case "UPDATE VALUES", "SELECT", "DELETE" -> selectByCondition(getDataAndConditionUDS(request));
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };
        } else {
            throw new IllegalArgumentException("Command not found!");
        }
    }

    public List<Map<String, Object>> selectByCondition(Map<String, Map<String, String>> stringMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (stringMap.containsKey(null)) {
            for (Map<String, String> data : stringMap.values()) {
                resultList = collection.stream().filter(map -> map == fillMap(data, map)).collect(Collectors.toList());
            }
            return resultList;
        }
        if (stringMap.containsValue(null)) {
            return filterMap(String.join("", stringMap.keySet()));
        }
        return null;
    }

    public List<Map<String, Object>> filterMap(String condition) {

        return null;
    }

    public String[] splitCondition(String condition) {
        String[] parts;
        parts = condition.strip().startsWith("(") ?
                condition.split("(?<=\\))\\s*(?i)(or|and)(?i)\\s*(?=\\(|[^()]+\\s*$)") :
                condition.split("\\s*(?i)(or|and)(?i)\\s*", 2);
        return parts;
    }

    public List<Map<String, Object>> insertMap(Map<String, String> dataString, Map<String, Object> map) {
        map = fillMap(dataString, map);
        if (map.values().stream().anyMatch(Objects::nonNull)) {
            collection.add(map);
        }
        return List.of(map);
    }

    public Map<String, Object> fillMap(Map<String, String> dataString, Map<String, Object> map) {
        try {
            for (String field : dataString.keySet()) {
                String correctName = map.keySet().stream().filter(e -> e.compareToIgnoreCase(field) == 0).findAny()
                        .orElseThrow(() -> new Exception("Invalid field found in request!"));
                Class o = fieldsAndTypes.get(correctName);
                String smth = dataString.get(field);
                Object obj = null;
                if (o == String.class && !smth.matches("(['‘].*['’]|null)")) {
                    throw new Exception("The string must be enclosed in single quotes or null!");
                }
                if (!smth.equals("null")) {
                    obj = o.getConstructor(String.class).newInstance(smth.replaceAll("['‘’]", ""));
                }
                map.put(correctName, obj);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printTable() {
        printTable(collection, fieldsAndTypes);
    }

    public static <K, V> Map<K, V> setKeysInMap(Collection<K> keys, Map<K, V> map) {
        for (K key : keys) {
            map.put(key, null);
        }
        return map;
    }

    public static void printTable(List<Map<String, Object>> collection, Map<String, Class> fieldsAndTypes) {
        for (String key : fieldsAndTypes.keySet()) out.printf("%-15s", key);
        out.println();
        for (Map<String, Object> map : collection) {
            for (Object value : map.values()) {
                out.printf("%-15s", value == null ? "" : value);
            }
            out.println();
        }
    }

    public static void main(String[] args) {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        try {
            printTable(cl.execute("INSErT  VALUES 'LASTName' ='null', 'iD'=null, 'aGe' = null "), cl.fieldsAndTypes);
            cl.execute("INSErT  VALUES 'LASTName' = 'Lister', 'iD'=4, 'aGe' = 25 ");
            printTable(
                    cl.execute("UPDate  VALUES 'LASTName' ='sos', 'iD'=null, 'aGe' = null "),
                    cl.fieldsAndTypes);
            cl.execute("select where ‘id’=''or 'age'=null");
            cl.execute("select where ((‘id’=''or('age'=null and'cost' < 4)) and ''='')and((‘active’=false or'lastName'like'test%')and'cost'>6 )");
            cl.filterMap("((‘active’=false or'lastName'like'test%')and'cost'>6 )and''=''");
        } catch (Exception e) {
            e.printStackTrace();
        }
        cl.printTable();
    }
}

