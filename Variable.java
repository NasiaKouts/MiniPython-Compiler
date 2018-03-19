/**
 * Class Variable
 * This class represents a variable which is used in expressions, statements etc.
 */
public class Variable {
    private String id;
    private int line;
    private int pos;

    // hasDefault is by default false. If the variable has a default value it will be changed to true.
    private boolean hasDefault = false;

    /* type is either INT for int num,
     *                STR for string,
     *                INT_ARR for array of integers,
     *                STR_ARR for array of strings,
     *                UNDEF_ARR for array of undefined types,
     *                UNDEF for undefined, if it is a funtion's parame for example, with no default value
     */
    private String type;

    // only one of the two following takes value, depending on which is the type of the variable.
    private int defIntValue;
    private String defStringValue;

    public Variable(){
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

    public boolean isHasDefault() {
        return hasDefault;
    }

    public void setHasDefault(boolean hasDefault) {
        this.hasDefault = hasDefault;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDefIntValue() {
        return defIntValue;
    }

    public void setDefIntValue(int defIntValue) {
        this.defIntValue = defIntValue;
    }

    public String getDefStringValue() {
        return defStringValue;
    }

    public void setDefStringValue(String defStringValue) {
        this.defStringValue = defStringValue;
    }
}
