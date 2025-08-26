package logic.xml;

import logic.instruction.Instruction;
import logic.label.Label;
import logic.program.Program;

import java.io.File;
import java.util.Optional;

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
    private boolean isValidLabel(String label) {
        return label.equals("EXIT") || label.matches("L\\d+");
    }

}
