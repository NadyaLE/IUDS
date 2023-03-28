package com.digdes.school;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        try {
            List<Map<String,Object>> result1 = starter.execute("");
            List<Map<String,Object>> result2 = starter.execute("");
            List<Map<String,Object>> result3 = starter.execute("");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
