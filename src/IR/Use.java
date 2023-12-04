package IR;

import IR.Value.User;
import IR.Value.Value;

public class Use {
    private Value value;
    private final User user;

    //pos表示value在user的operandList中的pos
    private final int pos;

    public Use(Value value, User user, int pos) {
        this.value=value;
        this.user=user;
        this.pos=pos;
    }
}
