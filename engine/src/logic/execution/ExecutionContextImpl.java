
package logic.execution;

import logic.Variable.Variable;
import logic.Variable.VariableImpl;
import logic.Variable.VariableType;
import logic.instruction.Instruction;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.program.Program;

import java.util.*;

public class ExecutionContextImpl implements ExecutionContext {
    private final Map<Variable, Long> variableState;
    private final Set<Label> labels;
    private final List<Instruction> activetedInstructions;
    private Map<String, Program> loadedPrograms;

    private static final Map<String, Program> globalProgramMap = new HashMap<>();
    private static final Map<String, String> globalXmlMap = new HashMap<>();


    public ExecutionContextImpl() {
        this.variableState = new HashMap<>();
        this.labels = new HashSet<>();
        this.activetedInstructions = new ArrayList<>();
        this.loadedPrograms = new HashMap<>();
    }


    public ExecutionContextImpl(Map<Variable, Long> variableState, Map<String,Program> programMap, Map<String, Program> loadedPrograms) {
        this.variableState = variableState;
        this.labels =  new HashSet<>();
        this.activetedInstructions = new ArrayList<>();

    }
    public ExecutionContextImpl(Map<Variable, Long> variableState) {
        this.variableState = variableState;
        this.labels = new HashSet<>();
        this.activetedInstructions = new ArrayList<>();
    }

    public static boolean hasProgram(String name) {
        return globalProgramMap.containsKey(name);
    }

    public static void loadProgram(Program program, String xml) {
        globalProgramMap.put(program.getName(), program);
        globalXmlMap.put(program.getName(), xml);
    }




    @Override
    public void setFunctionMap(Map<String, Program> functionMap) {
        if (functionMap == null || functionMap.isEmpty()) {
            System.out.println("setFunctionMap: received empty or null function map.");
            return;
        }

        for (Map.Entry<String, Program> entry : functionMap.entrySet()) {
            String funcName = entry.getKey();
            Program funcProg = entry.getValue();

            if (funcName == null || funcName.isBlank() || funcProg == null) {
                System.out.println("Skipping invalid function entry (null/blank).");
                continue;
            }

            if (globalProgramMap.containsKey(funcName)) {
                continue;
            }

            globalProgramMap.put(funcName, funcProg);
            System.out.println("Added function: " + funcName);
        }
    }




    @Override
    public long getVariableValue(Variable v) {
        return variableState.getOrDefault(v, 0L);
    }

    @Override
    public void updateVariable(Variable v, long value) {
        variableState.put(v, value);
    }

    @Override
    public Label findAvailableLabel() {
        int index = 1;
        Label newLabel;
        do {
            newLabel = new LabelImpl(index++);
        } while (labels.contains(newLabel));
        labels.add(newLabel);
        return newLabel;
    }

    @Override
    public Variable findAvailableVariable() {
        int index = 1;
        Variable candidate;
        do {
            candidate = new VariableImpl(VariableType.WORK, index++);
        } while (variableState.containsKey(candidate));
        variableState.put(candidate, 0L);
        return candidate;
    }

    @Override
    public Map<Variable, Long> getVariableState() {
        return variableState;
    }

    public void initializeVarsFromProgram(Program program) {
        for (Variable v : program.getVars()) {
            variableState.putIfAbsent(v, 0L);
        }
    }

    public void reset() {
        variableState.clear();
        labels.clear();
        activetedInstructions.clear();
        loadedPrograms.clear();

    }

    @Override
    public Program getProgramMap(String name) {
        return globalProgramMap.get(name);
    }


    @Override
    public boolean addProgram(Program newProgram) {
        String programName = newProgram.getName();

        if (loadedPrograms.containsKey(programName)) {
            throw new IllegalArgumentException("Program name already exists: " + programName);
        }

        for (String functionName : newProgram.getFunctionRefs()) {
            if (!globalProgramMap.containsKey(functionName)) {
                throw new IllegalArgumentException("Missing required function: " + functionName);
            }
        }

        for (String definedFunction : newProgram.getFunctionMap().keySet()) {
            if (globalProgramMap.containsKey(definedFunction)) {
                throw new IllegalArgumentException("Function '" + definedFunction + "' already exists in the system.");
            }
        }

        loadedPrograms.put(programName, newProgram);

        for (Map.Entry<String, Program> entry : newProgram.getFunctionMap().entrySet()) {
            globalProgramMap.put(entry.getKey(), entry.getValue());
        }

        return true;
    }

    public static boolean addGlobalProgram(Program program) {
        String name = program.getName();
        if (globalProgramMap.containsKey(name)) {
            return false;
        }
        globalProgramMap.put(name, program);
        return true;
    }



    @Override
    public Map<String, Program> getLoadedPrograms() {
        return loadedPrograms;
    }

    // ====== Static management of global function map ======

    public static void loadProgram(Program program) {
        globalProgramMap.put(program.getName(), program);
        if (program.getFunctionMap() != null) {
            globalProgramMap.putAll(program.getFunctionMap());
        }
    }



    public static Map<String, Program> getGlobalProgramMap() {
        return globalProgramMap;
    }

    public static String getXmlForProgram(String name) {
        if (name == null || name.isBlank()) {
            System.err.println("⚠️ getXmlForProgram called with null/blank name");
            return null;
        }

        if (globalXmlMap == null || globalXmlMap.isEmpty()) {
            System.err.println("⚠️ globalXmlMap is empty or not initialized");
            return null;
        }

        String key = name.trim();
        String xml = globalXmlMap.get(key);

        if (xml == null) {
            // ננסה גם lowercase אם השמות שונים רק באותיות
            xml = globalXmlMap.get(key.toLowerCase());
        }

        if (xml == null) {
            System.err.println("❌ XML not found in memory for program: " + key);
        } else {
            System.out.println("📄 XML found locally for program: " + key);
        }

        return xml;
    }


    public static void clearPrograms() {
        globalProgramMap.clear();
        globalXmlMap.clear();
    }
}

