import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class RootSymbolTable
 * This class represents our symbolTable
 */
public class RootSymbolTable {

    // Global Variables
    private HashMap<String, Variable> variableHashMap;

    // Functions
    private ArrayList<Function> functionsArray;

    public RootSymbolTable() {
        variableHashMap = new HashMap<>();
        functionsArray = new ArrayList<>();
    }

    public HashMap<String, Variable> getVariableHashMap() {
        return variableHashMap;
    }

    public void setVariableHashMap(HashMap<String, Variable> variableHashMap) {
        this.variableHashMap = variableHashMap;
    }

    public void addVariable(String key, Variable var){
        variableHashMap.put(key, var);
    }

    public ArrayList<Function> getFunctionsArray() {
        return functionsArray;
    }

    public void setFunctionsArray(ArrayList<Function> functionsArray) {
        this.functionsArray = functionsArray;
    }

    public void addFunction(Function fun){
        functionsArray.add(fun);
    }

    // Returns all the functions with the given id
    public ArrayList<Function> getAllFunctions(String id){
        ArrayList<Function> tempFunctions = new ArrayList<>();
        for(Function temp : functionsArray){
            if(temp.getId().equals(id)){
                tempFunctions.add(temp);
            }
        }
        return tempFunctions;
    }
}
