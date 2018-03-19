import minipython.node.*;

import java.util.ArrayList;

/**
 * This class contains some helper methods that are common in both visitors
 */
public class Utils {
    /**
     * Checks and returns if a function call is legit
     * @param functionId the TID of the function to search
     * @param args the arguments of the function call
     * @param symbolTable the symbolTable
     * @param originFunctionParamTypes the original function parameters, to reset later
     * @return returns an empty function with error string or the function instance
     */
    public static Function checkLegitFunction(TId functionId,
                                                PArglist args,
                                                RootSymbolTable symbolTable,
                                                ArrayList<String> originFunctionParamTypes) {
        // Start find out how many arguments has the function node
        ArrayList<PExpression> functionCallParams = new ArrayList<>();
        // Start the count recursively
        while (args != null) {
            // if the arg is simple expression, add the counter and break
            // we reached our point
            if (args instanceof AExpArglist) {
                functionCallParams.add(((AExpArglist) args).getExpression());
                break;
            }

            // if it contains a not null expression and its a more expressions arglist add it to the counter
            if (((AMoreExpArglist) args).getExpression() != null) {
                functionCallParams.add(((AMoreExpArglist) args).getExpression());
            }

            // override the args to the moreexparglist
            args = ((AMoreExpArglist) args).getArglist();
        }

        // Find all the functions with the same name
        ArrayList<Function> functionsFound = symbolTable.getAllFunctions(functionId.getText());

        // If there is at least one more function with the same name (id) we have to check if the number of parameters are acceptable
        if (functionsFound.size() > 0) {
            boolean found = false;
            Function foundFunction = null;

            // For every found function, check its acceptance bounds and decide it we accept the call or not
            for (Function function : functionsFound) {
                ArrayList<Integer> foundBounds = new ArrayList<>();
                for (int i = function.getNonDefaults(); i <= function.getDefaults() + function.getNonDefaults(); i++) {
                    foundBounds.add(i);
                }

                if (foundBounds.contains(functionCallParams.size())) {
                    found = true;
                    foundFunction = function;
                    foundFunction.setErrorString(null);
                    break;
                }
            }

            // If the function is not found, print the appropriate error
            if (!found) {
                foundFunction = new Function();
                foundFunction.setErrorString("No such function has been defined '" + functionId.getText() +
                        "' with " + functionCallParams.size() + " parameters" +
                        " in the line:" + functionId.getLine() +
                        " pos: " + functionId.getPos());
            }
            else {
                // hold the original parameters to switch back to them later
                for(Variable var : foundFunction.getParams()){
                    originFunctionParamTypes.add(var.getType());
                }

                for (int i = 0; i < functionCallParams.size(); i++) {
                    // Get the i function call param
                    PExpression functionCallParam = functionCallParams.get(functionCallParams.size() - 1 - i);

                    // Get the i function param
                    Variable functionParam = foundFunction.getParams().get(i);

                    // Get the expression type of the function call param
                    String functionCallParamType = Utils.getExpressionsType(functionCallParam, foundFunction, symbolTable);

                    // If we cannot determine the type of the function call param, do not print any error
                    if(functionCallParamType.equals("UNDEF")) continue;

                    if(!functionParam.getType().equals(functionCallParamType)){
                        functionParam.setType(functionCallParamType);
                    }
                }

                foundFunction.setReturnType(
                        Utils.getExpressionsType(foundFunction.getReturnNode(), foundFunction, symbolTable));

                if(foundFunction.getReturnType().equals("TYPECONFLICT")){
                    foundFunction.setErrorString("Found Conflict While Trying to Addition String and Int line: " + functionId.getLine());
                }else if(foundFunction.getReturnType().equals("TYPECONFLICT2")){
                    foundFunction.setErrorString("Cannot use string values in arithmetic operations(-,*,/) line: " + functionId.getLine());
                }

                return foundFunction;
            }
            return foundFunction;
        }
        // If we have not find any function with the function call name, then print the error
        else {
            Function tempFunction = new Function();
            tempFunction.setErrorString("No such function has been defined '" + functionId.getText() +
                    "' in the line:" + functionId.getLine() +
                    " pos: " + functionId.getPos());
            return tempFunction;
        }
    }

