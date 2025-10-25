package serverProgram;

import dto.UserStatsDTO;
import logic.program.Program;
import logic.xml.XmlLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central hybrid storage for both raw XML and parsed Program objects.
 * Used for all users and all functions.
 */
public class GlobalProgramStore {

    private static final Map<String, String> xmlMap = new ConcurrentHashMap<>();
    private static final Map<String, Program> programCache = new ConcurrentHashMap<>();

    /** Add both XML and Program to the store */
    public static void addProgram(String name, String xmlContent, Program program) {
        if (name == null || xmlContent == null || program == null)
            return;
        xmlMap.put(name, xmlContent);
        programCache.put(name, program);
    }

    /** Add only Program (optional for internal loading) */
    public static void addProgram(Program program) {
        if (program != null) {
            programCache.put(program.getName(), program);
        }
    }

    /** Add only XML (for lightweight load) */
    public static void addXml(String name, String xmlContent) {
        if (name != null && xmlContent != null) {
            xmlMap.put(name, xmlContent);
        }
    }

    /** Get the Program object (parse if needed from XML) */
    public static Program getProgram(String name, String username) {
        if (name == null) return null;

        // try cache first
        Program program = programCache.get(name);
        if (program != null)
            return program;

        // fallback – parse from XML map if available
        String xml = xmlMap.get(name);
        if (xml != null) {
            try {
                program = XmlLoader.fromXmlString(xml,username);
                programCache.put(name, program);
                return program;
            } catch (Exception e) {
                System.err.println("Failed to parse XML for program: " + name);
                e.printStackTrace();
            }
        }
        return null;
    }

    /** Get only XML */
    public static String getXml(String name) {
        return xmlMap.get(name);
    }

    /** For debugging */
    public static Map<String, String> getXmlMap() {
        return xmlMap;
    }

    public static Map<String, Program> getProgramCache() {
        return programCache;
    }
}
