package com.digdes.school;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorIUDS {
    public static final String iudsRules = "(?i)(insert|update)\\s+values\\s+|(select|delete)\\s*(\\s+where)?";
    public static final String or_and = "\\s*(?i)(or|and)(?i)\\s*";
    public static final String rightEndData = "(\\s*,|\\s*$|\\s+where)";
    public static final String exprValidity = "['‘][A-я\\d\\s]*['’](\\s*(([<>!]?=|[<>])\\s*\\d+(\\.\\d+)?|!?=\\s*(true|false|null))(?=($|\\)|,|\\s))|\\s*((?i)(like|ilike|!?=)(?i)\\s*(['‘][A-я\\d\\s%]*['’])))";
    public static final String dataEnumeration = exprValidity + "(\\s*,\\s*" + exprValidity + ")*(\\s*$)";
    public static final String expression = "(" + exprValidity + "|\\(\\s*\\))" + or_and + "(" + exprValidity + "|\\(\\s*\\)" + ")";

    private static String checkValidity(String iudsCommand) throws Exception {
        if (iudsCommand.matches("(?i)\\s*insert\\s+values\\s*")
                || iudsCommand.matches("(?i)\\s*update\\s+values\\s*")) {
            throw new Exception("Empty INSERT|UPDATE command!");
        }
        if (Pattern.compile("\\(\\s*\\)").matcher(iudsCommand).find()) {
            throw new Exception("Empty parentheses!");
        }
        iudsCommand = iudsCommand.replaceFirst(iudsRules, "").trim();
        String[] splitWhere = iudsCommand.split("(?i)\\s*where\\s*(?i)(?!\\s*$)");
        String checkData = null, checkExpr = null;
        if (splitWhere.length == 2) {
            checkData = splitWhere[0].replaceAll(dataEnumeration, "")
                    .replaceAll(exprValidity + "(\\s*,\\s*)", "");
            checkExpr = splitWhere[1];
        } else {
            if (iudsCommand.replaceAll(dataEnumeration, "").isBlank() ||
                    iudsCommand.replaceAll(expression, "").isBlank()) {
                return iudsCommand;
            }
            if (Pattern.compile("([<>!]=|[<>]|(?i)(like|ilike)(?i))|" + or_and)
                    .matcher(iudsCommand.replaceAll("['‘][A-я\\d\\s%]*['’]", "")).find()) {
                checkExpr = iudsCommand;
            } else {
                if (iudsCommand.endsWith(",")) throw new Exception("The comma must be followed by expression!");
                checkData = iudsCommand.replaceAll(dataEnumeration, "")
                        .replaceAll(exprValidity + "(\\s*,\\s*)", "");
            }
        }
        if (checkExpr != null) if (checkExpr.replaceAll(exprValidity, "").isBlank()) return iudsCommand;
        if (checkExpr == null) {
            if (checkData.isBlank()) return iudsCommand;
            throw new Exception("Syntax error: " + checkData +
                    "\nA comma may not have been placed after the expression, " +
                    "\nor the wrong operator may have been used");
        }

        Pattern pattern = Pattern.compile("(?<=\\()\\s*" + expression + "(?=\\s*\\))");
        Matcher matcher = pattern.matcher(checkExpr);
        while (matcher.find()) {
            System.out.println(checkExpr + " - " + matcher.group());
            checkExpr = checkExpr.replace(matcher.group(), "");
            matcher.reset(checkExpr);
        }
        checkExpr = checkExpr.replaceAll(expression, " ");
        //   checkExpr = checkExpr.replaceAll(dataEnumeration, " ");

        if (checkData == null) {
            if (checkExpr.isBlank()) return iudsCommand;
            throw new Exception("Syntax error: " + checkExpr +
                    "\nMaybe you forgot the parentheses that allow multiple or/and operations");
        }

        if (!checkData.isBlank() || !checkExpr.isBlank()) {
            throw new Exception("\nSyntax error!" +
                    "\nData check status - " + (checkData.isBlank() ? "Passed!" : "Failed! " +
                    (checkData.replaceAll(exprValidity, "").isBlank() ? "Missing comma after: " + checkData :
                            "There are unresolved expressions: " + checkData)) +
                    "\nConditions check status - " + (checkExpr.isBlank() ? "Passed!" : "Failed!\nCause: " + checkExpr));
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
            getData("UPDATE VALUES 'age' = 23 where (‘id’=''or('age'=null and'cost' < 4))and((‘active’=false or'lastName'like'test%')and'cost'>6 )");
            getData("    INSErT    VALUEs 'last Name' = 'Fedorov as %', 'id'=3, 'age' = null, 'active'= false  ");
            //   getData("select 'age'!=null and 'lastName' ilike '%п%' or 'cost' < 10");
            getData("UPDATE VALUES 'active'=true wHerE 'active'=false");
            getData("Select");
            //   ValidatorIUDS.getDataINSERT("INSErT    VALUEs ");
            //    insertValues("UPDATE VALUES ‘active’=true where ‘active’=false");
            //    insertValues("select ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            getData("INSErT  VALUEs 'last Name' = 'Fedorov_and','id'=3.4,'age' = 40  , 'active'= false");
            getData("UPDATE VALUES ‘active’=false, ‘cost’=10.1 where ‘id’=3 and 'age' >= 18");
            getData("UPDATE VALUES ‘active’=true");
            getData("select where ‘age’>=30 and ‘lastName’ ilike ‘%п%’");
            getData("UPDATE VALUES ‘active’=false, ‘cost’=10 where (‘id’='' or 'age'>=20) and(‘active’=false or 'lastName' = 'test')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
