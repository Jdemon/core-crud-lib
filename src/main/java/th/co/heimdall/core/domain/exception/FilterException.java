package th.co.heimdall.core.domain.exception;

public class FilterException extends RuntimeException{
    public FilterException(Exception e) {
        super(e);
    }
}
