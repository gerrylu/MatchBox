package mobitnt.util;

public abstract class EALooperTask{
	public EALooperTask(){
		m_object = null;
		m_iData = 0;
	}
	
	public EALooperTask(int i){
		m_iData = i;
	}

	public EALooperTask(int i,Object o){
		m_iData = i;
		m_object = o;
	}
	
	protected Object m_object;
	protected int m_iData;
	public abstract void run();
}
