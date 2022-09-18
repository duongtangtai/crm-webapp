package cybersoft.java18.model;

import lombok.Data;

@Data
public class ResponseData {
    private int statusCode;
    private boolean successful;
    private String message;
    private Object content;
    public ResponseData statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }
    public ResponseData successful(boolean successful) {
        this.successful = successful;
        return this;
    }
    public ResponseData message(String message) {
        this.message = message;
        return this;
    }
    public ResponseData content(Object content) {
        this.content = content;
        return this;
    }
}
