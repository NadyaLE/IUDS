package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                case "UPDATE VALUES", "SELECT", "DELETE" -> selectByCondition(getDataAndCondition(request));
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };
        } else {
            throw new IllegalArgumentException("Command not found!");
        }
    }

    public List<Map<String, Object>> selectByCondition(Map<String, Map<String, String>> stringMap) {
        List<Map<String, Object>> resultList = new ArrayList<>(collection);
        if (stringMap.containsKey(null)) {
            stringMap.values().forEach(data -> resultList.forEach(map -> fillMap(data, map)));
            return resultList;
        }
        if (stringMap.containsValue(null)) {
            return selectMap(String.join("", stringMap.keySet()));
        }
        return null;
    }

    public List<Map<String, Object>> selectMap(String condition) {
        List<Map<String, Object>> result = new ArrayList<>(collection);
        String[] parts = splitCondition(condition);
        if (parts.length == 1) {
           return result.stream().filter(e -> filterMap(e,parts[0])).toList();
        }
        else {
//            Matcher matcher = Pattern.compile("(?<=\\()\\s*" + expression + "(?=\\s*\\))").matcher(parts[0]);
//            while (matcher.find()) {
//                System.out.println(checkExpr + " - " + matcher.group());
//                checkExpr = checkExpr.replace(matcher.group(), "");
//                matcher.reset(checkExpr);
//            }
        }
        return null;
    }

    public boolean filterMap(Map<String, Object> row, String condition) {
        try {
            Matcher matcher = Pattern.compile("\\s*([<>!]?=|[<>]|(?i)(like|ilike)(?i))\\s*").matcher(condition);
            if (!matcher.find()) throw new Exception("Condition not found!");
            String operation = matcher.group();
            String[] fieldValue = condition.split(matcher.group());
            String correctName = getCorrectField(fieldValue[0].replaceAll("['‘’]", ""), fieldsAndTypes);
            Class cls = ((cls = fieldsAndTypes.get(correctName)) == Long.class) ? Double.class : cls;
            if (cls == String.class && !fieldValue[1].matches("(['‘].*['’])"))
                throw new Exception("The string must be enclosed in single quotes!");
            Object obj = cls.getConstructor(String.class).newInstance(fieldValue[1].replaceAll("['‘’]", ""));
            return Condition.getOperationByString(operation.strip()).compute(row.get(correctName), obj);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static String getCorrectField(String field, Map<String, Class> fieldsAndTypes) throws Exception {
        return fieldsAndTypes.keySet().stream()
                .filter(e -> e.compareToIgnoreCase(field) == 0).findAny()
                .orElseThrow(() -> new Exception("Invalid field found in request!"));
    }

    public static void main(String[] args) {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        try {
            List<Map<String, Object>> col = new ArrayList<>(cl.execute("INSErT  VALUES 'LASTName' ='lull', 'iD'=null, 'aGe' = null, 'coST' = 4.56 "));
           col.addAll(cl.execute("INSErT  VALUES 'LasTName' = 'Lister', 'iD'=4, 'aGe' = 25, 'cost' = 4.55 "));
//            printTable(
//                    cl.execute("UPDate  VALUES 'LASTName' ='sos', 'aGe' = null "),
//                    cl.fieldsAndTypes);
      //      cl.execute("select where ‘id’=''");
            //   cl.execute("select where ((‘id’=''or('age'=null and'cost' < 4)) and ''='')and((‘active’=false or'lastName'like'test%')and'cost'>6 )");
            //  cl.filterMap("((‘active’=false or'lastName'like'test%')and'cost'>6 )and''=''");
      //      cl.filterMap(Map.of("iD", 4), "'iD' <=4");
            printTable(cl.selectMap("'age' != 4.56"),cl.fieldsAndTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    //    cl.printTable();
    }

    public String[] splitCondition(String condition) {
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

    public List<Map<String, Object>> insertMap(Map<String, String> dataString, Map<String, Object> map) {
        if (fillMap(dataString, map).values().stream().allMatch(Objects::isNull)) return null;
        collection.add(map);
        return List.of(map);
    }

    public static <K, V> Map<K, V> setKeysInMap(Collection<K> keys, Map<K, V> map) {
        keys.forEach(key -> map.put(key, null));
        return map;
    }

    public void printTable() {
        printTable(collection, fieldsAndTypes);
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
}



