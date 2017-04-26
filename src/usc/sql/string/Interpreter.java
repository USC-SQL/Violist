package usc.sql.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import usc.sql.ir.*;

public class Interpreter {

	private Translator t;
	private int maxLoop;
	private Set<Variable> target;
	Map<String,Set<String>> fieldMap;
	//private Map<Integer,Map<String,List<Set<String>>>> tList = new HashMap<>();
	private Map<Variable,List<Set<String>>> fiList = new HashMap<>();
	private Map<Variable,List<Set<String>>> tList = new HashMap<>();
	public Interpreter(Translator t,int maxLoop)
	{
		this.t =t;
		this.maxLoop = maxLoop;
	}
	public Interpreter(Set<Variable> target,int maxLoop)
	{
		this.target = target;
		this.maxLoop = maxLoop;
		setValueToIR();
	}
	public Interpreter(Set<Variable> target,Map<String,Set<String>> fieldMap,int maxLoop)
	{
		this.target = target;
		this.maxLoop = maxLoop;
		this.fieldMap = fieldMap;
		setValueToIR();
	}
	public Set<String> getValue(String line)
	{
		Set<String> s = new HashSet<>();
		
		if(t.getTranslatedIR(line)!=null)
		for(Variable ir: t.getTranslatedIR(line))
		{
			s.addAll(interpret(ir,maxLoop));
		}
		return s;
			
	}

	public Set<String> getValueForIR()
	{
		Set<String> s = new HashSet<>();
		for(Variable ir: target)
		{
			//System.out.println(ir);
			s.addAll(ir.getInterpretedValue());
		}
		return s;
	}

	private void setValueToIR()
	{
		
		for(Variable ir: target)
		{
			//System.out.println(ir);
			Set<String> s = new HashSet<>();
			s.addAll(interpret(ir,maxLoop));
			ir.setInterpretedValue(s);
		}
	}
	private Set<String> interpretField(String fieldName)
	{
		Set<String> s = new HashSet<>();
		if(fieldMap==null)
			s.add("Unknown@FIELD");
		else
		{
			if(fieldMap.get(fieldName)!=null)
			{
				for(String temp:fieldMap.get(fieldName))
				{
					if(temp.matches("<.*>"))
						s.add("Unknown@FIELD");
					else
						s.add(temp);
				}
				//s.addAll(fieldMap.get(((ExternalPara) ir).getName()));
			}
			else 
				s.add("Unknown@FIELD");
				//s.add(fieldName);
		}
		return s;
	}
	
