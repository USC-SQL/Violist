package usc.sql.ir;

import java.util.List;

public class Expression extends Variable{

	List<List<Variable>> operands;
	Operation op;
	
	public Expression(List<List<Variable>> operands,Operation op)
	{
		this.operands = operands;
		this.op = op; 
	}
	public String getValue()
	{

		//	if(op.getName().equals("append"))
		//		return operands.get(0).getValue()+operands.get(1).getValue();
		
		return ":D";
	}
	public void setOperands(List<List<Variable>> operands)
	{
		this.operands = operands;
	}
	public List<List<Variable>> getOperands()
	{
		return operands;
	}
	public Operation getOperation()
	{
		return op;
	}
	
	@Override
	public String toString()
	{
		String output = op.toString()+ " ";
		
		for(List<Variable> vl:operands)
		{//System.out.println(v);
			for(int i=0;i<vl.size();i++)
			{
				if(i==vl.size()-1)
					output+=vl.get(i).toString()+" "; 
				else
					output+=vl.get(i).toString()+"|";
			}
		}
		return output;
	}
	@Override
	public int getSize()
	{
		int size = 0;
		for(List<Variable> vl:operands)
		{

			for(int i=0;i<vl.size();i++)
			{
				
					size+= vl.get(i).getSize();
			}
			
		}
		return size;
	}

}
