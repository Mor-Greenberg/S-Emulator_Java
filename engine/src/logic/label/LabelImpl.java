package logic.label;

import java.util.List;
import java.util.Objects;

public class LabelImpl implements Label {

    private final String label;
    private int number;

    public LabelImpl(int number) {
        label = "L" + number;
    }
    public int getNumber() {
        return number;
    }

    public String getLabelRepresentation() {
        return label;
    }

    @Override
    public List<Label> getLabels() {
        return labels;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelImpl)) return false;
        LabelImpl that = (LabelImpl) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return label;
    }

}
