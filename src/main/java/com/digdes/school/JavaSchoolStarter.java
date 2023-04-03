package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.digdes.school.ValidatorIUDS.*;
import static java.lang.System.out;

public class JavaSchoolStarter {
    private final List<Map<String, Object>> collection = new ArrayList<>();
    private final Map<String, Class> fieldsAndTypes;

    public JavaSchoolStarter() {
        fieldsAndTypes = Map.of("id", Long.class, "lastName", String.class,
                "age", Long.class, "cost", Double.class, "active", Boolean.class);
    }

    public JavaSchoolStarter(Map<String, Class> fieldsAndTypes) {
        this.fieldsAndTypes = fieldsAndTypes;
    }

    public List<Map<String, Object>> execute(String request) {

        return new ArrayList<>();
    }

    public static void main(String[] args) {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        try {
            cl.addMap("INSErT  VALUES 'LASTName' = 'null', 'iD'=null, 'aGe' = null ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        cl.printTable();
    }

    public static <K, V> Map<K, V> setKeysInMap(Collection<K> keys, Map<K, V> map) {
        for (K key : keys) {
            map.put(key, null);
        }
        return map;
    }

    public Map<String, Object> addMap(String iudsCommand) throws Exception {
        Map<String, Object> map = setKeysInMap(fieldsAndTypes.keySet(), new HashMap<>());
        Pattern pattern = Pattern.compile(iudsRules);
        Matcher matcher = pattern.matcher(iudsCommand);
        if (matcher.find()) {
            String command = matcher.group().replaceAll("(?i)where", "")
                    .toUpperCase().trim().replaceAll("\\s{2,}", " ");
            switch (command) {
                case "INSERT VALUES" -> fillMap(getDataINSERT(iudsCommand), map);
                case "UPDATE VALUES", "SELECT", "DELETE" -> getDataAndConditionUDS(iudsCommand);
                default -> throw new IllegalStateException("Unexpected value: " + command);
            }
        }else{
            throw new IllegalArgumentException("Command not found!");
        }
        map.forEach((k,v) -> out.println(k + " - " + (v==null? "": v + "   " + v.getClass().getSimpleName())));

        return null;
    }

    public List<Map<String, Object>> fillMap(Map<String, String> stringMap, Map<String, Object> map) {
        try {
            for (String field : stringMap.keySet()) {
                String correctName = map.keySet().stream().filter(e -> e.compareToIgnoreCase(field) == 0).findAny()
                        .orElseThrow(() -> new Exception("Invalid field found in request!"));
                Class o = fieldsAndTypes.get(correctName);
                String smth = stringMap.get(field);
                Object obj = null;
                if (!smth.equals("null")) {
                    obj = o.getConstructor(String.class).newInstance(smth.replaceAll("['‘’]", ""));
                }
                map.put(correctName,obj);
            }
            if (map.values().stream().anyMatch(Objects::nonNull)) {
                out.println("Has nonNull!");
                collection.add(map);
                return collection;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void printTable() {
        for (String key : fieldsAndTypes.keySet()) {
            out.printf("%-15s", key);
        }
        out.println();
        for (Map<String, Object> map : collection) {
            for (Object value : map.values()) {
                out.printf("%-15s", value);
            }
            out.println();
        }
    }
}

