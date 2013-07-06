package test;

import jvnobject.JvnObjImp;
import jvnobject.JvnObject;
import server.JvnLocalServer;
import server.JvnServerImpl;

public class JvnServer2 {

	public static void main(String[] argv) throws Exception {

		JvnLocalServer jsi = JvnServerImpl.jvnGetServer();

		JvnObject jo = jsi.jvnLookupObject("jvnObject1");
		jo.jvnSetLocalServer(jsi);

		System.out.println("server 2 : waiting for a write lock");
		jo.jvnLockWrite();
		System.out.println("server 2 : writeLock got");
		System.out.println("stat : " + ((JvnObjImp) (jo)).lock);
		Thread.sleep(15000);
		jo.jvnUnLock();
		System.out.println("server 2 : write data done");
		System.out.println("stat : " + ((JvnObjImp) (jo)).lock);

	}

}
