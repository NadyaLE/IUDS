package com.digdes.school;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorIUDS {
    private static String insert = "['‘][A-z]*['’]\\s*=\\s*(?:['‘][.\\W\\dА-я]*['’]|(true|false)|\\d+(\\.\\d+)?)\\s*\\,?";
    private static String iudsRules = "(?i)(insert|update)\\s+values\\s+|(select|delete)\\s+(where)?";
    private static String validation = "['‘][A-я\\d\\s]*['’]\\s*(?:[<>!]?=|[<>]|(like|ilike))\\s*(?:['‘][A-я\\d\\s%]*['’]|true|false|\\d+(\\.\\d+)?)";
    private static String valid = "((?i)\\s*,|\\s+(and|or)\\s+|\\s+where\\s+)";

    public static String checkValidityAndGetData(String str) throws Exception {
        if(str.endsWith(",")){
            throw new Exception("The comma must be followed by an expression!");
        }
        str = str.replaceAll(iudsRules,"").trim();
        System.out.println(str);
        if (str.matches("\\s*")){
            throw new Exception("There must be some data in the request!");
        }
        String check = str.replaceAll(validation + valid,"");
        System.out.println(check);
        Pattern pattern = Pattern.compile(validation);
        Matcher matcher = pattern.matcher(check);
        if(matcher.results().count() != 1){
            throw new Exception("Missing comma between: " + matcher.reset()
                    .results().map(MatchResult::group)
                    .collect(Collectors.joining(" and ")));
        }
        check = check.replaceAll(validation,"");
        if(!check.matches("\\s*")){
            throw new Exception("Syntax error: " + check);
        }
        return str;
    }

    public static Map<String,Object> insertValues(String str) throws Exception {
        str = checkValidityAndGetData(str);
        String string = str.replaceAll("['‘][A-я\\d\\s%]*['’]", "");
        Pattern pattern = Pattern.compile("(?i)[<>!]+=?|\\s+(like|ilike|and|or|where)\\s+");
        Matcher matcher = pattern.matcher(string);
        if(matcher.find()){
            throw new Exception("Invalid statements found in query for INSERT command: " + matcher.reset()
                    .results().map(MatchResult::group)
                    .collect(Collectors.joining(", ")));
        }
        String[] data = str.split("\\s*,\\s*");
        return null;
    }

    public static void main(String[] args) {
        try {
            ValidatorIUDS.checkValidityAndGetData("    INSErT    VALUEs 'last Name' = 'Fedorov as' , 'id'=3, 'age' = 40, 'active'= false");
            ValidatorIUDS.checkValidityAndGetData("select 'age'>=30 and 'lastName' ilike '%п%'");
            ValidatorIUDS.checkValidityAndGetData("UPDATE VALUES 'active'=true wHerE 'active'=false");
            //ValidatorIUDS.checkValidityAndGetData("UPATE VALUES ");
           // insertValues("UPDATE VALUES ‘active’=true where ‘active’=false");
           // insertValues("select ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            insertValues("INSErTVALUEs 'last Name' = 'Fedorov_and','id'=3.4,'age' = 40,'active'= false");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