    /**
     * Get the type from an expression
     * @param expression the expression to return it's type
     * @param insideFunction the function where the expression is inside, otherwise null
     * @param symbolTable the symbolTable
     * @return the type of the expression
     */
    public static String getExpressionsType(PExpression expression, Function insideFunction, RootSymbolTable symbolTable){
        if(expression instanceof AValueExpression){
            AValueExpression value = (AValueExpression) expression;
            if(value.getValue() instanceof ANumberValue) return "INT";
            else return "STR";
        }
        if(expression instanceof AIdentifierExpression){
            AIdentifierExpression id = (AIdentifierExpression) expression;
            Variable varFound = getVariableFromId(id.getId(), insideFunction, symbolTable);
            return varFound == null ? "UNDEF" : varFound.getType();
        }
        if(expression instanceof AExpInBracketsExpression){
            return "UNDEF";
        }
        if(expression instanceof AFunctionExpression){
            AFunctionExpression funExp = (AFunctionExpression) expression;
            AFunctionCall funCall = (AFunctionCall) funExp.getFunctionCall();

            ArrayList<String> originFunctionParamTypes = new ArrayList<>();
            Function foundFunction = checkLegitFunction(funCall.getId(), funCall.getArglist(), symbolTable, originFunctionParamTypes);

            if(foundFunction.getParams() != null){
                for(int i = 0; i < foundFunction.getParams().size(); i++){
                    foundFunction.getParams().get(i).setType(originFunctionParamTypes.get(i));
                }
            }

            return foundFunction.getReturnType() == null ? "UNDEF" : foundFunction.getReturnType();
        }
        if(expression instanceof AAdditionExpression){
            AAdditionExpression addExp = (AAdditionExpression) expression;
            String typeLeft = getExpressionsType(addExp.getExp1(), insideFunction, symbolTable);
            String typeRight = getExpressionsType(addExp.getExp2(), insideFunction, symbolTable);

            if(typeLeft.equals("UNDEF") || typeRight.equals("UNDEF")) return "UNDEF";
            if(typeLeft.equals("ARR") || typeRight.equals("ARR")) return "UNDEF";

            if(typeLeft.equals(typeRight)){
                return typeLeft;
            }else{
                return "TYPECONFLICT";
            }
        }
        if(expression instanceof AAbstractionExpression){
            AAbstractionExpression addExp = (AAbstractionExpression) expression;
            String typeLeft = getExpressionsType(addExp.getExp1(), insideFunction, symbolTable);
            String typeRight = getExpressionsType(addExp.getExp2(), insideFunction, symbolTable);

            if(typeLeft.equals("STR") || typeRight.equals("STR")) return "TYPECONFLICT2";
            return "INT";
        }
        if(expression instanceof AMultExpression){
            AMultExpression addExp = (AMultExpression) expression;
            String typeLeft = getExpressionsType(addExp.getExp1(), insideFunction, symbolTable);
            String typeRight = getExpressionsType(addExp.getExp2(), insideFunction, symbolTable);

            if(typeLeft.equals("STR") || typeRight.equals("STR")) return "TYPECONFLICT2";
            return "INT";
        }
        if(expression instanceof ADivExpression){
            ADivExpression addExp = (ADivExpression) expression;
            String typeLeft = getExpressionsType(addExp.getExp1(), insideFunction, symbolTable);
            String typeRight = getExpressionsType(addExp.getExp2(), insideFunction, symbolTable);

            if(typeLeft.equals("STR") || typeRight.equals("STR")) return "TYPECONFLICT2";
            return "INT";
        }
        if(expression instanceof AExpsInsideBracketsExpression){
            return "ARR";
        }
        return "UNDEF";
    }

    /**
     * Search and returns the variable from its name
     * @param id the TID of the variable
     * @param insideFunction if the variable is inside a function, otherwise pass null
     * @param symbolTable the symbolTable
     * @return the variable instance
     */
    public static Variable getVariableFromId(TId id, Function insideFunction, RootSymbolTable symbolTable){
        if(insideFunction == null){
            return getGlobalVariableFromId(id, symbolTable);
        }
        else{
            if(!insideFunction.getVars().containsKey(id.getText())){
                return getGlobalVariableFromId(id, symbolTable);
            }
            else{
                return insideFunction.getVars().get(id.getText());
            }
        }
    }

    /**
     * Search and returns the global variable from its name
     * @param id the TID of the variable
     * @param symbolTable the symbolTable
     * @return the variable instance
     */
    public static Variable getGlobalVariableFromId(TId id, RootSymbolTable symbolTable){
        if(!symbolTable.getVariableHashMap().containsKey(id.getText())){
            return null;
        }
        else{
            return symbolTable.getVariableHashMap().get(id.getText());
        }
    }
}
