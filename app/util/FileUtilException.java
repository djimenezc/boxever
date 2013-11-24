package util;

/**
 * Use this exception when you work with files to indicate anomalous behaviors
 * 
 * @author david
 * 
 */
public class FileUtilException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileUtilException() {
		super();
	}

	public FileUtilException(final String message) {
		super(message);
	}

	public FileUtilException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FileUtilException(final Throwable cause) {
		super(cause);
	}

}
