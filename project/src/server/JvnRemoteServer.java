package server;

import java.io.Serializable;
import java.rmi.Remote;

import jvnobject.JvnException;
import jvnobject.JvnObject;

public interface JvnRemoteServer extends Remote{
	
	/**
	* Invalidate the Read lock of a JVN object 
	* @param joi : the JVN object id
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,JvnException;
	    
	/**
	* Invalidate the Write lock of a JVN object 
	* @param joi : the JVN object id
	* @return the current JVN object state 
	* @throws java.rmi.RemoteException,JvnException
	**/
        public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,JvnException;
	
	/**
	* Reduce the Write lock of a JVN object 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,JvnException;

	
	public void updateGettedObject(int id,Serializable newObject)throws java.rmi.RemoteException;
	

}
