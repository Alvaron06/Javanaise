package test;

import java.io.Serializable;
import java.util.Date;

import jvnobject.JvnObjImp;
import jvnobject.JvnObject;
import server.JvnLocalServer;
import server.JvnServerImpl;

public class JvnServer1 {

	public static void main(String[] argv) throws Exception {

		JvnLocalServer jsi = JvnServerImpl.jvnGetServer();
		JvnObject jo = jsi.jvnCreateObject((Serializable) new Sentence());
		JvnObjImp joi = ((JvnObjImp) jo);
		joi.proxy = (SentenceInterface) jo.getProxy(joi);
		jsi.jvnRegisterObject("IRC", jo);
		
		
		int i = 0;
		
		
		long start = System.currentTimeMillis();
		long end ;
		
		String s = joi.proxy.read();
		
		for(;i<1000000;i++)
		{
			s = joi.proxy.read();
		}
		
		end = System.currentTimeMillis();
		double result = (end - start)/1000.0;
		
		System.out.println("----- " + result + " -----" );
		

	}

}
