package jvnobject;

import java.io.Serializable;

import server.JvnLocalServer;

//object of this class will be transmited by copy

public interface JvnObject extends Serializable {
	/**
	 * Get a Read lock on the object
	 * 
	 * @throws JvnException
	 **/
	public void jvnLockRead() throws JvnException;

	/**
	 * Get a Write lock on the object
	 * 
	 * @throws JvnException
	 **/
	public void jvnLockWrite() throws JvnException;

	/**
	 * Unlock the object
	 * 
	 * @throws JvnException
	 **/
	public void jvnUnLock() throws JvnException;



	public int jvnGetObjectId() throws JvnException;
	
	public String jvnGetObjectName() throws JvnException;

	public Serializable jvnGetRealObject() throws JvnException;
	
	public void jvnSetLocalServer(JvnLocalServer server);

	public void jvnSetRealObj(Serializable object);

	public void jvnInvalidateReader() throws JvnException;

	public Serializable jvnInvalidateWriter() throws JvnException;

	/**
	 * Reduce the Write lock of the JVN object
	 * 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader() throws JvnException;
	
	public Object getProxy(JvnObjImp handler);

}
