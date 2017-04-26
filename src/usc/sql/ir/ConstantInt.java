package usc.sql.ir;

public class ConstantInt extends Variable {

	String value;
	public ConstantInt(String value)
	{
		this.value = value;
	}
	@Override
	public String getValue() 
	{
		return value;
	}
	@Override
	public String toString()
	{
		return "\""+value+"\"";
	}
}
