package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import jvnobject.JvnException;
import jvnobject.JvnObjImp;
import jvnobject.JvnObject;
import coordinator.JvnRemoteCoord;

@SuppressWarnings("serial")
public class JvnServerImpl extends UnicastRemoteObject implements
		JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton per JVM
	private static JvnServerImpl js = null;

	// a remote reference to coordinator as a static member
	private JvnRemoteCoord jci = null;

	private HashMap<Integer, JvnObject> jvnobjList = new HashMap<Integer, JvnObject>();

	private JvnServerImpl() throws Exception {
		super();
		this.jci = getCoordinator();
	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * 
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws JvnException {
		// to be completed
	}

	/**
	 * creation of a JVN object register the object automaticlly
	 * 
	 * @param o
	 *            : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws JvnException {
		JvnObject jvno = new JvnObjImp(o);
		jvno.jvnSetLocalServer(JvnServerImpl.js);
		this.jvnobjList.put(jvno.jvnGetObjectId(), jvno);
//		this.jvnRegisterObject(jvno.jvnGetObjectName(), jvno);
		return jvno;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {

		try {
			this.jci.jvnRegisterObject(jon, jo);
		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws JvnException {

		try {
			JvnObject tmp = this.jci.jvnLookupObject(jon, js);
			if (tmp == null)
				return null;
			// ne pas oublier stocker dans le server l'objet récupérer
			else {
				tmp.jvnSetLocalServer(this);
				this.jvnobjList.put(tmp.jvnGetObjectId(), tmp);
				return (tmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 * @throws RemoteException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		try {
			return this.jci.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		try {
			return this.jci.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	/*
	 * cette méthode n'a pas besion de retourner un jvnobjet car un reader ne
	 * modife jamais l'état du objet
	 */
	synchronized public void jvnInvalidateReader(int joi)
			throws java.rmi.RemoteException, JvnException {

		this.jvnobjList.get(joi).jvnInvalidateReader();

	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	synchronized public Serializable jvnInvalidateWriter(int joi)
			throws java.rmi.RemoteException, JvnException {
		return this.jvnobjList.get(joi).jvnInvalidateWriter();

	}

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 *             ,JvnException
	 **/
	synchronized public Serializable jvnInvalidateWriterForReader(int joi)
			throws java.rmi.RemoteException, JvnException {
		return this.jvnobjList.get(joi).jvnInvalidateWriterForReader();

	}

	// method to get a singlton coordinator
	private static JvnRemoteCoord getCoordinator() {

		JvnRemoteCoord jrc = null;
		try {
			jrc = (JvnRemoteCoord) LocateRegistry.getRegistry().lookup(
					"coordinator");

		} catch (Exception e) {

			e.printStackTrace();
		}

		return jrc;
	}

	@Override
	public HashMap<Integer, JvnObject> getObjList() {

		return this.jvnobjList;
	}

	@Override
	public void updateObject(String name, Serializable newObject) {
		try {
			this.jci.updateRealObject(name, newObject);
		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	@Override
	public void updateGettedObject(int id, Serializable newObject) {

		this.jvnobjList.get(id).jvnSetRealObj(newObject);

	}
}