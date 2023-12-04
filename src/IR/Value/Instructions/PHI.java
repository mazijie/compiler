package IR.Value.Instructions;

import IR.Value.BasicBlock;

import java.io.IOException;

public class PHI extends Instruction{
    public PHI(InstructionType type, BasicBlock basicBlock) {
        super(type, basicBlock);
    }

    @Override
    public void print() throws IOException {

    }
}
