package com.test.todo_list_backend.models.enums;

public enum TodoPriority {

    EXTREME(3),
    MODERATE(2),
    LOW(1);

    private int priority;

    private TodoPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static TodoPriority fromValue(int value) {
        for (TodoPriority priority : TodoPriority.values()) {
            if (priority.getPriority() == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }

}
