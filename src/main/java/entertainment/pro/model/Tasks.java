package entertainment.pro.model;

import java.util.regex.Pattern;

public abstract class Tasks {
    protected String description;
    protected String type;
    protected boolean done;

    /**
     * Represents a duke.task in a todolist.
     * A duke.task can have description, type and whether is it done or not.
     */
    public Tasks(String description, String type) {
        this.description = description;
        this.type = type;
        this.done = false;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getStatusIcon() {
        return (done ? "\u2713" : "\u2718"); //return tick or X symbols
    }

}

