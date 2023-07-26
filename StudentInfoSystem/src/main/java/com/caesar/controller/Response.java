package main.java.com.caesar.controller;

public class Response {
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    private String state = OK;
    private String message = "";
    private String data;

    public Response() {

    }

    public Response(String state, String message, String data) {
        this.state = state;
        this.message = message;
        this.data = data;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "\"state\":\"" + state + '\"' +
                ",\"message\":\"" + message + '\"' +
                ",\"data\":" + data +
                '}';
    }
}
