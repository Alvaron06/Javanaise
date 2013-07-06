package test;

import jvnobject.JvnAnnotation;

public interface SentenceInterface {
	
	@JvnAnnotation(operation="write")
	public void write(String text);
	
	@JvnAnnotation(operation="read")
	public String read();
	
}
