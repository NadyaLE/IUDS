package com.digdes.school;

import java.util.*;

public class JavaSchoolStarter{
    private final List<Map<String, Object>> collection = new ArrayList<>();
    public Map<String,Class> fieldsAndTypes;
    private String regex = "'[a-zA-z]+' *= *[a-zA-Z\\d]+"; // 'active' = false
    public static String insert = "['‘][A-z]*['’]\\s*=\\s*(?:['‘][.\\W\\dА-я]*['’]|(true|false)|\\d+(\\.\\d+)?)\\s*\\,?"; // 'active' = false

    public JavaSchoolStarter() {
        fieldsAndTypes = new HashMap<>();
        fieldsAndTypes.put("id", Long.class);
        fieldsAndTypes.put("lastName", String.class);
        fieldsAndTypes.put("age", Long.class);
        fieldsAndTypes.put("cost", Double.class);
        fieldsAndTypes.put("active", Boolean.class);
    }

    public JavaSchoolStarter(Map<String,Class> fieldsAndTypes) {
        this.fieldsAndTypes = fieldsAndTypes;
    }

    public List<Map<String, Object>> execute(String request) {

        return new ArrayList<>();
    }

    public static void main(String[] args) {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        cl.addMap();
        cl.printTable();
    }

    public static <K, V> Map<K, V> setKeysInMap(Collection<K> keys, Map<K, V> map) {
        for (K key : keys) {
            map.put(key, null);
        }
        return map;
    }

    public void addMap(){
        Map<String,Object> map = setKeysInMap(fieldsAndTypes.keySet(), new HashMap<>());

        collection.add(map);
        map.put("cost", 45);
        map.put("active", true);
        collection.get(0).put("id", 15);
        collection.get(0).put("lastName", "Suho");
        collection.get(0).put("age", 23);
        this.printTable();
        collection.add(setKeysInMap(fieldsAndTypes.keySet(),new HashMap<>()));
        collection.get(1).put("cost", 100);
        if(collection.get(1).values().stream().anyMatch(Objects::nonNull)){
            System.out.println("Has nonNull!");
        }
    }

    public void printTable() {
        for (String key : fieldsAndTypes.keySet()) {
            System.out.printf("%-15s", key);
        }System.out.println();
        for (Map<String, Object> map : collection) {
            for (Object value : map.values()) {
                System.out.printf("%-15s", value);
            }System.out.println();
        }
    }


}

