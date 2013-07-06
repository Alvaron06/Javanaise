/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package test;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import jvnobject.JvnException;
import jvnobject.JvnObjImp;
import jvnobject.JvnObject;
import server.JvnServerImpl;

public class Irc {
	public TextArea text;
	public TextField data;
	public TextField state;
	public Frame frame;
	public JvnObject sentence;

	/**
	 * main method create a JVN object nammed IRC for representing the Chat
	 * application
	 **/
	public static void main(String argv[]) {
		try {

			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			JvnObject jo = js.jvnLookupObject("IRC");

			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new Sentence());
				JvnObjImp joi = ((JvnObjImp) jo);
				// initilalize the proxy
				joi.proxy = (SentenceInterface) jo.getProxy(joi);
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			new Irc(jo);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * IRC Constructor
	 * 
	 * @param jo
	 *            the JVN object representing the Chat
	 * @throws JvnException
	 **/
	public Irc(JvnObject jo) {
		sentence = jo;
		frame = new Frame();
		frame.setLayout(new GridLayout(1, 1));
		text = new TextArea(10, 60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data = new TextField(40);
		frame.add(data);
		state = new TextField(20);
		frame.add(state);
		JvnObjImp tmp = (JvnObjImp) jo;
		state.setText("state : " + tmp.lock);
		tmp.irc = this;
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
		frame.setSize(545, 201);
		text.setBackground(Color.black);
		frame.setVisible(true);
	}
}

/**
 * Internal class to manage user events (read) on the CHAT application
 **/

class readListener implements ActionListener {
	Irc irc;

	public readListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed(ActionEvent e) {

		Thread readerThread = new Thread(new ReaderManner(this.irc));
		readerThread.start();

	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListener implements ActionListener {
	Irc irc;

	public writeListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed(ActionEvent e) {

		Thread writerThread = new Thread(new WriterManner(this.irc));
		writerThread.start();

	}
}

class ReaderManner implements Runnable {

	private Irc irc;

	public ReaderManner(Irc irc) {

		this.irc = irc;

	}

	@Override
	public void run() {

		try {
			String s = ((JvnObjImp) (irc.sentence)).proxy.read();
			JvnObjImp tmp = (JvnObjImp) irc.sentence;

			// irc.data.setText(s);
			irc.text.append(s + "\n");

			irc.state.setText("state : " + tmp.lock);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}

class WriterManner implements Runnable {

	private Irc irc;

	public WriterManner(Irc irc) {

		this.irc = irc;

	}

	@Override
	public void run() {

		try {
			String s = irc.data.getText();
			((JvnObjImp) (irc.sentence)).proxy.write(s);
			JvnObjImp tmp = (JvnObjImp) irc.sentence;
			irc.state.setText("state : " + tmp.lock);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
