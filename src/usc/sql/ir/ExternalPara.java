package usc.sql.ir;

public class ExternalPara extends Variable{

	String name;
	public ExternalPara(String name)
	{
		this.name = name;
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
}
