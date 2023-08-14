package ru.andrew.fileserver.util;

public class CustomHTTPError {
    private int statusCode;
    private String message;

    public CustomHTTPError(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": ");
        sb.append(statusCode);
        sb.append(", \"message\": ");
        sb.append(message);
        sb.append("}");
        return sb.toString();
    }
}
