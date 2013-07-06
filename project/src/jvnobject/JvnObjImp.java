package jvnobject;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import server.JvnLocalServer;
import test.Irc;
import test.SentenceInterface;

@SuppressWarnings({ "serial" })
public class JvnObjImp implements JvnObject, InvocationHandler {

	public String name;
	private int id;
	private Serializable realObject;
	public JvnLock lock;
	public Irc irc;

	// a proxy object
	public SentenceInterface proxy;

	// should not be passed to remote side
	transient private JvnLocalServer server = null;

	public JvnObjImp(Serializable o) {

		this.realObject = o;
		this.name = "IRC";
		this.id = this.name.hashCode();
		this.lock = JvnLock.NL;

	}

	@Override
	public void jvnSetLocalServer(JvnLocalServer server) {

		this.server = server;

	}

	@Override
	public Serializable jvnGetRealObject() {
		return realObject;
	}

	@Override
	public int jvnGetObjectId() {
		return this.id;
	}

	@Override
	public String jvnGetObjectName() {
		return this.name;
	}

	@Override
	public void jvnLockRead() throws JvnException {

		switch (this.lock.getId()) {

		// NL
		case (1): {
			Serializable newObject = this.server.jvnLockRead(this.id);
			if (newObject != null)
				this.jvnSetRealObj(newObject);
			this.lock = JvnLock.RLT;
			break;
		}

		// RLC : cas de figure 4
		case (2): {

			this.lock = JvnLock.RLT;
			break;

		}

		// WLC -> RWC : cas de figure 5
		case (3): {

			this.lock = JvnLock.RTWC;
			break;
		}

		}

	}

	@Override
	public void jvnLockWrite() throws JvnException {

		switch (this.lock.getId()) {

		// NL or RLC
		case (1):
		case (2): {
			Serializable newObject = this.server.jvnLockWrite(this.id);
			if (newObject != null)
				this.jvnSetRealObj(newObject);
			this.lock = JvnLock.WLT;
			break;
		}

		// WLC -> directement ?WLT
		case (3): {
			this.lock = JvnLock.WLT;
			break;

		}

		}

	}

	@Override
	public void jvnUnLock() throws JvnException {

		synchronized (this) {
			switch (this.lock.getId()) {

			// RLT -> RLC
			case (4): {

				this.lock = JvnLock.RLC;
				notify();
				break;
			}

			// RTWC -> RLC
			case (6): {

				this.lock = JvnLock.RLC;
				notify();
				break;

			}

			// WLT -> WLC
			case (5): {
				this.server.updateObject(this.name, this.jvnGetRealObject());
				this.lock = JvnLock.WLC;
				notify();
				break;
			}

			}

		}
	}

	@Override
	// un client voulant 阾re writer fait appel ?cette m閠hodes
	public void jvnInvalidateReader() throws JvnException {

		synchronized (this) {

			while (this.lock == JvnLock.RLT) {
				try {
					wait();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			this.lock = JvnLock.NL;
			irc.state.setText("state : NL");
		}

	}

	@Override
	/*
	 * lorsque un autre client veut prendre le v閞rou en 閏riture cette m閠hode
	 * sera invoqu閑
	 */
	public Serializable jvnInvalidateWriter() throws JvnException {

		synchronized (this) {

			while (this.lock == JvnLock.WLT || this.lock == JvnLock.RTWC
					|| this.lock == JvnLock.RLT) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.lock = JvnLock.NL;
			irc.state.setText("state : NL");
		}

		return this.realObject;
	}

	@Override
	public Serializable jvnInvalidateWriterForReader() throws JvnException {

		synchronized (this) {

			while (this.lock == JvnLock.WLT) {

				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.lock = JvnLock.RLC;
			irc.state.setText("state : RLC");
		}
		return this.realObject;
	}

	@Override
	public void jvnSetRealObj(Serializable object) {

		this.realObject = object;

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		JvnAnnotation annotation = (JvnAnnotation) method
				.getAnnotation(JvnAnnotation.class);
		if (annotation.operation().equals("read")) {

			this.jvnLockRead();
		}
		if (annotation.operation().equals("write")) {

			this.jvnLockWrite();

		}

		Object obj = method.invoke(this.realObject, args);

		this.jvnUnLock();

		return obj;

	}

	@Override
	public Object getProxy(JvnObjImp handler) {

		Class<?> classType = handler.jvnGetRealObject().getClass();
		Class<?>[] interfaces = classType.getInterfaces();

		Object proxy = Proxy.newProxyInstance(classType.getClassLoader(),
				interfaces, handler);

		return proxy;

	}

}
