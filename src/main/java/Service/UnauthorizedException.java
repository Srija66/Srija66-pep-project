package Service;

public class UnauthorizedException extends RuntimeException  {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(Throwable cause){
        super(cause);
    }
    public UnauthorizedException(String message, Throwable cause){
        super(message,cause);
    }

}
