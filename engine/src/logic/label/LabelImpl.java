package logic.label;

import java.util.List;

public class LabelImpl implements Label {

    private final String label;

    public LabelImpl(int number) {
        label = "L" + number;
    }

    public String getLabelRepresentation() {
        return label;
    }

    @Override
    public List<Label> getLabels() {
        return labels;
    }
}
