import minipython.node.PExpression;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class Function
 * This class represents a function of our symbolTable
 */
public class Function {
    private String id;
    private int line;
    private int pos;
    private int nonDefaults;
    private int defaults;
    private HashMap<String, Variable> vars;
    private ArrayList<Variable> params;
    private String returnType;
    private PExpression returnNode;
    private String errorString;

    public Function() {
        nonDefaults = 0;
        defaults = 0;
        vars = new HashMap<>();
    }

    @Override
    public String toString() {
        String x = "FName: " + id + " Returns: " + returnType +  " Defaults " + defaults + " Non Defaults " + nonDefaults + " Vars: ";
        for(Variable var : params){
            x += "(id: " + var.getId() + " type: " + var.getType() + ") ";
        }
        return x;
    }

    /* Getters and Setters */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getNonDefaults() {
        return nonDefaults;
    }

    public void setNonDefaults(int nonDefaults) {
        this.nonDefaults = nonDefaults;
    }

    public int getDefaults() {
        return defaults;
    }

    public void setDefaults(int defaults) {
        this.defaults = defaults;
    }

    public HashMap<String, Variable> getVars() {
        return vars;
    }

    public void setVars(HashMap<String, Variable> vars) {
        this.vars = vars;
    }

    public ArrayList<Variable> getParams() {
        return params;
    }

    public void setParams(ArrayList<Variable> params) {
        this.params = params;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public PExpression getReturnNode() {
        return returnNode;
    }

    public void setReturnNode(PExpression returnNode) {
        this.returnNode = returnNode;
    }

    public String getErrorString() { return errorString; }

    public void setErrorString(String errorString) { this.errorString = errorString; }
}
