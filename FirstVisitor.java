import minipython.analysis.DepthFirstAdapter;
import minipython.node.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Class FirstVisitor
 * This class represents out first visitor. Here we fill up the symbolTable and check some
 * compilation errors that are not deal with function calls
 */
public class FirstVisitor extends DepthFirstAdapter {
    /* Our Symbol Table */
    private RootSymbolTable symbolTable;

    /* Error Counter */
    private int errors;

    /* If we are inside a function */
    private Function withinAFunction = null;

    /* count of the no default and default parms of each function */
    private int nonDefaultFunctionParams;
    private int defaultFunctionParams;

    /* If he for statement added a new variable, remove it later */
    private boolean ifStatementHasNewVar = false;

    /* The function parameters, linked because we need addition order */
    private LinkedHashMap<Node, Variable> functionParams;

    /**
     * Constructor
     * @param symbolTable the symbol table
     */
    FirstVisitor(RootSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        errors = 0;
    }

    /**
     * Checks if a function has already been defined
     * @param functionToBeAdded the function to search
     * @return the line where the function is defined or -1
     */
    private int hasAlreadyBeenDefined(Function functionToBeAdded){
        // functionsFound contains all the functions already visited, that have the same name (id) with the currently being checked funtion
        ArrayList<Function> functionsFound = symbolTable.getAllFunctions(functionToBeAdded.getId());

        // if there is at least one more function with the smae name (id) we have to check if we have to accept this function or not, depending on if it is correct way of overloading
        if(functionsFound.size() > 0){
            // we use foundBounds to save all the possible number of params, in what we can call the currently being checked function
            ArrayList<Integer> foundBounds = new ArrayList<>();
            for(int i = nonDefaultFunctionParams; i <= defaultFunctionParams + nonDefaultFunctionParams; i++){
                foundBounds.add(i);
            }

            // for each function of those we have already visited with the same name (id)
            for(Function temp : functionsFound){
                // we use tempBounds to save all the possible number of params, in what we can call the specific previous visited function
                ArrayList<Integer> tempBounds = new ArrayList<>();
                for(int i = temp.getNonDefaults(); i <= temp.getDefaults() + temp.getNonDefaults(); i++){
                    tempBounds.add(i);
                }

                // if those two funtions can be called with the same num of params, since they also have the same name, its not a correct way of overloading methods, thus we have to show the error to the user and not accept the current funtion
                for(Integer k : tempBounds){
                    if(foundBounds.contains(k)){
                        return temp.getLine();
                    }
                }
            }
            return -1;
        }
        // if there was no other funtion with the same name (id) already visited, we simple add the new function
        else{
            return -1;
        }
    }

    /* Function */

    @Override
    public void inAFunction(AFunction node) {
        // Function's info
        String id = node.getId().getText();
        int line = node.getId().getLine();
        int pos = node.getId().getPos();

        // Each time we find a new function, we set the counts back to zero
        nonDefaultFunctionParams = 0;
        defaultFunctionParams = 0;

        functionParams = new LinkedHashMap<>();

        // Visit all the nodes that has the functions parameters
        AArgument args = (AArgument) node.getArgument();
        if(args!=null){
            args.apply(this);
        }

        // Create the new fucntion object
        Function tempFunction = new Function();
        tempFunction.setId(id);
        tempFunction.setLine(line);
        tempFunction.setPos(pos);
        tempFunction.setNonDefaults(nonDefaultFunctionParams);
        tempFunction.setDefaults(defaultFunctionParams);

        // Set the params to the instance and check for duplicates
        HashMap<String, Variable> params = new HashMap<>();
        ArrayList<Variable> sortedParameters = new ArrayList<>();
        for(Variable temp : functionParams.values()){
            if(params.containsKey(temp.getId())){
                System.out.println("Error " + (++errors) + ": The parameter '" + temp.getId() +
                        "' in the line:" + temp.getLine() +
                        " has already been defined in line " + params.get(temp.getId()).getLine());
                return;
            }
            params.put(temp.getId(), temp);
            sortedParameters.add(temp);
        }
        tempFunction.setVars(params);
        tempFunction.setParams(sortedParameters);

        withinAFunction = tempFunction;

        // Check if function has already been defined
        int lineFound = hasAlreadyBeenDefined(tempFunction);
        if(lineFound == -1){
            node.getStatement().apply(this);
            symbolTable.addFunction(tempFunction);
        }
        else{
            System.out.println("Error " + (++errors) + ": The function '" + tempFunction.getId() +
                    "' in the line:" + tempFunction.getLine() +
                    " has already been defined in line " + lineFound);
            return;
        }
    }

