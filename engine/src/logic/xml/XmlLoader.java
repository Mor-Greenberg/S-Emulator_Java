package logic.xml;

import jakarta.xml.bind.*;
import jaxbV2.jaxb.v2.*;
import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.execution.ExecutionContext;
import logic.execution.ExecutionContextImpl;
import logic.instruction.*;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;
import logic.program.ProgramImpl;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
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
    public static Program fromXmlString(String xml) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("jaxbV2.jaxb.v2");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Object unmarshalled = unmarshaller.unmarshal(new StringReader(xml));

        SProgram sProgram;

        if (unmarshalled instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof SProgram sp) {
            sProgram = sp;
        } else if (unmarshalled instanceof SProgram sp) {
            sProgram = sp;
        } else {
            throw new IllegalArgumentException("Expected root element <S-Program> but got: " + unmarshalled.getClass().getSimpleName());
        }

        try {
            XmlMapper mapper = new XmlMapper(new ExecutionContextImpl());
            return mapper.map(sProgram, "FromString");
        } catch (Exception e) {
            System.err.println("Error during XmlMapper.map:");
            e.printStackTrace();
            throw e;
        }
    }
    public static String toXmlString(Program program) throws Exception {
        JAXBContext context = JAXBContext.newInstance("jaxbV2.jaxb.v2");
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(program, writer);
        return writer.toString();
    }



}









