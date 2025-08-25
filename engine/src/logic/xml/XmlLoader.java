package logic.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logic.jaxb.schema.generated.SProgram;

import java.io.File;

public class XmlLoader {

    public static SProgram loadFromFile(String path) {
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File file = new File(path);
            return (SProgram) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

}
