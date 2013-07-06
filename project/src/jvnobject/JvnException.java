package jvnobject;


@SuppressWarnings("serial")
public class JvnException extends Exception {
	String message;

	public JvnException() {
	}

	public JvnException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
