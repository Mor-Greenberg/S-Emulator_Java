package logic.xml;

import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.program.Program;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class XmlValidation {
    public static int validateXmlFilePath(String path) {
        File file = new File(path);


        if (!path.toLowerCase().endsWith(".xml")) {
            return  2;
        }
        if (!file.exists()) {
            return 1;
        }

        return 3;
    }
    public static boolean validateLabels(List<Instruction> instructions)  {
        Set<String> definedLabels = new HashSet<>();
        Set<String> referencedLabels = new HashSet<>();

        for (Instruction instr : instructions) {
            if (instr.getLabel() != null && !instr.getLabel().equals(FixedLabel.EMPTY)) {
                definedLabels.add(instr.getLabel().getLabelRepresentation());
            }

            // אסוף את כל ההפניות לתוויות
            if (instr instanceof GoToLabelInstruction gotoInstr) {
                referencedLabels.add(gotoInstr.getTargetLabel().getLabelRepresentation());
            } else if (instr instanceof JumpNotZeroInstruction jnzInstr) {
                referencedLabels.add(jnzInstr.getTargetLabel().getLabelRepresentation());
            } else if (instr instanceof JumpZeroInstruction jzInstr) {
                referencedLabels.add(jzInstr.getTargetLabel().getLabelRepresentation());
            }
        }

        // הגדרה: תוויות מיוחדות שלא חייבות להיות מוגדרות בפועל
        Set<String> specialLabels = Set.of("EXIT", "HALT");

        // בדוק רק תוויות רגילות
        for (String label : referencedLabels) {
            if (!definedLabels.contains(label) && !specialLabels.contains(label)) {
                System.err.println("Label not defined: " + label);  // עוזר לדיבוג
                return false;
            }
        }

        return true;
    }

}
