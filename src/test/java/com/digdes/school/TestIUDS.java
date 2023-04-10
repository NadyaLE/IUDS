package com.digdes.school;

import org.testng.annotations.Test;

import java.util.*;

import static com.digdes.school.JavaSchoolStarter.printTable;
import static com.digdes.school.JavaSchoolStarter.selectMap;

public class TestIUDS {
    private final Map<String, Class> fieldsAndTypes = new LinkedHashMap<>() {{
        put("id", Long.class);
        put("lastName", String.class);
        put("age", Long.class);
        put("cost", Double.class);
        put("active", Boolean.class);
    }};

    @Test
    public void conditionAssertsShouldAnswerWithTrue()
    {
        assert Condition.EQUAL.compute("5","5");
        assert Condition.EQUAL.compute(false,false);
        assert Condition.NOT_EQUAL.compute(5.0,false);
        assert Condition.NOT_EQUAL.compute(null,false);
        assert Condition.MORE.compute(Double.valueOf(5.6),Long.valueOf(5));
        assert Condition.MORE_OR_EQUEAL.compute(Long.valueOf(5),Double.valueOf(5));
        assert Condition.LESS_OR_EQUEAL.compute(Double.valueOf(4.999),Long.valueOf(5));
        assert Condition.LIKE.compute("testAD","test%");
        assert Condition.LIKE.compute("adtestAD","%test%");
        assert Condition.LIKE.compute("test","test");
        assert Condition.ILIKE.compute("tESt","test");
        assert Condition.ILIKE.compute("ADtESt","%test");

        assert !Condition.EQUAL.compute(Double.valueOf(5),"5");
        assert !Condition.EQUAL.compute(Long.valueOf(5),Double.valueOf(5));
        assert !Condition.NOT_EQUAL.compute(false,false);
        assert !Condition.EQUAL.compute(5.0,"5.0");
        assert !Condition.LIKE.compute("AtestAD","test%");
        assert !Condition.LIKE.compute("tESt","test");
        assert !Condition.ILIKE.compute("ADtEStAD","%test");
    }

    @Test
    public void orAndTest() throws Exception {
        List<Map<String, Object>> col = new ArrayList<>();
        col.add(new LinkedHashMap<>(){{put("id", 1);put("lastName", "Петров");put("age", 30);put("cost", 5.4);put("active", true);}});
        col.add(new LinkedHashMap<>(){{put("id", 2);put("lastName", "Иванов");put("age", 25);put("cost", 4.3);put("active", false);}});
        col.add(new LinkedHashMap<>(){{put("id", 3);put("lastName", "Федоров");put("age", 20);put("cost", null);put("active", false);}});
        col.add(new LinkedHashMap<>(){{put("id", 4);put("lastName", "павлов");put("age", 40);put("cost", 4.8);put("active", false);}});

        List<Map<String, Object>> col2 = new ArrayList<>(col);
        col2.add(new LinkedHashMap<>(){{put("id", null);put("lastName", "lull");put("age", null);put("cost", 4.56);
            put("active", null);
        }});
        System.out.println("Bванов".matches("(?i)(b.*)"));
        List<Map<String, Object>> sel1 = selectMap(fieldsAndTypes,col,
                "('AGE' >= 30 and 'coST'<4.5) or (('age' >= 26 or 'lasTNAME' ilike 'и%') and 'ACTIVE' = false)");
                // "'age' >= 26  or ('AGE' >= 30 and 'coST'<4.5)");
                //"('AGE' >= 30 and 'coST'<4.5)  or 'age' >= 26");

     //   assert Objects.requireNonNull(sel1).contains(col.get(3));
      //  List<Map<String, Object>> sel2 = selectMap(fieldsAndTypes,col2,"'cost' != 0");

      //  Condition.getOperationByString("  and ").compute(sel1, sel2);

        printTable(sel1);
    }
}
