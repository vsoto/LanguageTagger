package language_id.com.detectLanguage.errors;

@SuppressWarnings("serial")
public class APIError1 extends Exception {
    public int code;

    public APIError1(String message, int code) {
        super(message);
        this.code = code;
    }
}