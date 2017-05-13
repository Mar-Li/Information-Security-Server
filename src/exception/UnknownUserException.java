package exception;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class UnknownUserException extends Exception{

    private String username;

    public UnknownUserException(String username) {
        super();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
