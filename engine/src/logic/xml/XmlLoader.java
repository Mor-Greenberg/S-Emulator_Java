package logic.xml;

import jaxbV2.jaxb.v2.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;

import java.io.File;
import java.util.*;

public class XmlLoader {
    public static SProgram loadFromFile(String path) {
        if (XmlValidation.validateXmlFilePath(path) == 1) {
            System.out.println("XML file does not exist");
            return null;
        } else if (XmlValidation.validateXmlFilePath(path) == 2) {
            System.out.println("XML file does not end with .xml");
            return null;
        }

        try {
            JAXBContext context = JAXBContext.newInstance("jaxbV2.jaxb.v2");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = new File(path);
            return (SProgram) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to load XML: " + path, e);
        }
    }
}









