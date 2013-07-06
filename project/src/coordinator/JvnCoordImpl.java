package coordinator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import jvnobject.JvnException;
import jvnobject.JvnObjImp;
import jvnobject.JvnObject;
import server.JvnRemoteServer;

public class JvnCoordImpl implements JvnRemoteCoord {


	// list to manage jvn objects
	private HashMap<String, JvnObject> list = new HashMap<String, JvnObject>();

	// reader server owner list
	private HashMap<Integer, HashSet<JvnRemoteServer>> readerServerList = new HashMap<Integer, HashSet<JvnRemoteServer>>();

	// writer server owner
	private HashMap<Integer, JvnRemoteServer> writeLockOwner = new HashMap<Integer, JvnRemoteServer>();

	// server which done a lookup for a object
	private HashMap<String, HashSet<JvnRemoteServer>> getters = new HashMap<String, HashSet<JvnRemoteServer>>();

	// A JVN coordinator is managed as a singleton per JVM
	private static JvnCoordImpl jci = null;

	private static JvnCoordImpl jvnGetCoordinator() {
		if (jci == null) {
			try {
				jci = new JvnCoordImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return jci;
	}

	synchronized public int jvnGetObjectId() throws RemoteException,
			JvnException {

		return 0;
	}

	synchronized public void jvnRegisterObject(String jon, JvnObject jo)
			throws RemoteException, JvnException {

		// update "symbolic name - JvnObject" binding in list
		list.put(jon, jo);

	}

	synchronized public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
			throws RemoteException, JvnException {

		// enregister quels sont les servers qui ont fait lookup
		HashSet<JvnRemoteServer> set = this.getters.get(jon);
		if (null == set) {
			set = new HashSet<JvnRemoteServer>();
			set.add(js);
			this.getters.put(jon, set);
		} else
			set.add(js);

		if (this.list.containsKey(jon)) {
			return this.list.get(jon);
		} else
			return null;
	}

	synchronized public Serializable jvnLockRead(int joi, JvnRemoteServer js)
			throws RemoteException, JvnException {

		Serializable newObject = null;
		// si aucun server dispose du vérrou en écriture
		if (this.writeLockOwner.containsKey(joi) == false) {
			this.addToReaders(joi, js);

			/*
			 * s'il n'y a aucun writer en cours il suffit de retourner l'objet
			 * enregistré sur coordinateur cet objet a été déjà mise jours
			 */
			for (Iterator<Map.Entry<String, JvnObject>> iter = this.list
					.entrySet().iterator(); iter.hasNext();) {
				Map.Entry<String, JvnObject> tmp = iter.next();
				if (tmp.getKey().hashCode() == joi)
					newObject = tmp.getValue().jvnGetRealObject();
			}
		} else {
			JvnRemoteServer owner = this.writeLockOwner.get(joi);
			this.addToReaders(joi, js);
			/*
			 * s'il y a un writer en cours il faut faire
			 * "invalidateWriterForReader"
			 */
			newObject = owner.jvnInvalidateWriterForReader(joi);
			this.addToReaders(joi, owner);
			this.writeLockOwner.remove(joi);

		}
		return newObject;
	}

	synchronized public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
			throws RemoteException, JvnException {

		Serializable newObject = null;
		// si il y a un Writer en cours
		if (this.writeLockOwner.containsKey(joi) == true) {
			JvnRemoteServer owner = this.writeLockOwner.get(joi);
			// invalidate write lock
			newObject = owner.jvnInvalidateWriter(joi);
		}

		// si il y a des Readers
		if (this.readerServerList.containsKey(joi) == true) {

			HashSet<JvnRemoteServer> set = this.readerServerList.get(joi);
			for (JvnRemoteServer tmp : set) {
				if (tmp.equals(js) == false) {
					tmp.jvnInvalidateReader(joi);
				}
			}
			set.removeAll(set);

		}

		this.writeLockOwner.put(joi, js);

		return newObject;
	}

	private void addToReaders(int joi, JvnRemoteServer js) {

		HashSet<JvnRemoteServer> set = this.readerServerList.get(joi);

		if (null == set) {
			set = new HashSet<JvnRemoteServer>();
			set.add(js);
			this.readerServerList.put(joi, set);
		} else
			set.add(js);
	}

	@Override
	public void notifyAllGetters(JvnRemoteServer src, String key)
			throws RemoteException {
		//
		// if (this.getters.get(key) != null) {
		// for (JvnRemoteServer tmp : this.getters.get(key)) {
		// if (tmp.equals(src) == false) {
		// JvnObjImp obj = (JvnObjImp) this.list.get(key);
		// tmp.updateGettedObject(obj.jvnGetObjectId(),
		// obj.jvnGetRealObject());
		// }
		// }
		// }

	}

	@Override
	/*
	 * mettre à jour l'état d'un objet enregistré
	 */
	public void updateRealObject(String key, Serializable realObj)
			throws RemoteException {

		this.list.get(key).jvnSetRealObj(realObj);

	}

	// to boot the coordinator
	public static void main(String[] argv) {

		JvnCoordImpl jci = JvnCoordImpl.jvnGetCoordinator();
		try {
			JvnRemoteCoord j_stub = (JvnRemoteCoord) UnicastRemoteObject
					.exportObject(jci, 0);

			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind("coordinator", j_stub);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("coordinator ready....");

	}

	synchronized public void jvnTerminate(JvnRemoteServer js)
			throws RemoteException, JvnException {

	}
}
