package SYSCALL;

import java.util.List;

public class Syscall {
    private SyscallType type;
    private List<String> args;

    public Syscall(SyscallType type, List<String> args) {
        this.type = type;
        this.args = args;
    }

    public SyscallType getType() {
        return type;
    }

    public List<String> getArgs() {
        return args;
    }
}
