package com.samifying.plugin.atributes;

public class BackendError {
    private String name;
    private String message;
    private String path;

    public BackendError() {
    }

    public BackendError(String name, String message, String path) {
        this.name = name;
        this.message = message;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
