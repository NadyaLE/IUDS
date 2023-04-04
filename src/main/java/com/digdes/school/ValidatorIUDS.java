package com.digdes.school;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorIUDS {
    public static final String iudsRules = "(?i)(insert|update)\\s+values\\s+|(select|delete)\\s+(where)?";
    public static final String rules = "\\s*(\\(\\s*\\))?\\s*(?i)(or|and)(?i)\\s*(\\(\\s*\\))?";
    public static final String or_and = "(?i)(or|and)(?i)";
    public static final String best = "['‘][A-я\\d\\s]*['’](\\s*([<>!]?=|[<>])\\s*\\d+(\\.\\d+)?|(?i)(\\s*(like|ilike)\\s*|\\s*!?=\\s*)(?i)(['‘][A-я\\d\\s%]*['’])|\\s*(?!<=|>=)!?=\\s*(true|false)|\\s*!?=\\s*null)";

    public static String checkValidity(String iudsCommand) throws Exception {
        if (iudsCommand.matches("(?i)\\s*insert\\s+values\\s*")
                || iudsCommand.matches("(?i)\\s*update\\s+values\\s*")) {
            throw new Exception("Empty INSERT|UPDATE command!");
        }
        iudsCommand = iudsCommand.replaceFirst(iudsRules, "").trim();
        String check1 = iudsCommand.replaceAll(best + "(\\s*,\\s*" + best + ")*(\\s*$|(?i)\\s*where\\s*(?i))", "");
        String check = check1.replaceAll(best + "(\\s+" + or_and + "\\s*" + best + ")?", "");
        Pattern pattern = Pattern.compile(best + "(\\s+" + or_and + "\\s*" + best + ")?");
        Matcher matcher = pattern.matcher(check);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        pattern = Pattern.compile("(?<=\\()" + rules + "(?=\\s*\\))");
        matcher = pattern.matcher(check);
        while (matcher.find()) {
            System.out.println(check + " - " + matcher.group());
            check = check.replaceAll("(?<=\\()" + rules + "(?=\\s*\\))", "");
            matcher = pattern.matcher(check);
        }
        check = check.replaceFirst(rules, " ");
        if (!check.isBlank()) {
            throw new Exception("Syntax error: " + check);
        }
        return iudsCommand;
    }

    public static Map<String, String> getDataINSERT(String insertCommand) throws Exception {
        insertCommand = checkValidity(insertCommand);
        String string = insertCommand.replaceAll("['‘][A-я\\d\\s%]*['’]", "");
        Pattern pattern = Pattern.compile("(?i)[<>!]=?|\\s+(like|ilike|and|or|where)\\s+");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            throw new Exception("Invalid statements found in query for assignment: " + matcher.reset()
                    .results().map(MatchResult::group).collect(Collectors.joining(", ")));
        }
        Map<String, String> mapData = new HashMap<>();
        for (String value : insertCommand.split("\\s*,\\s*")) {
            String[] strings = value.split("\\s*=\\s*");
            mapData.put(strings[0].replaceAll("['‘’]", ""), strings[1]);
        }
        return mapData;
    }

    public static Map<Object, String> getDataAndConditionUDS(String udsCommand) throws Exception {
        udsCommand = checkValidity(udsCommand);
        String[] string = udsCommand.split("(?i)\\s+where\\s+");
//        Arrays.stream(string).forEach(System.out::println);
        Map<Object, String> mapData = new HashMap<>();
        Map<String, String> updateVal = null;
        if (string.length == 2) {
            updateVal = getDataINSERT(string[0]);
            mapData.put(updateVal, string[1]);
        }
        mapData.put(updateVal, string[0]);
        return mapData;
    }

    public static Map<?, ?> getData(String iudsCommand) throws Exception {
        Map<?, ?> result = null;
        Pattern pattern = Pattern.compile(iudsRules);
        Matcher matcher = pattern.matcher(iudsCommand);
        if (matcher.find()) {
            String command = matcher.group().replaceAll("(?i)where", "")
                    .toUpperCase().trim().replaceAll("\\s{2,}", " ");
            result = switch (command) {
                case "INSERT VALUES" -> getDataINSERT(iudsCommand);
                case "UPDATE VALUES", "SELECT", "DELETE" -> getDataAndConditionUDS(iudsCommand);
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            getData("UPDATE VALUES ‘active’  =  false, ‘cost’  =    10.1, 'age' = 23 where (‘id’='' or (('age'=null) and 'cost' < 4)) and (‘active’=false or 'lastName' like 'test%' and ('cost' > 6) )");
            getData("    INSErT    VALUEs 'last Name' = 'Fedorov as %', 'id'=3, 'age' = null, 'active'= false  ");
            getData("select 'age'>=null and 'lastName' ilike '%п%' or 'cost' < 10");
            getData("UPDATE VALUES 'active'=true wHerE 'active'=false");
            getData("Select ");
            ValidatorIUDS.getDataINSERT("INSErT    VALUEs ");
            //    insertValues("UPDATE VALUES ‘active’=true where ‘active’=false");
            //    insertValues("select ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            getData("INSErT  VALUEs 'last Name' = 'Fedorov_and','id'=3.4,'age' = 40  , 'active'= false");
            getData("UPDATE VALUES ‘active’=false, ‘cost’=10.1 where ‘id’=3 and 'age' >= 18");
            getData("UPDATE VALUES ‘active’=true");
            getData("select where ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            getData("UPDATE VALUES ‘active’=false, ‘cost’=10.1 where (‘id’<='' or 'age'>=20) and(‘active’=false or 'lastName' = 'test')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