	private void parseTT(T t,int itr)
	{
		
		
		if(!tList.containsKey(t))
		{
			tList.put(t, new ArrayList<Set<String>>());			
		}
		else
		{
			//avoid making extra parseTT due to the initial variable problem
			if(tList.get(t).size()>=itr+1)
			{
				if(t.isFi())
					return;
				else
					tList.get(t).clear();
			}
			//return;
		}
		
		
		Variable ir = t.getVariable();
		String name = t.getTVarName();
		int regionNum = t.getRegionNumber();
		String line = t.getLine();
		if(itr==0)
		{
			
			tList.get(t).add(interpret(ir,itr));

		}
		else
		{
	
			setInitOfInternal(name,regionNum,line,ir,itr,t);
			//System.out.println(tList.get(t).get(itr-dif));

			tList.get(t).add(interpret(ir,itr));
			//if(t.getTVarName().equals("r2"))
			//System.out.println("ar1:"+t.getLine()+" "+tList.get(t));
		}
	}
	//We use ""
	private Set<String> interpret(Variable ir,int itr)
	{
		if(ir instanceof ConstantInt)
		{
			

			Set<String> s = new HashSet<>();
			//s.add(ir.getValue());
			
			try
			{
				String temp = ""+Integer.parseInt(ir.getValue());
				if(temp.length()>0)
					s.add( temp);				
			}
			catch(NumberFormatException e) { 
		    //   e.printStackTrace();
				s.add(ir.getValue());
		    }
			return s;
		}
		if(ir instanceof ConstantString)
		{
			Set<String> s = new HashSet<>();
			s.add(ir.getValue());
			return s;
		}
	
		else if(ir instanceof ExternalPara)
		{
			Set<String> s = new HashSet<>();
			String externalName = ((ExternalPara) ir).getName();
			//field
			if(externalName.matches("<.*>"))
			{
				s.addAll(interpretField(externalName));
			}
			//para
			else if(externalName.contains("@parameter"))
				s.add("Unknown@PARA@"+externalName.substring(externalName.indexOf("<"),externalName.lastIndexOf(">")+1));
			//method
			else if(externalName.contains("<"))
				s.add("Unknown@METHOD@"+externalName.substring(externalName.indexOf("<"),externalName.indexOf(">")+1));
			else
				s.add(externalName);
			return s;
		}
		else if(ir instanceof Init)
		{			
			Set<String> s = new HashSet<>();
			if(itr != 0)
				return s;
			else
			{
				for(Variable v:((Init) ir).getInitVar())
					s.addAll(interpret(v,itr));
				//((Init) ir).setInitVar(null);
				return s;
			}
			
		}
		else if(ir instanceof Expression)
		{
			switch(((Expression) ir).getOperation().getName()) 
			{
				case "append":
					return plusInterpreter((Expression) ir,itr);
				case "replaceAll":
					return replaceAllInterpreter((Expression)ir,itr);
				case "replaceFirst":
					return replaceFirstInterpreter((Expression)ir,itr);
				case "replace(java.lang.CharSequence,java.lang.CharSequence)":
					return replaceCharSequenceInterpreter((Expression)ir,itr);
				case "replace(char,char)":
					return replaceCharInterpreter((Expression)ir,itr);
				case "toUpperCase":
					return toUpperInterpreter((Expression)ir,itr);
				case "toLowerCase":
					return toLowerInterpreter((Expression) ir,itr);
				case "trim":
					return trimInterpreter((Expression) ir,itr);
				
				/*
				case "CastChar":
					return castInterpreter((Expression) ir,itr);
				case "contains":
					return containsInterpreter((Expression) ir,itr);
				case "substring(int)":
					return substringInterpreter((Expression) ir,itr);
				case "charAt":
					return charAtInterpreter((Expression) ir,itr);
				case "toCharArray":
					return toCharArrayInterpreter((Expression) ir,itr);
				*/
					
				case "encode":
					return encodeInterpreter((Expression) ir,itr);
				default:
					return new HashSet<String>();
			}
			
		}
		
		else if(ir instanceof T)
		{

			if(!((T) ir).isFi())
			{
				//find immediate T or Fi parent
			
				
				for(int i=0;i<maxLoop;i++)
					parseTT((T) ir,i);
				
				
				Set<String> s = new HashSet<>();
				for(Set<String> tmp: tList.get(ir))
				{
					
					s.addAll(tmp);
				}
				return s;
				
			}
			else
			{
				//find immediate T or Fi parent
				int temp = -1;
				Variable parent = ir.getParent();
				while(parent!=null)
				{
					if(parent instanceof T)
					{
						temp = itr-((T)parent).getK()+((T)ir).getK();
						break;
					}
					else
						parent = parent.getParent();
				}
				//System.out.println(temp);
				//int temp = itr+((T)ir).getK();//System.out.println("itr"+ itr);
				if(temp<0)
				{
					//System.out.println("oh no:"+ir);
					return new HashSet<>();
					/*
					Set<String> value = new HashSet<>();
					Variable e = ir.getParent();
					if(e instanceof Expression)
					{
						List<Variable> target = new ArrayList<>();
						for(List<Variable> opList: ((Expression) e).getOperands())
						{
							if(opList.contains(ir))
							{
								
								target.addAll(opList);
								target.remove(ir);
								
								for(Variable init: target)
								{
									// !!!might be a trouble here, make extra interpret
									if(init instanceof Init)
										value.addAll(interpret(init,0));
								}
								break;
							}
						}
						
					}
					return value;*/
				}
				else
				{
										
					parseTT((T) ir,temp);				
					//System.out.println(temp+" "+tList.get(ir).get(temp));
					//System.out.println(temp+" "+tList.get(ir).get(temp-1));
					return tList.get(ir).get(temp);
				}
				

			}
		}
		else if(ir instanceof InternalVar)
		{			
				return ((InternalVar) ir).getInitValue();
		}
		else
			return null;
	}
	



