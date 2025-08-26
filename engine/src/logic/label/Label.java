package logic.label;

import java.util.ArrayList;
import java.util.List;

public interface Label {
    String getLabelRepresentation();
    List<Label> labels = new ArrayList<Label>();
    default List<Label> getLabels() {
        return labels;
    }

   // int getNumber();
}