    @Override
    public void caseAFunction(AFunction node) {
        inAFunction(node);
        outAFunction(node);
    }

    @Override
    public void outAFunction(AFunction node) {
        defaultFunctionParams = 0;
        nonDefaultFunctionParams = 0;
        functionParams = null;
        withinAFunction = null;
    }

    /* Argument */

    @Override
    public void inAArgument(AArgument node) {
        // Check the first param
        Variable param = new Variable();

        TId id = node.getId();
        param.setId(id.getText());
        param.setLine(id.getLine());
        param.setPos(id.getPos());
        functionParams.put(node, param);

        // check if there is assignValue
        AAssignValue assignValue = (AAssignValue) node.getAssignValue();
        if(assignValue!=null){
            defaultFunctionParams++;
            assignValue.apply(this);
        }
        else {
            nonDefaultFunctionParams++;
            functionParams.get(node).setType("UNDEF");
        }

        // Do the same for each one of the "more args - params"
        LinkedList params = node.getParameters();
        for(Object temp : params){
            AParameters tempParam = ((AParameters) temp);
            tempParam.apply(this);
        }
    }

    @Override
    public void caseAArgument(AArgument node) {
        inAArgument(node);
        outAArgument(node);
    }

    /* Parameters */

    @Override
    public void inAParameters(AParameters node) {
        Variable param = new Variable();

        TId id = node.getId();
        param.setId(id.getText());
        param.setLine(id.getLine());
        param.setPos(id.getPos());
        functionParams.put(node, param);

        // check if there is assignValue
        AAssignValue assignValue = (AAssignValue) node.getAssignValue();
        if(assignValue!=null){
            defaultFunctionParams++;
            assignValue.apply(this);
        }
        else {
            nonDefaultFunctionParams++;
            functionParams.get(node).setType("UNDEF");
        }
    }

    @Override
    public void caseAParameters(AParameters node) {
        inAParameters(node);
        outAParameters(node);
    }

    /* Assign Value */

    @Override
    public void inAAssignValue(AAssignValue node) {
        if(node.parent() instanceof AArgument || node.parent() instanceof AParameters){
            functionParams.get(node.parent()).setHasDefault(true);
        }
        PValue value = node.getValue();
        value.apply(this);
    }

    @Override
    public void caseAAssignValue(AAssignValue node) {
        inAAssignValue(node);
        outAAssignValue(node);
    }

    /* Values */

    @Override
    public void inANumberValue(ANumberValue node) {
        if(node.parent() instanceof AAssignValue){
            functionParams.get(node.parent().parent()).setType("INT");
        }
    }

    @Override
    public void inAStringValue(AStringValue node) {
        // We do not want to check the addition because we can add two string values
        if (node.parent() instanceof AValueExpression){
            if(node.parent().parent() instanceof AAbstractionExpression
                    || node.parent().parent() instanceof AMultExpression
                    || node.parent().parent() instanceof ADivExpression){
                if(withinAFunction == null){
                    System.out.println("Error " + (++errors) + ": An arithmetic expression cannot contain string literal '" + node.getStringLit().getText() +
                            "' in the line:" + node.getStringLit().getLine() +
                            " pos: " + node.getStringLit().getPos());
                }
                else{
                    System.out.println("Error " + (++errors) + ": An arithmetic expression cannot contain string literal '" + node.getStringLit().getText() +
                            "' in the line:" + node.getStringLit().getLine() +
                            " pos: " + node.getStringLit().getPos() +
                            " within the function '" + withinAFunction.getId());
                }
            }
        }
        else if(node.parent() instanceof AAssignValue){
            functionParams.get(node.parent().parent()).setType("STR");
        }
    }

