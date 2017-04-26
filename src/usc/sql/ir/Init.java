package usc.sql.ir;

import java.util.ArrayList;
import java.util.List;

public class Init extends Variable{
	String name;
	String line;

	List<Variable> initVar = new ArrayList<>();
	
	public Init(String name,String line)
	{
		this.name = name;
		this.line = line;
		
	}
	
	public Init(String name,String line,List<Variable> initVar)
	{
		this.name = name;
		this.line = line;
		this.initVar = initVar;
		//initVar.addAll(initVar);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	public String getLine()
	{
		return line;
	}
	public List<Variable> getInitVar()
	{
		return initVar;
	}
	public void setInitVar(List<Variable> initVar)
	{
		this.initVar = initVar;
		
	}
	@Override
	public String getValue() {
		return "@"+name;
	}
	

	@Override
	public String toString()
	{
		
		if(initVar.isEmpty())
			return "\""+name+"\""+" "+line;
		else
		{
			
		
			//return "\""+name+"\"";
			return initVar.toString();
		}
			
	}
}
