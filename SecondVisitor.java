import minipython.analysis.DepthFirstAdapter;
import minipython.node.*;
import java.util.ArrayList;

/**
 * Second Visitor
 * This class represents checks all the function calls and where are going to be used
 */
public class SecondVisitor extends DepthFirstAdapter {
    /* Our Symbol Table */
    private RootSymbolTable symbolTable;

    /* Error Counter */
    private int errors;

    /* The errors list */
    private ArrayList<String> errorMessages;

    /* The original parameters of the function, we do some tricks, so we need them */
    private ArrayList<String> originFunctionParamTypes;
    private ArrayList<String> assignWithFunction;

    /**
     * Constructor
     * @param symbolTable the symbol table
     * @param errors the int counter to continue with
     */
    public SecondVisitor(RootSymbolTable symbolTable, int errors) {
        this.symbolTable = symbolTable;
        this.errorMessages = new ArrayList<>();
        this.errors = errors;
        this.assignWithFunction = new ArrayList<>();
    }

    /* Function Calls */
    @Override
    public void inAFunctionStatement(AFunctionStatement node) {
        this.originFunctionParamTypes = new ArrayList<>();
        Function temp = Utils.checkLegitFunction(node.getId(), node.getArglist(), symbolTable, originFunctionParamTypes);
        if(temp.getParams() != null){
            for(int i = 0; i < temp.getParams().size(); i++){
                temp.getParams().get(i).setType(originFunctionParamTypes.get(i));
            }
        }

        if(temp.getErrorString() != null){
            if(!errorMessages.contains(temp.getErrorString())){
                errorMessages.add(temp.getErrorString());
                System.out.println("Error " + (++errors) + ": " + temp.getErrorString());
            }
        }

        if(temp.getReturnType() != null &&
                temp.getReturnType().equals("STR") &&
                (node.parent() instanceof AAbstractionExpression
                        || node.parent() instanceof ADivExpression
                        || node.parent() instanceof AMultExpression
                        || node.parent() instanceof AExpInBracketsExpression
                        || node.parent() instanceof AMinusEqualsStatement
                        || node.parent() instanceof ADivEqualsStatement)){
            String error = ": String value returned from function cannot be used in arithmetic or array index " +
                    "Function used '" + node.getId().getText() +
                    "' in the line:" + node.getId().getLine() +
                    " pos: " + node.getId().getPos();

            if(!errorMessages.contains(error)){
                errorMessages.add(error);
                System.out.println("Error " + (++errors) + error);
            }
        }
    }

    @Override
    public void inAFunctionExpression(AFunctionExpression node) {
        this.originFunctionParamTypes = new ArrayList<>();
        AFunctionCall functionCall = (AFunctionCall) node.getFunctionCall();
        Function temp = Utils.checkLegitFunction(functionCall.getId(), functionCall.getArglist(), symbolTable,originFunctionParamTypes);

        if(temp.getParams() != null){
            for(int i = 0; i < temp.getParams().size(); i++){
                temp.getParams().get(i).setType(originFunctionParamTypes.get(i));
            }
        }

        if(temp.getErrorString() != null){
            if(!errorMessages.contains(temp.getErrorString())){
                errorMessages.add(temp.getErrorString());
                System.out.println("Error " + (++errors) + ": " + temp.getErrorString());
            }
        }

        if(temp.getReturnType() != null &&
                temp.getReturnType().equals("STR") &&
                (node.parent() instanceof AAbstractionExpression
                        || node.parent() instanceof ADivExpression
                        || node.parent() instanceof AMultExpression
                        || node.parent() instanceof AExpInBracketsExpression
                        || node.parent() instanceof AMinusEqualsStatement
                        || node.parent() instanceof ADivEqualsStatement)){
            String error = ": String value returned from function cannot be used in arithmetic or array index " +
                    "Function used '" + functionCall.getId().getText() +
                    "' in the line:" + functionCall.getId().getLine() +
                    " pos: " + functionCall.getId().getPos();

            if(!errorMessages.contains(error)){
                errorMessages.add(error);
                System.out.println("Error " + (++errors) + error);
            }
        }
    }