    /* Statements */
    /* TODO IN SECOND VISITOR, If we meet function at any statement
     * {if} comparison statement |
     * {while} comparison statement |
     * {for} [id1]:id [id2]:id statement |
     * {return} expression |
     * {print} arglist |
     * {equals} id expression |
     * {minus_equals} id expression |
     * {div_equals} id expression |
     * {array} id [exp1]:expression [exp2]:expression |
     * {function} id arglist;  TODO IN SECOND VISITOR
     */

    /* For Statement */

    @Override
    public void inAForStatement(AForStatement node) {
        boolean notFound = true;
        // Add the new id, id1, to the vars list
        TId newVarId = node.getId1();
        Variable newVar = new Variable();
        newVar.setId(newVarId.getText());
        newVar.setLine(newVarId.getLine());
        newVar.setPos(newVarId.getPos());
        newVar.setType("UNDEF");

        // Make sure that the id2, meaning the in id, has been defined and its type is array
        TId idArray = node.getId2();

        // If the id2 does not exist print error
        if(Utils.getVariableFromId(idArray, withinAFunction, symbolTable) == null){
            System.out.println("Error " + (++errors) + ": The variable '" + idArray.getText() +
                    "' in the line:" + idArray.getLine() +
                    " pos: " + idArray.getPos() +
                    "', has not been defined yet!");
        }

        if(withinAFunction != null){
            withinAFunction.getVars().put(newVarId.getText(), newVar);
            // Check id2
            if(withinAFunction.getVars().containsKey(idArray.getText())){
                notFound = false;
                if(!withinAFunction.getVars().get(idArray.getText()).getType().equals("ARR")
                        && !withinAFunction.getVars().get(idArray.getText()).getType().equals("UNDEF")){
                    System.out.println("Error " + (++errors) +
                            ": Cannot use for statement ( line: " + idArray.getLine() +
                            ", pos: " + idArray.getPos() +
                            ") with simple variable '" + idArray.getText() +
                            "' defined in the line: " + withinAFunction.getVars().get(idArray.getText()).getLine() +
                            " whithin the function '" + withinAFunction.getId());
                    return;
                }
            }
        }
        else{
            // if the id1 does not exist put it in the symbol table but we want to remove it in the outForStatement function
            if(Utils.getGlobalVariableFromId(newVarId, symbolTable) == null){
                ifStatementHasNewVar = true;
                symbolTable.getVariableHashMap().put(newVarId.getText(), newVar);
            }
        }

        if(notFound){
            if(symbolTable.getVariableHashMap().containsKey(idArray.getText())){
                if(!symbolTable.getVariableHashMap().get(idArray.getText()).getType().equals("ARR")
                        && !symbolTable.getVariableHashMap().get(idArray.getText()).getType().equals("UNDEF")){
                    System.out.println("Error " + (++errors) +
                            ": Cannot use for statement ( line: " + idArray.getLine() +
                            ", pos: " + idArray.getPos() +
                            ") with simple variable '" + idArray.getText() +
                            "' defined in the line: " + symbolTable.getVariableHashMap().get(idArray.getText()).getLine());
                    return;
                }
            }
        }

    }

    @Override
    public void outAForStatement(AForStatement node) {
        if(ifStatementHasNewVar){
            ifStatementHasNewVar = false;

            TId newVarId = node.getId1();
            symbolTable.getVariableHashMap().remove(newVarId.getText());
        }
    }

    /* Equals Statement */

