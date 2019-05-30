package chalkbox.api.common;

public class ProcessExecution {
    private String output;
    private String error;

    public ProcessExecution() {
        output = "";
        error = "";
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }
}
