package jvnobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)// for introspecting
@Target(ElementType.METHOD)
public @interface JvnAnnotation {
	
	String operation();

}
