package serverProgram;

import logic.program.Program;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalFunctionsManager {

    // Global in-memory storage for all functions uploaded from all clients
    private static final Map<String, Program> globalFunctions = new ConcurrentHashMap<>();

    //Add a function to the global map (overwrites if same name already exists)
    public static void addFunction(Program function) {
        if (function != null && function.isFunction()) {
            globalFunctions.put(function.getName(), function);
        }
    }

    //Retrieve a specific function by name
    public static Program getFunction(String name) {
        return globalFunctions.get(name);
    }

    //Return a snapshot of all stored functions
    public static Map<String, Program> getAllFunctions() {
        return globalFunctions;
    }


}
