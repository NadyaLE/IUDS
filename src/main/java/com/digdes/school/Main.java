/*
 * Copyright (c) 2023. NadyaLE
 */
package com.digdes.school;

import java.util.List;
import java.util.Map;

import static com.digdes.school.JavaSchoolStarter.printTable;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        try {
            starter.execute("INSERT VALUES 'lastName' = 'Петров' , 'id'=1, 'age'=30,'coST' = 5.4, 'active'=true");
            starter.execute("insert values 'lastName' = 'Иванов' , 'id'=2, 'age'=25,'coST' = 4.3, 'active'=false");

            //Вставка строки в коллекцию
            System.out.println("INSERT result");
            List<Map<String, Object>> result1 = starter.execute("INserT vaLUES 'lastName' = 'Федоров' , 'id'=3, 'age'=40, 'active'=true");
            printTable(result1);
            System.out.println("\n\nUPDATE result");
            //Изменение значения которое выше записывали
            List<Map<String, Object>> result2 = starter.execute("UPDATE VALUES 'active'=false, 'cost'=10.1 where 'id'=3");
            printTable(result2);
            System.out.println("\n\nSelect(all) result");
            //Получение всех данных из коллекции
            List<Map<String, Object>> result3 = starter.execute("SELect");
            printTable(result3);
            System.out.println("\n\nDELETE active = false result");
            //Удаление элемента
            List<Map<String, Object>> result4 = starter.execute("Delete where 'active'=false");
            printTable(result4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n\nResult collection");
        starter.printTable();
    }
}