	private Set<String> toCharArrayInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
			{
				for(int i=0;i<value1.length();i++)
					s.add(value1.charAt(i)+"");
			}
		}
		return s;
	}
	private Set<String> charAtInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(String value1:s1)
					for(String value2: s2)
					{
						
						
						if(value1.equals("null"))
							continue;
						value1 = ""+ (int)value1.charAt(0);
						
						//Can only work in JSA benchmark
						
						int temp = value2.charAt(0);
						//System.out.println(temp);
						//System.out.println(value1+" charatatat "+temp);
						try
						{
						s.add(""+value1.charAt(temp));
						}
						catch(IndexOutOfBoundsException e)
						{
							
						}
					}
						
			}
			
		}
		
		return s;
	}
	private Set<String> substringInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(String value1:s1)
					for(String value2: s2)
					{
						if(value1.equals("null"))
							continue;
						
						int temp = value2.charAt(0);
						//System.out.println(temp);
						s.add(value1.substring(temp));
					}
						
			}
			
		}
		
		return s;
	}
	private Set<String> plusInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		Set<String> s1 = new HashSet<>();
		Set<String> s2 = new HashSet<>();
		for(Variable v1: op1)
			s1.addAll(interpret(v1,itr));
		for(Variable v2: op2)
			s2.addAll(interpret(v2,itr));
		if(s1.isEmpty()||s2.isEmpty())
			return s;
		else
		{
			for(String value1:s1)
			{	
				for(String value2: s2)
				{
					s.add(value1+value2);
				}
			}
			return s;
				
		}
		/*
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(String value1:s1)
					for(String value2: s2)
					{
						//System.out.println("PPP:"+value1+value2);
						
						//String temp1 = value1;
						//String temp2 = value2;
						try
						{
						//	temp1 = ""+(char)Integer.parseInt(temp1);
					
							
						}
						catch(NumberFormatException e) { 
					    //   e.printStackTrace();
					    }
						try
						{
			
						//	temp2 = ""+(char)Integer.parseInt(temp2);
							
						}
						catch(NumberFormatException e) { 
					     //  e.printStackTrace();
					    }
						//s.add(temp1+temp2);

	
							
						s.add(value1+value2);
						//value1 = null;
						//value2 = null;
					}
						
			}
			
			
		}
		
		return s;
		*/
	}
	private Set<String> containsInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(String value1:s1)
					for(String value2: s2)
					{
						boolean temp = value1.contains(value2);
						if(temp)
						s.add("true");
						else
							s.add("false");
						//s.add(value1+value2);
					}
						
			}
			
		}
		
		return s;
	}
	private Set<String> trimInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
				s.add(value1.trim());
		}
		return s;
	}
	private Set<String> encodeInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
				s.add("###@@@<java.net.URLEncoder: java.lang.String encode(java.lang.String,java.lang.String)>"+value1+"@@@###");
		}
		return s;
	}
	private Set<String> castInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
			{
				try
				{
				int tmp = Integer.parseInt(value1);
				s.add(""+(char)tmp);
				}
				catch(NumberFormatException e)
				{
					//e.printStackTrace();
					s.add(value1);
				}
				
			}
		}
		return s;
	}
	private Set<String> toLowerInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
				s.add(value1.toLowerCase());
		}
		return s;
	}

	private Set<String> toUpperInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		List<Variable> op1 =  ir.getOperands().get(0);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(String value1:s1)
				s.add(value1.toUpperCase());
		}
		return s;
	}
	private Set<String> replaceCharInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		List<Variable> op3 =  ir.getOperands().get(2);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(Variable v3: op3)
				{
					Set<String> s3 = interpret(v3,itr);
					if(s3.isEmpty())
						continue;
					
					for(String value1:s1)
						for(String value2: s2)
							for(String value3: s3)
							{
								//System.out.println("PPP:"+value1+value2);
								
								//Cast char to String
								/*
								String temp2 = null;
								String temp3 = null;
								try
								{
									temp2 = ""+(char)Integer.parseInt(value2);
									temp3 = ""+(char)Integer.parseInt(value3);
								}
								catch(NumberFormatException e) { 
							        e.printStackTrace();
							    }
								if(temp2!=null&&temp3!=null)
									s.add(value1.replace(temp2,temp3));*/
								s.add(value1.replace(value2,value3));
							}
				}

						
			}
			
		}
		
		return s;
	}
	private Set<String> replaceCharSequenceInterpreter(Expression ir, int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		List<Variable> op3 =  ir.getOperands().get(2);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(Variable v3: op3)
				{
					Set<String> s3 = interpret(v3,itr);
					if(s3.isEmpty())
						continue;
					
					for(String value1:s1)
						for(String value2: s2)
							for(String value3: s3)
							{
								//System.out.println("PPP:"+value1+value2);
								s.add(value1.replace(value2,value3));
							}
				}

						
			}
			
		}
		
		return s;
	}
	private Set<String> replaceFirstInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		List<Variable> op3 =  ir.getOperands().get(2);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(Variable v3: op3)
				{
					Set<String> s3 = interpret(v3,itr);
					if(s3.isEmpty())
						continue;
					
					for(String value1:s1)
						for(String value2: s2)
							for(String value3: s3)
							{
								//System.out.println("PPP:"+value1+value2);
								s.add(value1.replaceFirst(value2,value3));
							}
				}

						
			}
			
		}
		
		return s;
	}

	private Set<String> replaceAllInterpreter(Expression ir,int itr) {
		Set<String> s = new HashSet<>();
		
		List<Variable> op1 =  ir.getOperands().get(0);
		List<Variable> op2 =  ir.getOperands().get(1);
		List<Variable> op3 =  ir.getOperands().get(2);
		for(Variable v1: op1)
		{
			Set<String> s1 = interpret(v1,itr);
			if(s1.isEmpty())
				continue;
			for(Variable v2: op2)
			{
				Set<String> s2 = interpret(v2,itr);
				if(s2.isEmpty())
					continue;
				for(Variable v3: op3)
				{
					Set<String> s3 = interpret(v3,itr);
					if(s3.isEmpty())
						continue;
					
					for(String value1:s1)
						for(String value2: s2)
							for(String value3: s3)
							{
								//System.out.println("PPP:"+value1+" "+value2+" "+value3);
								try{
									s.add(value1.replaceAll(value2,value3));
								}
								catch(PatternSyntaxException e)
								{
									
								}
								
								
							}
				}

						
			}
			
		}
		
		return s;
	}

	private void setInitOfInternal(String name,int regionNum,String line,Variable v,int itr,T t)
	{
		if(v instanceof T)
		{
		//	if(((T) v).isFi())
		//	{
				if(!(((T) v).getTVarName().equals(name)&&((T) v).getRegionNumber()==regionNum&&((T) v).getLine().equals(line)))
				{				
					if(itr-t.getK()+((T)v).getK()>0)
					{
						
					
					setInitOfInternal(name,regionNum,line,((T) v).getVariable(),itr,t);
					}
				}
		//	}
			
				
		}
		else if(v instanceof Expression)
		{
			for(List<Variable> operandList: ((Expression) v).getOperands())
			{
				for(Variable operand: operandList)
				{
					setInitOfInternal(name,regionNum,line,operand,itr,t);
				}
			}
		}
		else if(v instanceof InternalVar)
		{
			if(((InternalVar) v).getName().equals(name)&& ((InternalVar) v).getRegionNum()==regionNum&&((InternalVar) v).getLine().equals(line))
			{
				if((itr-(((InternalVar) v).getK())<0))
				{
				//	System.out.println("itr - the k of internal < 0"+" "+v);
					Variable e = v.getParent();
					if(e instanceof Expression)
					{
						List<Variable> target = new ArrayList<>();
						for(List<Variable> opList: ((Expression) e).getOperands())
						{
							if(opList.contains(v))
							{
								
								target.addAll(opList);
								target.remove(v);//System.out.println("got you"+target);
								Set<String> value = new HashSet<>();
								for(Variable init: target)
								{
									// !!!might be a trouble here, make extra interpret
									if(init instanceof Init)
										value.addAll(interpret(init,0));
								}
								((InternalVar) v).setInitValue(value);
							}
						}
					}
				}
				else
				{
					//if(((InternalVar) v).getName().equals("r2"))
					//	System.out.println(v+" "+tList.get(t).get(itr-(((InternalVar) v).getK())));
					if(itr-(((InternalVar) v).getK())<tList.get(t).size())
					((InternalVar) v).setInitValue(tList.get(t).get(itr-(((InternalVar) v).getK())));
				
				}
			}
		}
	}

}

