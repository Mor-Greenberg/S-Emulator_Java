package logic.Variable;

public class VariableImpl implements Variable {
    private final VariableType type;
    private int number;

    public VariableImpl(VariableType type) {
        this.type = type;
    }

    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }
    @Override
    public String toString(){
        return type.getVariableRepresentation(number);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableImpl that)) return false;
        return number == that.number && type == that.type;
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + number;
    }

}
