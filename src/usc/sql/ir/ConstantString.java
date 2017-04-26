package usc.sql.ir;

public class ConstantString extends Variable{

	String value;
	public ConstantString(String value)
	{
		this.value = value;
	}
	@Override
	public String getValue() {
		return replaceLast(value.replaceFirst("\"", ""),"\"","");
	}
	@Override
	public String toString()
	{
		return "\"\\\""+getValue()+"\\\"\"";
	}
	public String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}
}
