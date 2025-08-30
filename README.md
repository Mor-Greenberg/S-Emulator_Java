S-Emulator

Project Overview
S-Emulator is a Java-based simulator for S-language. It allows loading a program from an XML file, expanding synthetic instructions to basic ones according to a user-defined degree, and executing the program with output statistics and history.
System Structure
The project is divided into two modules:
1. console (User Interface and Menu Handling)
This module manages interaction with the user and includes the program's entry point and printing mechanisms.

- Main: Program entry point. Runs the menu.
- PrintMenu: Handles the UI for running programs and choosing execution or expansion.
- HandleExecution: Executes loaded and expanded programs.
- PrintExpansion: Handles how the expanded instructions are displayed.
- ProgramDisplay: Formats and prints program instructions, including synthetic expansion     in one line.
- InstructionFormat: Responsible for formatting instruction strings for display.

2. engine (Core Logic and Emulation)
This module handles the emulation logic, instruction expansion, and execution.

- execution
  - ProgramExecutor: Executes the instructions.
  - ExecutionContextImpl: Maintains program state (variables, labels, instructions activated).
- instruction
  - Instruction: Interface for all instruction types.
  - AbstractInstruction: Common base for instructions.
  - Synthetic instructions: AssignmentInstruction, DecreaseInstruction, IncreaseInstruction, etc.
  - Jump instructions: JumpEqualConstantInstruction, JumpNotZeroInstruction, etc.
- program
  - Program: Holds the list of instructions and variables, supports expansion.
  - ProgramImpl: Implementation.
- history
  - RunHistoryEntry: Represents a snapshot of the program state after each instruction.
- label
  - Label, FixedLabel: Represent labels in the program.
- xml
  - XmlLoader: Loads and parses the XML file describing the program.
  - XmlValidation: Ensures the XML is valid.
- Variable
  - Variable, VariableImpl: Represent variable names and values.

Features
- Load programs from XML files usig JAXB.
- Expand synthetic instructions based on a selected degree (0 = no expansion, full expansion = maximum degree).
- Execute programs step-by-step and print detailed execution log.
- Print program after expansion with proper formatting.
- Display the program state after each instruction.


