package com.digdes.school;

import org.testng.annotations.Test;

public class TestIUDS {
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
}
