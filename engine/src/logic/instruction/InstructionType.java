package logic.instruction;

public enum InstructionType {
    B('B'),
    S('S');

    private final char symbol;

    InstructionType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }
}
