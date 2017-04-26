package usc.sql.ir;

public class T extends Variable{

	private Variable v;

	private String varName;
	private int regionNum;
	private int k;
	private boolean isFi;
	private String line;
	
	public T(Variable v,String varName,int regionNum,String line)
	{
		
		this.v = v;
		this.varName = varName;
		this.regionNum = regionNum;
		this.line = line;
	}
	public T(Variable v,String varName,int regionNum,int k,boolean isFi,String line)
	{	
		this.v = v;
		this.varName = varName;
		this.regionNum = regionNum;
		this.k = k;
		this.isFi = isFi;
		this.line = line;
	}	
	public String getLine()
	{
		return line;
	}
	public boolean isFi() {
		return isFi;
	}
	public void setFi(boolean isFi) {
		this.isFi = isFi;
	}
	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
	}
	public void setVariable(Variable v) {
		this.v = v;
	}
	public Variable getVariable()
	{
		return v;
	}
	public String getTVarName()
	{
		return varName;
	}
	public int getRegionNumber()
	{
		return regionNum;
	}
	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}
    public String toTString()
    {
    	if(isFi)
    		return "F"+" "+varName+" "+regionNum+ " s="+k;
    	else
    		return "T"+" "+varName+" "+regionNum+ " s="+k;
    }
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(isFi)
			return "F"+" "+varName+" "+regionNum+" s="+k+" "+line+" "+v.toString() ;
		else
			return "T"+" "+varName+" "+regionNum+" s="+k+" "+line+" "+v.toString() ;
		//return "T"+" "+v.toString();
	}
	
	/*
	@Override
	public int hashCode() {
		
		int result = varName.hashCode()+regionNum+k;
		
		return result;
	}
	*/
}
