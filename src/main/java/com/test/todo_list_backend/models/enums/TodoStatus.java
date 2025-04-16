package com.test.todo_list_backend.models.enums;

public enum TodoStatus {

    COMPLETED(1),
    IN_PROGRESS(2),
    NOT_STARTED(3);

    private int status;

    private TodoStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public static TodoStatus fromValue(int value) {
        for (TodoStatus status : TodoStatus.values()) {
            if (status.getStatus() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }

}