    /* Equals Statement */

    @Override
    public void inAEqualsStatement(AEqualsStatement node) {
        int errorBeforeExpApplied = errors;
        node.getExpression().apply(this);
        if(errorBeforeExpApplied == errors){
            String typeOfExpToTheLeft = Utils.getExpressionsType(node.getExpression(), null, symbolTable);

            TId id = node.getId();
            Variable var = Utils.getVariableFromId(id, null, symbolTable);

            if(typeOfExpToTheLeft.equals("TYPECONFLICT")){
                System.out.println("Error " + (++errors) + ": Type Conflict, Addition Between String and INT" +
                        " in the line:" + node.getId().getLine() +
                        " pos: " + node.getId().getPos());

                if(var == null) return;

                var.setType(typeOfExpToTheLeft);
            }


            if(var == null){
                var = new Variable();
                var.setId(id.getText());
                var.setLine(id.getLine());
                var.setPos(id.getPos());
                var.setType(typeOfExpToTheLeft);
                symbolTable.getVariableHashMap().put(var.getId(), var);
                return;
            }

            if(!var.getType().equals(typeOfExpToTheLeft)){
                symbolTable.getVariableHashMap().get(id.getText()).setType(typeOfExpToTheLeft);
                return;
            }
        }

    }

    /* Addition Expression */

    @Override
    public void inAAdditionExpression(AAdditionExpression node) {
        String leftType = Utils.getExpressionsType(node.getExp1(), null, symbolTable);
        String rightType = Utils.getExpressionsType(node.getExp2(), null, symbolTable);

        if(leftType.equals("TYPECONFLICT") || rightType.equals("TYPECONFLICT")){
            System.out.println("Error " + (++errors) + ": The variable has not been defined" +
                    " in the line: '" + node.toString().trim() + "'"+ "left: " + leftType + " right: " + rightType);
        }else{
            if((leftType.equals("STR") && rightType.equals("INT"))
                    || (leftType.equals("INT") && rightType.equals("STR"))){
                System.out.println("Error " + (++errors) + ": Type Conflict, Addition Between String and INT" +
                        " in the line: '" + node.toString() + "'");
            }
        }

        node.getExp1().apply(this);
        node.getExp2().apply(this);
    }

    @Override
    public void caseAAdditionExpression(AAdditionExpression node) {
        inAAdditionExpression(node);
        outAAdditionExpression(node);
    }

    /* Identifier Expression
     *  */
    @Override
    public void inAIdentifierExpression(AIdentifierExpression node) {
        Variable foundVar = Utils.getVariableFromId(node.getId(), null, symbolTable);

        if(foundVar == null) return;
        if(!assignWithFunction.contains(foundVar.getId())) return;

        if(node.parent() instanceof AAdditionExpression
                || node.parent() instanceof AAbstractionExpression
                || node.parent() instanceof AMultExpression
                || node.parent() instanceof ADivExpression
                || node.parent() instanceof AExpInBracketsExpression){
            if(foundVar.getType().equals("STR")){
                String error = ": Cannot use string variables in arithmetic expression or as array index. " +
                        "Variable used '" + node.getId().getText() +
                        "' in the line:" + node.getId().getLine() +
                        " pos: " + node.getId().getPos();
                if(!errorMessages.contains(error)){
                    errorMessages.add(error);
                    System.out.println("Error " + (++errors) + error);
                }
                return;
            }
            if(foundVar.getType().equals("ARR")){
                String error = ": Cannot use array as whole in arithmetic expression or as array index. " +
                        "Variable used '" + node.getId().getText() +
                        "' in the line:" + node.getId().getLine() +
                        " pos: " + node.getId().getPos();
                if(!errorMessages.contains(error)){
                    errorMessages.add(error);
                    System.out.println("Error " + (++errors) + error);
                }
                return;
            }
            if(foundVar.getType().equals("UNDEF")){
                symbolTable.getVariableHashMap().get(node.getId().getText()).setType("INT");
            }
        }
    }

    /* Getters and Setters */

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }
}


