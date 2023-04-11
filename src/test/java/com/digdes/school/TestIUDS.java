package com.digdes.school;

import org.junit.jupiter.api.Test;

import java.util.*;

import static com.digdes.school.JavaSchoolStarter.printTable;
import static com.digdes.school.JavaSchoolStarter.selectMap;
import static com.digdes.school.ValidatorIUDS.checkValidity;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static java.lang.System.out;

public class TestIUDS {
    private List<Map<String, Object>> col = new ArrayList<>() {{
        add(new LinkedHashMap<>() {{
            put("id", 1);
            put("lastName", "Петров");
            put("age", 30);
            put("cost", 5.4);
            put("active", true);
        }});
        add(new LinkedHashMap<>() {{
            put("id", 2);
            put("lastName", "Иванов");
            put("age", 25);
            put("cost", 4.3);
            put("active", false);
        }});
        add(new LinkedHashMap<>() {{
            put("id", 3);
            put("lastName", "Федоров");
            put("age", 20);
            put("cost", null);
            put("active", false);
        }});
        add(new LinkedHashMap<>() {{
            put("id", 4);
            put("lastName", "павлов");
            put("age", 40);
            put("cost", 4.8);
            put("active", false);
        }});
    }};

    private final Map<String, Class> fieldsAndTypes = new LinkedHashMap<>() {{
        put("id", Long.class);
        put("lastName", String.class);
        put("age", Long.class);
        put("cost", Double.class);
        put("active", Boolean.class);
    }};

    @Test
    public void conditionAssertsShouldAnswerWithTrue() {
        assert Condition.EQUAL.compute("5", "5");
        assert Condition.EQUAL.compute(false, false);
        assert Condition.NOT_EQUAL.compute(5.0, false);
        assert Condition.NOT_EQUAL.compute(null, false);
        assert Condition.MORE.compute(Double.valueOf(5.6), Long.valueOf(5));
        assert Condition.MORE_OR_EQUEAL.compute(Long.valueOf(5), Double.valueOf(5));
        assert Condition.LESS_OR_EQUEAL.compute(Double.valueOf(4.999), Long.valueOf(5));
        assert Condition.LIKE.compute("testAD", "test%");
        assert Condition.LIKE.compute("adtestAD", "%test%");
        assert Condition.LIKE.compute("test", "test");
        assert Condition.ILIKE.compute("tESt", "test");
        assert Condition.ILIKE.compute("ADtESt", "%test");

        assert !Condition.EQUAL.compute(Double.valueOf(5), "5");
        assert !Condition.EQUAL.compute(Long.valueOf(5), Double.valueOf(5));
        assert !Condition.NOT_EQUAL.compute(false, false);
        assert !Condition.EQUAL.compute(5.0, "5.0");
        assert !Condition.LIKE.compute("AtestAD", "test%");
        assert !Condition.LIKE.compute("tESt", "test");
        assert !Condition.ILIKE.compute("ADtEStAD", "%test");
        assert !"Иванов".matches("(?i)(и.*)");   //???
    }

    @Test
    public void orAndTest() throws Exception {
        System.out.println("Иванов".matches("(?i)(и.*)"));
        List<Map<String, Object>> sel1 = selectMap(fieldsAndTypes, col,
                "('AGE' >= 30 and 'coST'<4.5) or ('ACTIVE' = false and ('age' >= 26 or 'lasTNAME' ilike 'и%'))");
        // "'age' >= 26  or ('AGE' >= 30 and 'coST'<4.5)");
        //"('AGE' >= 30 and 'coST'<4.5)  or 'age' >= 26");
        // "'AGE' >= 25 and 'ID'<3");
        assert Objects.requireNonNull(sel1).contains(col.get(1));
        printTable(sel1);
    }

    @Test
    public void checkValidatorTest() throws Exception {
        checkValidity("UPDATE VALUES 'age' = 23, 'cost'=null,'active'=12 where ((‘id’=''or('age'=21 and'cost' < 4)) and ''='')and((‘active’=false or'lastName'like'test%')and'cost'>6 )");
        checkValidity("    INSErT    VALUEs 'last Name' = 'Fedorov as', 'id'=3, 'age' = null, 'active'= false  ");
        Throwable thrown = assertThrows(Exception.class,
                () -> checkValidity("select where 'age'=null and 'lastName' ilike 'рк % sr'"),
                "Expected Exception caused by 'age'=null!");
        assertNotNull(thrown.getMessage());
        assert checkValidity("UPDATE VALUES 'active'=true ")[0].equals("'active'=true");
        thrown = assertThrows(Exception.class, () -> checkValidity("Select where"),
                "Expected Exception caused by invalid command!");
        assertNotNull(thrown.getMessage());
        thrown = assertThrows(Exception.class, () -> checkValidity("Delete where"),
                "Expected Exception caused by invalid command!");
        assertNotNull(thrown.getMessage());
        thrown = assertThrows(Exception.class, () -> checkValidity("INSErT    VALUEs "),
                "\"Expected Exception caused by empty data command!");
        assertNotNull(thrown.getMessage());
        thrown = assertThrows(Exception.class, () ->
                        checkValidity("select where   'age'<false and 'lastName' ilike 'рк % sr'"),
                "Expected Exception caused by 'age'<false!");
        assertNotNull(thrown.getMessage());
        assert checkValidity("UPDATE VALUES ‘active’=true where ‘active’=false")[1].equals("‘active’=false");
    }

    @Test
    public void execiteTest() throws Exception {
        JavaSchoolStarter cl = new JavaSchoolStarter();
        cl.execute("INSErT  VALUES 'LASTName' ='litke Nadya', 'aGe' = 26, 'coST' = 4.46, 'active' = false ");
        Throwable thrown = assertThrows(Exception.class, () -> cl.execute("    INSErT    VALUEs 'last Name' = 'Федоров as', 'id'=3, 'age' = null, 'active'= false  "),
                "Expected Exception caused by field not found!");
        assertNotNull(thrown.getMessage());
        cl.execute("INSErT  VAluES 'LasTName' = 'Lister', 'iD'=1, 'aGe' = 25, 'cost' = 4.20 ");
        cl.execute("INSERT VALUES 'lastName' = 'Петров' , 'id'=1, 'age'=30,'coST' = 5.4, 'active'=true");
        cl.execute("INSErT  vaLUES 'LasTName' = 'Connor', 'iD'=2, 'aGe' = 31, 'cost' = 4.75 ");
        cl.execute("insert values 'lastName' = 'Иванов' , 'id'=2, 'age'=null,'coST' = 4.3, 'active'=false");
        cl.execute("INserT vaLUES 'lastName' = 'Федоров' , 'id'=3, 'age'=40, 'active'=true");
        cl.printTable();
        out.println("\nResult update:");
        printTable(cl.execute("UPDate  VALUes 'active' = false"));
        out.println("\nResult update:");
        printTable(cl.execute("UPDate  VALUES 'LASTName' ='changed', 'aCtIve' = true" +
                " where ('ID'!=2 and (‘id’<3 or('age'>=26 and'cost' < 4.5)))and((‘active’=false or'lastName'like'l%')and'cost'<5 )"));
        out.println("\nResult select(all):");
        printTable(cl.execute("SELECT"));
        out.println("\nResult select id=2:");
        printTable(cl.execute("SELECT where 'Id'=2"));
        out.println("\nResult delete:");
        printTable(cl.execute("DELETE"));
        out.println("\n\nAfter iuds commands:");
        cl.printTable();
    }
}
