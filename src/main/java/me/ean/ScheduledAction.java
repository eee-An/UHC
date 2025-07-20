package me.ean;

import java.time.Duration;
import java.util.Map;

public class ScheduledAction {
    private final Duration time;
    private final String action;
    private final Map<String, Object> params;

    public ScheduledAction(Duration time, String action, Map<String, Object> params) {
        this.time = time;
        this.action = action;
        this.params = params;
    }

    public Duration getTime() { return time; }
    public String getAction() { return action; }
    public Map<String, Object> getParams() { return params; }
}