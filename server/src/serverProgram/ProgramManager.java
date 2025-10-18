package serverProgram;


import dto.ProgramStatsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramManager {
    private static final Map<String, ProgramStatsDTO> programs = new ConcurrentHashMap<>();

    public static void addProgram(ProgramStatsDTO dto) {
        programs.put(dto.getProgramName(), dto);
    }

    public static List<ProgramStatsDTO> getAllPrograms() {
        return new ArrayList<>(programs.values());
    }

    public static ProgramStatsDTO getProgram(String name) {
        return programs.get(name);
    }

    public static boolean hasProgram(String name) {
        return programs.containsKey(name);
    }
}