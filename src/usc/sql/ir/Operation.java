package usc.sql.ir;

public class Operation extends Variable{
	private String name;
	public Operation(String name) {
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
	@Override
	public String toString()
	{
		return "\""+name+"\"";
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