    @Override
    public void inAEqualsStatement(AEqualsStatement node) {
        // we save the errors till now
        int errorBeforeExpApplied = errors;
        // we visit the experssion part of the node
        node.getExpression().apply(this);
        // now we check if and only if the expression was valid, and didn't create any compile error in order to poceed, otherwise we ignore the assignment
        if(errorBeforeExpApplied == errors){
            // we get the type of expression we are going to assign to the id
            String typeOfExpToTheLeft = Utils.getExpressionsType(node.getExpression(), withinAFunction, symbolTable);

            // we check if the if the id has already been defined or not
            // if it hasnt, we add it now to the corresponding hashmap (local vars or globals) and set its type to the type of the expression assigned to it
            TId id = node.getId();
            Variable var = Utils.getVariableFromId(id, withinAFunction, symbolTable);

            if(var == null){
                var = new Variable();
                var.setId(id.getText());
                var.setLine(id.getLine());
                var.setPos(id.getPos());
                var.setType(typeOfExpToTheLeft);
                if(withinAFunction!=null) withinAFunction.getVars().put(var.getId(), var);
                else symbolTable.getVariableHashMap().put(var.getId(), var);
                return;
            }

            // but if it has been defined then we simple check if their type is different, we update the type of the id to the new type, due to the expression assigned
            if(!var.getType().equals(typeOfExpToTheLeft)){
                if(withinAFunction != null){
                    withinAFunction.getVars().get(id.getText()).setType(typeOfExpToTheLeft);
                    return;
                }
                symbolTable.getVariableHashMap().get(id.getText()).setType(typeOfExpToTheLeft);
                return;
            }
        }
    }

    @Override
    public void caseAEqualsStatement(AEqualsStatement node) {
        inAEqualsStatement(node);
        outAEqualsStatement(node);
    }

    /* Minus Equal Statement */

    @Override
    public void inAMinusEqualsStatement(AMinusEqualsStatement node) {
        // we save the errors till now
        int errorBeforeExpApplied = errors;
        // we visit the experssion part of the node
        node.getExpression().apply(this);
        // now we check if and only if the expression was valid, and didn't create any compile error in order to poceed, otherwise we ignore the assignment
        if(errorBeforeExpApplied == errors){
            // we get the type of expression to the right of the operator
            String typeOfExpToTheLeft = Utils.getExpressionsType(node.getExpression(), withinAFunction, symbolTable);

            // we check if the if the id has already been defined or not
            TId id = node.getId();
            Variable var = Utils.getVariableFromId(id, withinAFunction, symbolTable);

            // if it hasnt been defined, we print the corresponding error of using an undefined variable
            if(var == null){
                System.out.println("Error " + (++errors) + ": The variable '" + id.getText() +
                        "' in the line:" + id.getLine() +
                        " pos: " + id.getPos() +
                        "', has not been defined yet!");
                return;
            }

            // if it has been defined, we check if its type is string or array (meaning not int and not undef), and print the corresponding error, that only arithmetics are accepted in this kind of operation
            if(!typeOfExpToTheLeft.equals("INT") && !typeOfExpToTheLeft.equals("UNDEF")){
                if(var.getType().equals("INT") || var.getType().equals("UNDEF")){
                    System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the minus-equal operator," +
                            " and in line:" + id.getLine() +
                            " the right part of the equality is not arithmetic");
                }
                else{
                    System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the minus-equal operator," +
                            " and in line:" + id.getLine() +
                            " the both parts of the equality is not arithmetic");
                    return;
                }
            }

