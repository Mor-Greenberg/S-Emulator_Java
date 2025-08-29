package logic.label;


public class LabelImpl implements Label {

    private final String label;
    private int number;

    public LabelImpl(int number) {
        this.number = number;
        label = "L" + number;
    }


    public String getLabelRepresentation() {
        String strNum = String.valueOf(number);
        String strLabel = "L" + strNum;

        return strLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelImpl)) return false;
        LabelImpl that = (LabelImpl) o;
        return this.number == that.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }


    @Override
    public String toString() {
        return label;
    }

}
