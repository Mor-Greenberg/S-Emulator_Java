package logic.program;

import logic.Variable.Variable;
import logic.execution.ExecutionContext;
import logic.instruction.AbstractInstruction;
import logic.instruction.Instruction;
import logic.label.Label;
import serverProgram.ProgramStats;
import user.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructions();
    Set<Variable> getVars();
    Set<Label> getLabels();
    int getNextIndexByLabel(Label nextLabel);
    void addVar(Variable variable);
    void addLabel(Label label) ;
     void setFunction(boolean isFunction) ;

     String getUploaderName() ;
    public void recordRun(int usedCredits) ;
     void setUploaderName(String uploaderName) ;


    boolean isFunction() ;
        void expandToDegree(int maxDegree, ExecutionContext context) ;
     boolean hasSyntheticInstructions();
     List<Instruction> getActiveInstructions();
     boolean validate();
     int calculateMaxDegree() ;
        int calculateCycles();
     List <AbstractInstruction> getExpandedInstructions();
    public boolean isMainProgram();
    Map<String, Program> getFunctionMap();
     void setFunctionMap(Map<String, Program> functionMap);
    public List<Instruction> getInstructionsLevel0();
    Set<String> getFunctionRefs();

    Map<Variable, Long> getVarsAsMapWithZeroes();

    int getRunCount();

    double getAverageCredits();
    public void setParentProgramName(String parentProgramName) ;
    public String getParentProgramName() ;

    boolean isMain();
}