            // if the left part of the operation, is string type or array we print the error
            if(var.getType().equals("STR") || var.getType().equals("ARR")){
                System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the minus-equal operator," +
                        " and in line:" + id.getLine() +
                        " the left part of the equality, the variable '" + var.getId() +
                        "' is not arithmetic, defined in line: " +var.getLine());
                return;
            }
            // if the left type was undef, we update its type to an int
            if(var.getType().equals("UNDEF")){
                if(withinAFunction!=null) withinAFunction.getVars().get(var.getId()).setType("INT");
                else symbolTable.getVariableHashMap().get(var.getId()).setType("INT");
            }
        }
    }

    @Override
    public void caseAMinusEqualsStatement(AMinusEqualsStatement node) {
        inAMinusEqualsStatement(node);
        outAMinusEqualsStatement(node);
    }

    /* Div Equals Statement */

    @Override
    public void inADivEqualsStatement(ADivEqualsStatement node) {
        // we save the errors till now
        int errorBeforeExpApplied = errors;
        // we visit the experssion part of the node
        node.getExpression().apply(this);
        // now we check if and only if the expression was valid, and didn't create any compile error in order to poceed, otherwise we ignore the assignment
        if(errorBeforeExpApplied == errors){
            // we get the type of expression to the right of the operator
            String typeOfExpToTheLeft = Utils.getExpressionsType(node.getExpression(), withinAFunction, symbolTable);

            // we check if the if the id has already been defined or not
            TId id = node.getId();
            Variable var = Utils.getVariableFromId(id, withinAFunction, symbolTable);

            // if it hasnt been defined, we print the corresponding error of using an undefined variable
            if(var == null){
                System.out.println("Error " + (++errors) + ": The variable '" + id.getText() +
                        "' in the line:" + id.getLine() +
                        " pos: " + id.getPos() +
                        "', has not been defined yet!");
                return;
            }

            // if it has been defined, we check if its type is string or array (meaning not int and not undef), and print the corresponding error, that only arithmetics are accepted in this kind of operation
            if(!typeOfExpToTheLeft.equals("INT") && !typeOfExpToTheLeft.equals("UNDEF")){
                if(var.getType().equals("INT") || var.getType().equals("UNDEF")){
                    System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the div-equal operator," +
                            " and in line:" + id.getLine() +
                            " the right part of the equality is not arithmetic");
                }
                else{
                    System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the div-equal operator," +
                            " and in line:" + id.getLine() +
                            " the both parts of the equality is not arithmetic");
                    return;
                }
            }

            // if the left part of the operation, is string type or array we print the error
            if(var.getType().equals("STR") || var.getType().equals("ARR")){
                System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part in the div-equal operator," +
                        " and in line:" + id.getLine() +
                        " the left part of the equality, the variable '" + var.getId() +
                        "' is not arithmetic, defined in line: " +var.getLine());
                return;
            }
            // if the left type was undef, we update its type to an int
            if(var.getType().equals("UNDEF")){
                if(withinAFunction!=null) withinAFunction.getVars().get(var.getId()).setType("INT");
                else symbolTable.getVariableHashMap().get(var.getId()).setType("INT");
            }
        }
    }

    @Override
    public void caseADivEqualsStatement(ADivEqualsStatement node) {
        inADivEqualsStatement(node);
        outADivEqualsStatement(node);
    }

    /* Return Statement */

    @Override
    public void inAReturnStatement(AReturnStatement node) {
        // If we are not inside a function, we cannot have return
        if(withinAFunction == null){
            System.out.println("Error " + (++errors) +
                    ": The return expression 'return " + node.toString().trim() +
                    "' cannot be outside a function");
            return;
        }

        PExpression returnExp = node.getExpression();

        // Visit the node
        returnExp.apply(this);

        // Find the return type from Utils and set it to the function instance
        withinAFunction.setReturnType(Utils.getExpressionsType(returnExp, withinAFunction, symbolTable));
        withinAFunction.setReturnNode(returnExp);
    }

    @Override
    public void caseAReturnStatement(AReturnStatement node) {
        inAReturnStatement(node);
        outAReturnStatement(node);
    }

    /* Array Statement */

    @Override
    public void inAArrayStatement(AArrayStatement node) {
        Variable foundVar = Utils.getVariableFromId(node.getId(), withinAFunction, symbolTable);

        // If the type of the expression is string print error
        String type = Utils.getExpressionsType(node.getExp1(), withinAFunction, symbolTable);
        if(type.equals("STR")){
            System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part as an index of array," +
                    " and in line:" + node.getId().getLine() +
                    " the index is string");
        }

        // If the variable has not been defined print error
        if(foundVar == null){
            System.out.println("Error " + (++errors) + ": The variable '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos() +
                    "', has not been defined yet!");
            return;
        }

        // If the variable is not array, cannot be used as array
        if(!foundVar.getType().equals("ARR")){
            System.out.println("Error " + (++errors) + ": The array '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos() +
                    "', has been defined as a variable in the line:" + foundVar.getLine() +
                    " pos: " + foundVar.getPos());
        }
    }


    /* Expressions */
    /* TODO IN SECOND VISITOR, If we meet function at any statement
     * {addition} [exp1]:expression [exp2]:expression |
     * {abstraction} [exp1]:expression [exp2]:expression |
     * {mult} [exp1]:expression [exp2]:expression |
     * {div} [exp1]:expression [exp2]:expression |
     * {identifier} id |
     * {exp_in_brackets} id expression |
     * {value} value |
     * {function} function_call TODO IN SECOND VISITOR |
     * {exps_inside_brackets} arglist
     */

    /* Identifier Expression */

    @Override
    public void inAIdentifierExpression(AIdentifierExpression node) {
        Variable foundVar = Utils.getVariableFromId(node.getId(), withinAFunction, symbolTable);

        // If the variable has not been defined print error
        if (foundVar == null) {
            System.out.println("Error " + (++errors) + ": The variable '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos() +
                    "', has not been defined yet!");
            return;
        }

        // If the variable is array and is going to be used in a arithmetic expression print error
        if (foundVar.getType().equals("ARR")) {
            if (node.parent() instanceof AAdditionExpression
                    || node.parent() instanceof AAbstractionExpression
                    || node.parent() instanceof AMultExpression
                    || node.parent() instanceof ADivExpression
                    || node.parent() instanceof AExpInBracketsExpression) {

                System.out.println("Error " + (++errors) + ": Cannot use array as whole in arithmetic expression or as array index. " +
                        "Variable used '" + node.getId().getText() +
                        "' in the line:" + node.getId().getLine() +
                        " pos: " + node.getId().getPos());
                return;
            }
        }

        // If the variable is string and is going to be used in arithmetic(except addition) print error
        if (foundVar.getType().equals("STR")) {
            if (node.parent() instanceof AAbstractionExpression
                    || node.parent() instanceof AMultExpression
                    || node.parent() instanceof ADivExpression
                    || node.parent() instanceof AExpInBracketsExpression) {
                System.out.println("Error " + (++errors) + ": Cannot use string variables in arithmetic expression or as array index. " +
                        "Variable used '" + node.getId().getText() +
                        "' in the line:" + node.getId().getLine() +
                        " pos: " + node.getId().getPos());
                return;
            }
        }
    }

    /* Expression in brackets Expression */

    @Override
    public void inAExpInBracketsExpression(AExpInBracketsExpression node) {
        Variable foundVar = Utils.getVariableFromId(node.getId(), withinAFunction, symbolTable);

        // If the type of the expression is string print error
        String type = Utils.getExpressionsType(node.getExpression(), withinAFunction, symbolTable);
        if(type.equals("STR")){
            System.out.println("Error " + (++errors) + ": Only Arithmetic Values are accepted to take part as an index of array," +
                    " and in line:" + node.getId().getLine() +
                    " the index is string");
        }

        // If the variable has not been defined print error
        if(foundVar == null){
            System.out.println("Error " + (++errors) + ": The variable '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos() +
                    "', has not been defined yet!");
            return;
        }

        // If the variable is not array, cannot be used as array
        if(!foundVar.getType().equals("ARR")){
            System.out.println("Error " + (++errors) + ": The array '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos() +
                    "', has been defined as a variable in the line:" + foundVar.getLine() +
                    " pos: " + foundVar.getPos());
        }
    }

    @Override
    public void caseAExpInBracketsExpression(AExpInBracketsExpression node) {
        inAExpInBracketsExpression(node);
        if(node.getExpression() != null){
            node.getExpression().apply(this);
        }
        outAExpInBracketsExpression(node);
    }

    /* Addition Expression */

    @Override
    public void inAAdditionExpression(AAdditionExpression node) {
        // Get the types of the nodes
        String leftType = Utils.getExpressionsType(node.getExp1(), withinAFunction, symbolTable);
        String rightType = Utils.getExpressionsType(node.getExp2(), withinAFunction, symbolTable);

        // If both are string then return
        if(leftType.equals("STR")
                && leftType.equals(rightType)){
            return;
        }

        // Else visit the nodes
        node.getExp1().apply(this);
        node.getExp2().apply(this);
    }

    @Override
    public void caseAAdditionExpression(AAdditionExpression node) {
        inAAdditionExpression(node);
        outAAdditionExpression(node);
    }

    /* Getters and Setters */

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }
}
