package com.digdes.school;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorIUDS {
    private static String insert = "['‘][A-z]*['’]\\s*=\\s*(?:['‘][.\\W\\dА-я]*['’]|(true|false)|\\d+(\\.\\d+)?)\\s*\\,?";


    public static boolean checkStringValidity(String str){

       // str = str.replaceAll(JavaSchoolStarter.insert,"");
        Pattern pattern = Pattern.compile(insert);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
       // System.out.println(str.split());
        return true;
    }

    public static void main(String[] args) {
        ValidatorIUDS.checkStringValidity("INSERT VALUES ‘lastName’ = ‘’ , ‘id’=3, ‘age’ = 40., ‘active’=false");
    }
}
