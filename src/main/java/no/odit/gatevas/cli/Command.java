package no.odit.gatevas.cli;

public class Command {

    private String cmd;

    private String[] args;

    private boolean hasArgs;

    public Command(String input) {

        // Command with multiple arguments
        if (input.contains(" ")) {

            // Load first input word as command
            String[] split = input.split(" ");
            this.cmd = split[0];

            // Load arguments
            this.args = new String[split.length - 1];
            for (int i = 0; i < this.args.length; ) {
                this.args[i] = split[++i];
            }
            hasArgs = true;
        }

        // Command with zero additional arguments
        else {
            this.cmd = input;
            this.args = new String[0];
            hasArgs = false;
        }
    }

    public String getSub() {
        return hasArgs ? args[0] : new String();
    }

    public boolean hasArgs() {
        return hasArgs;
    }

    public void setHasArgs(boolean hasArgs) {
        this.hasArgs = hasArgs;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}