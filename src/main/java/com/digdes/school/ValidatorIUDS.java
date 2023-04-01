package com.digdes.school;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorIUDS {
    private static final String iudsRules = "(?i)(insert|update)\\s+values\\s+|(select|delete)\\s+(where)?";
    private static final String validExpr = "['‘][A-я\\d\\s]*['’]\\s*(?:[<>!]?=|[<>]|(like|ilike))\\s*(?:['‘][A-я\\d\\s%]*['’]|true|false|\\d+(\\.\\d+)?)";
    private static final String validExprEnd = "((?i)\\s*,|\\s+(and|or)\\s+|\\s+where\\s+)";

    public static String checkValidityAndGetData(String iudsCommand) throws Exception {
        if (iudsCommand.endsWith(",")) {
            throw new Exception("The comma must be followed by an expression!");
        }
        iudsCommand = iudsCommand.replaceAll(iudsRules, "").trim();
        String check = iudsCommand.replaceAll(validExpr + validExprEnd, "");
        Pattern pattern = Pattern.compile(validExpr);
        Matcher matcher = pattern.matcher(check);
        if (matcher.results().count() > 1) {
            throw new Exception("Missing comma between: " + matcher.reset()
                    .results().map(MatchResult::group)
                    .collect(Collectors.joining(" and ")));
        }
        check = check.replaceAll(validExpr, "");
        if (!check.isBlank()) {
            throw new Exception("Syntax error: " + check);
        }
        return iudsCommand;
    }

    public static Map<String, String> getDataFromCommandINSERT(String iudsCommand) throws Exception {
        iudsCommand = checkValidityAndGetData(iudsCommand);
        String string = iudsCommand.replaceAll("['‘][A-я\\d\\s%]*['’]", "");
        Pattern pattern = Pattern.compile("(?i)[<>!]+=?|\\s+(like|ilike|and|or|where)\\s+");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            throw new Exception("Invalid statements found in query for INSERT command: " + matcher.reset()
                    .results().map(MatchResult::group)
                    .collect(Collectors.joining(", ")));
        }
        Map<String, String> mapData = new HashMap<>();
        for (String value : iudsCommand.split("\\s*,\\s*")) {
            String[] strings = value.split("\\s*=\\s*");
            mapData.put(strings[0].replaceAll("['‘’]", ""), strings[1]);
        }
        mapData.forEach((key, value) -> System.out.println(key + " " + value));
        return mapData;
    }

    public static Map<String, String> getData(String iudsCommand) throws Exception {
        Pattern pattern = Pattern.compile(iudsRules);
        Matcher matcher = pattern.matcher(iudsCommand);
        System.out.println(matcher
                .results().map(MatchResult::group)
                .collect(Collectors.joining(", ")));
        if (matcher.results().count() > 1) {
            throw new IllegalArgumentException("Undefined command!");
        }
        if (matcher.reset().find()) {
            String command = matcher.group().toUpperCase().trim().replaceAll("\\s{2,}"," ");
            return switch (command) {
                case "INSERT VALUES" -> getDataFromCommandINSERT(iudsCommand);
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            ValidatorIUDS.checkValidityAndGetData("    INSErT    VALUEs 'last Name' = 'Fedorov as' , 'id'=3, 'age' = 40, 'active'= false");
            ValidatorIUDS.checkValidityAndGetData("select 'age'>=30 and 'lastName' ilike '%п%'");
            ValidatorIUDS.checkValidityAndGetData("UPDATE VALUES 'active'=true wHerE 'active'=false");
            ValidatorIUDS.checkValidityAndGetData("Select ");
            // insertValues("UPDATE VALUES ‘active’=true where ‘active’=false");
            // insertValues("select ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            getData("INSErT  VALUEs 'last Name' = 'Fedorov_and','id'=3.4,'age' = 40  , 'active'= false");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
