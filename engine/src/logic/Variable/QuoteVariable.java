package logic.Variable;

import logic.instruction.QuoteInstruction;

public class QuoteVariable implements Variable {
    private final QuoteInstruction quote;

    public QuoteVariable(QuoteInstruction quote) {
        this.quote = quote;
    }

    public QuoteInstruction getQuote() {
        return quote;
    }

    public String getName() {
        return "QuoteVar(" + quote.getQuotedProgramName() + ")";
    }

    @Override
    public VariableType getType() {
        return null;
    }

    @Override
    public String getRepresentation() {
        return "";
    }
    public QuoteInstruction getQuoteInstruction() {
        return quote;
    }

}
