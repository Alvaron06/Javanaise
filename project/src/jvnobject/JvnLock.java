package jvnobject;

public enum JvnLock {
	
	NL(1),RLC(2),WLC(3),RLT(4),WLT(5),RTWC(6);
	
	private int id;
	
	JvnLock(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}

}
