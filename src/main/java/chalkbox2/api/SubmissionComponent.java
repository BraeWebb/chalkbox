package chalkbox2.api;

import java.util.HashMap;
import java.util.Map;

public class SubmissionComponent {
    public boolean failed;
    public boolean hasRun;
    public SubmissionError error;
    public Map<String, String> attributes = new HashMap<>();
}
