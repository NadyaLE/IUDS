/*
 * Copyright (c) 2023. NadyaLE
 */
package com.digdes.school;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorIUDS {
    public static final String iudsRules = "((?i)(insert|update)\\s+values\\s*(?!\\s*$|\\s*where)|(select|delete)\\s*(\\s+where(?!\\s*$)|\\s*$))";
    public static final String or_and = "\\s*(?i)(or|and)(?i)\\s*";
    public static final String regex = "(%(?!['‘’]))?[A-я\\d\\s]*((?<!['‘’%])%)?";
    public static final String exprValidity = "['‘][A-я\\d\\s]*['’]\\s*((([<>!]?=|[<>])\\s*\\d+(\\.\\d+)?|!?=\\s*(true|false))(?=($|\\)|,|\\s))|(?i)(like|ilike|!?=)(?i)\\s*['‘]" + regex + "(" + regex + ")*['’])";
    public static final String dataValidity = "['‘][A-я\\d\\s]*['’]\\s*=\\s*((\\d+(\\.\\d+)?|(true|false|null))(?=($|,|\\s))|['‘][A-я\\d\\s]*['’])";
    public static final String dataEnumeration = dataValidity + "(\\s*,\\s*" + dataValidity + ")*(\\s*$)";
    public static final String expression = "(" + exprValidity + "|\\(\\s*\\))" + or_and + "(" + exprValidity + "|\\(\\s*\\)" + ")";
    private static final String exprMessage = "\nYou may have forgotten(added too many) parentheses that allow multiple operations (or|and), or the wrong data may have been used.";
    private static final String dataMessage = "\nPerhaps a comma was not placed after the expression, or the wrong operator may have been used.";
    private static final String missCommaMessage = "The comma must be followed by expression!";

    static String[] checkValidity(String iudsCommand) throws Exception {
        if (Pattern.compile("\\(\\s*\\)").matcher(iudsCommand).find()) throw new Exception("Empty parentheses!");
        String command;
        Matcher matcherCommand = Pattern.compile(iudsRules).matcher(iudsCommand);
        if (matcherCommand.find()) command = matcherCommand.group();
        else throw new Exception("Unsupported command!");
        if (Pattern.compile("(?i)\\s*where\\s*")
                .matcher(iudsCommand.replaceAll("['‘][A-я\\d\\s%]*['’]", "")).results().count() > 1)
            throw new Exception("WHERE cannot be more than one!");

        iudsCommand = iudsCommand.replaceFirst(iudsRules, "").strip();
        String[] splitWhere = iudsCommand.split("(?i)\\s*where\\s*(?i)(?!\\s*$)");
        String checkData = null, checkExpr = null;
        if (splitWhere.length == 2) {
            checkData = clearDataString(splitWhere[0]);
            checkExpr = splitWhere[1];
        } else {
            if (command.matches("(?i)(insert|update)\\s+values\\s*")) checkData = clearDataString(iudsCommand);
            else checkExpr = iudsCommand;
        }
        if (checkExpr == null) {
            if (checkData.isBlank()) return new String[]{splitWhere[0], checkExpr};
            throw new Exception("Syntax error: " + checkData + dataMessage);
        }
        Matcher matcher = Pattern.compile("(?<=\\()\\s*" + expression + "(?=\\s*\\))").matcher(checkExpr);
        while (matcher.find()) {
            checkExpr = checkExpr.replace(matcher.group(), "");
            matcher.reset(checkExpr);
        }
        checkExpr = checkExpr.replaceAll(expression, " ");
        checkExpr = Pattern.compile(or_and + "|,").matcher(checkExpr).find() ?
                checkExpr : checkExpr.replaceAll(exprValidity, "");

        if (checkData == null) {
            if (checkExpr.isBlank()) return new String[]{checkData, splitWhere[0]};
            throw new Exception("Syntax error: " + checkExpr + exprMessage);
        }

        if (!checkData.isBlank() || !checkExpr.isBlank()) {
            throw new Exception("\nSyntax error!" +
                    "\nData check status - " + (checkData.isBlank() ? "Passed!" : "Failed! " +
                    (checkData.replaceAll(exprValidity, "").isBlank() ? "Missing comma after: " + checkData :
                            "There are unresolved expressions: " + checkData)) +
                    "\nConditions check status - " + (checkExpr.isBlank() ? "Passed!" : "Failed!\nCause: " +
                    checkExpr.replaceAll(exprValidity, "")) + exprMessage);
        }
        return splitWhere;
    }

    static String clearDataString(String dataStr) throws Exception {
        if (dataStr.endsWith(",")) throw new Exception(missCommaMessage);
        return dataStr.replaceAll(dataEnumeration, "").replaceAll(dataValidity + "(\\s*,\\s*)", "");
    }

    static Map<String, String> getData(String insertCommand) {
        Map<String, String> mapData = new HashMap<>();
        for (String value : insertCommand.split("\\s*,\\s*")) {
            String[] strings = value.split("\\s*=\\s*");
            mapData.put(strings[0].replaceAll("['‘’]", ""), strings[1]);
        }
        return mapData;
    }

    static Map<String, Map<String, String>> getDataAndCondition(String udsCommand) throws Exception {
        String[] string = checkValidity(udsCommand);
        Map<String, String> updateVal = null;
        if (string[0] != null) updateVal = getData(string[0]);
        return Collections.singletonMap(string[1], updateVal);
    }
}
