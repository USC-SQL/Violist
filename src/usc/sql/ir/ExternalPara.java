package usc.sql.ir;

import soot.Unit;

public class ExternalPara extends Variable{

	String name;
	String containingMethod;
	int sourceLineNumber;
	int byteCodeOffset;
	
	//public ExternalPara(String name, Unit statement)
	{
		this.name = name;
	}
	
	public ExternalPara(String name, String containingMethod, int sourceLineNum, int bytecodeOffSet)
	{
		this.name = name;
		this.containingMethod = containingMethod;
		this.sourceLineNumber = sourceLineNum;
		this.byteCodeOffset = bytecodeOffSet;
	}
	
	public String getName()
	{
		return name;
	}

	@Override
	public String getValue() {
		return "@"+name;
	}
	
	@Override
	public String toString()
	{
		return "\""+name+"\"";
	}
	
	public String getContainingMethod()
	{
		return containingMethod;
	}

	public int getSourceLineNum()
	{
		return sourceLineNumber;
	}
	
	public int getBytecodeOffSet()
	{
		return byteCodeOffset;
	}
}
