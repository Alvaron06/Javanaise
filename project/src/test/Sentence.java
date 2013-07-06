/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package test;

@SuppressWarnings("serial")
public class Sentence implements java.io.Serializable,SentenceInterface {
	public String data;
  
	public Sentence() {
		data = new String("");
	}
	
	@Override
	public void write(String text) {
		data = text;
	}
	@Override
	public String read() {
		return data;	
	}
	
}