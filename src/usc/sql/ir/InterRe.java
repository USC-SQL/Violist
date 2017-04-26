package usc.sql.ir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import SootEvironment.JavaApp;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import usc.sql.string.Encoder;
import usc.sql.string.ReachingDefinition;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class InterRe {

	private Map<String,Variable> lineUseMap = new HashMap<>();
	private Map<String,Boolean> regionDef;
	private List<String> lineDef;
	private Map<NodeInterface,Variable> returnVarNodeAndName = new HashMap<>();
	private Map<NodeInterface,List<String>> targetVarNodeAndName;
	private Map<String,ArrayList<Variable>> lineUseMapFromOtherMethod = new HashMap<>();
	private  Map<String, Set<NodeInterface>> paraMap;
	private Map<String,Set<String>> field;
	private List<String> stringvirtual = new ArrayList<>();
	private JavaApp App;
	private String folderName;
	
	//target signature
	private Map<String,List<Integer>> targetSignature;
	//the Nth string parameter

	
	private int targetCount;
	private String methodName;
	public InterRe(List<NodeInterface> nodes,Map<String,Boolean> regionDef,List<String> lineDef,String folderName,
			Map<NodeInterface,List<String>> targetVarNodeAndName, Map<String, Set<NodeInterface>> paraMap,Map<String,Set<String>> field,
			int targetCount,String methodName,JavaApp App, Map<String,List<Integer>> targetSignature)
	{
		this.regionDef = regionDef;
		this.lineDef = lineDef;
		this.folderName = folderName;
		this.targetVarNodeAndName = targetVarNodeAndName;
		this.paraMap = paraMap;
		this.field = field;
		this.targetCount = targetCount;
		this.methodName = methodName;
		this.targetSignature = targetSignature;

		this.App = App;
		stringvirtual.add("<java.lang.String: java.lang.String replace(char,char)>");
		stringvirtual.add("<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)>");
		stringvirtual.add("<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>");
		stringvirtual.add("<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>");
		stringvirtual.add("<java.lang.String: java.lang.String toUpperCase()>");
		stringvirtual.add("<java.lang.String: java.lang.String toLowerCase()>");
		stringvirtual.add("<java.lang.String: java.lang.String substring(int,int)>");
		stringvirtual.add("<java.lang.String: java.lang.String substring(int)>");
		stringvirtual.add("<java.lang.String: java.lang.CharSequence subSequence(int,int)>");
		stringvirtual.add("<java.lang.StringBuilder: java.lang.StringBuilder append");
		stringvirtual.add("<java.lang.StringBuffer: java.lang.StringBuffer append");				
		stringvirtual.add("<java.lang.String: java.lang.String trim()>");
		stringvirtual.add("<java.lang.String: java.lang.String intern()>");
		stringvirtual.add("<java.lang.String: java.lang.String[] split(java.lang.String)>");
		stringvirtual.add("<java.lang.String: java.lang.String[] split(java.lang.String,int)>");
		stringvirtual.add("<java.lang.String: boolean contains(java.lang.CharSequence)>");
		
		stringvirtual.add("<java.lang.String: char charAt(int)>");
		stringvirtual.add("<java.lang.String: char[] toCharArray()>");
		stringvirtual.add("<java.lang.StringBuilder: java.lang.StringBuilder replace(int,int,java.lang.String)>");
		stringvirtual.add("<java.lang.StringBuilder: java.lang.StringBuilder delete(int,int)>");
		stringvirtual.add("<java.lang.StringBuilder: java.lang.StringBuilder deleteCharAt(int)>");
		stringvirtual.add("<java.lang.StringBuffer: java.lang.StringBuffer delete(int,int)>");
		stringvirtual.add("<java.lang.StringBuilder: java.lang.StringBuilder insert(int,java.lang.String)>");
		stringvirtual.add("<java.lang.StringBuffer: java.lang.StringBuffer insert(int,java.lang.String)>");
		stringvirtual.add("<java.lang.StringBuffer: java.lang.StringBuffer insert(int,char)>");
		stringvirtual.add("<java.lang.StringBuffer: java.lang.StringBuffer replace(int,int,java.lang.String)>");
		//non java.lang method but return string		
		stringvirtual.add("<java.net.URLEncoder: java.lang.String encode(java.lang.String,java.lang.String)>");
		
		
		for(NodeInterface n: nodes)
		{
			interpret(n);
		}		
		
	}
	

	public Map<String,Variable> getUseMap()
	{
		return lineUseMap;
	}
	public Map<String,ArrayList<Variable>> getUseMapFromOtherMethod()
	{
		return lineUseMapFromOtherMethod;
	}
	public Map<NodeInterface,Variable> getReturnVar()
	{
		return returnVarNodeAndName;
	}
	public Map<NodeInterface,List<String>> getTargetVar()
	{
		return targetVarNodeAndName;
	}
	private void interpret(NodeInterface n)
	{
		Stmt actualNode = (Stmt) ((Node)n).getActualNode();	
	
		
		if(actualNode!=null)
		{

			if(targetSignature!=null&&actualNode.containsInvokeExpr())
			{
				String signature = actualNode.getInvokeExpr().getMethod().getSignature();
				if(targetSignature.keySet().contains(signature))
				{	
				
						int paraNum=0;
						
						for(ValueBox vb:actualNode.getUseBoxes())
						{
							if(vb.getValue().getType().toString().equals("java.lang.String")&&!vb.getValue().toString().contains("<"))
							{
								
								paraNum++;
								if(targetSignature.get(signature).contains(paraNum))
								{
									List<String> nameAndLabel = new ArrayList<>();
									//name
									nameAndLabel.add(vb.getValue().toString());
									int bytecodeOffset = -1;
									for (Tag t : actualNode.getTags()) {
										if (t instanceof BytecodeOffsetTag)
											bytecodeOffset = ((BytecodeOffsetTag) t).getBytecodeOffset();
									}
		
									
									//label: the target signature is found in method name at line source line + bytecode offset
									nameAndLabel.add(methodName + "@" + actualNode.getJavaSourceStartLineNumber() + "@"
												+ bytecodeOffset + "@" + paraNum+"@"+actualNode.toString());
										// System.out.println(targetCount+":"+vb.getValue().toString());
									targetVarNodeAndName.put(n, nameAndLabel);
									
								}	
								
								
							}
						}
					
				} 
				
			}
		}
		
		
		//if(actualNode!=null&&actualNode.getDefBoxes()!=null)
		//System.out.println(n.getOffset().toString()+" "+actualNode.toString()+" "+actualNode.getUseBoxes());
		

		
		
		if(actualNode!=null&&lineDef.contains(n.getOffset().toString()))
		{
			
			
			
			
			List<ValueBox> sootDef = actualNode.getDefBoxes();
			List<ValueBox> sootUse = actualNode.getUseBoxes();
			
			
			
			//case : field
			if(!sootDef.isEmpty())
			if(sootDef.get(0).getValue() instanceof FieldRef)
			{
				FieldRef v=(FieldRef) sootDef.get(0).getValue();
				String value;
				if(sootUse.size()==1)								
					value = sootUse.get(0).getValue().toString();				
				else
					value = sootUse.get(1).getValue().toString();
				
				if(value.contains("\""))
				{
					//value = value.replaceFirst("\"", "");
					//value = value.substring(0,value.lastIndexOf("\""));
					value = value.replaceAll("\"", "");
					String signature = v.getField().getSignature();
					if(field.keySet().contains(signature))
						field.get(signature).add(value);
					else
					{
						Set<String> s = new HashSet<>();
						s.add(value);
						field.put(signature, s);
					}
				}
				
			}
			
			
			
			//case : @parameter
			if(actualNode.toString().contains("@parameter"))
				lineUseMap.put(n.getOffset().toString(),new ExternalPara(sootUse.get(0).getValue().toString()+":"+methodName));
			//case : new StringBuilder
			//!!
			else if(actualNode.toString().contains("= new java.lang.StringBuilder")||actualNode.toString().contains("= new java.lang.StringBuffer"))
				lineUseMap.put(n.getOffset().toString(), new ConstantString(""));
			//case : valueOf or toString
			else if(actualNode.toString().contains("staticinvoke <java.lang.String: java.lang.String valueOf(java.lang.Object)>")
					||actualNode.toString().contains("<java.lang.StringBuilder: java.lang.String toString()>")
					||actualNode.toString().contains("<java.lang.StringBuffer: java.lang.String toString()>")
					||actualNode.toString().contains("<java.lang.String: java.lang.String toString()>")
					||actualNode.toString().contains("<java.lang.CharSequence: java.lang.String toString()>"))
			{
				String use = sootUse.get(1).getValue().toString();
			
				if(use.contains("\""))
					lineUseMap.put(n.getOffset().toString(), new ConstantString(use));
				else
				{
					if(regionDef.get(use)!=null)
					{
						if(regionDef.get(use))
							lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
						else
							lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
					}
				}
			}
			
			//case : init
			else if(actualNode.toString().contains("<java.lang.StringBuilder: void <init>()>")||actualNode.toString().contains("<java.lang.StringBuffer: void <init>()>")||actualNode.toString().contains("<java.lang.String: void <init>()>"))
			{
				lineUseMap.put(n.getOffset().toString(), new ConstantString(""));
			}
			else if(actualNode.toString().contains("<java.lang.StringBuilder: void <init>(java.lang.String)>")||actualNode.toString().contains("<java.lang.StringBuilder: void <init>(java.lang.CharSequence)>")
					||actualNode.toString().contains("<java.lang.StringBuffer: void <init>(java.lang.String)>")
					||actualNode.toString().contains("<java.lang.String: void <init>(java.lang.StringBuffer)>")||actualNode.toString().contains("<java.lang.String: void <init>(java.lang.String)>")||actualNode.toString().contains("<java.lang.String: void <init>(java.lang.StringBuilder)>"))	
			{
				String use = sootUse.get(0).getValue().toString();
				if(use.contains("\""))
					lineUseMap.put(n.getOffset().toString(), new ConstantString(use));
				else
				{
					if(regionDef.get(use))
						lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
					else
						lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
				}
			}
			//case : constant String  || set field
			else if(actualNode.toString().contains("\"")&&sootUse.size()==1)
			{
			
					lineUseMap.put(n.getOffset().toString(),new ConstantString(sootUse.get(0).getValue().toString()));
			}
			
			
			//array clone
			else if(actualNode.toString().contains("<java.lang.Object: java.lang.Object clone()>"))
			{
				for(ValueBox vb : sootUse)
					if(vb instanceof JimpleLocalBox)
					{
						String use = vb.getValue().toString();
						if(regionDef.get(use))
							lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
						else
							lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
						
					}
			}
			
			//string manipulation
			else if(contain(actualNode.toString()))
			{				
				//System.out.println(actualNode.getDefBoxes()+" "+actualNode.getUseBoxes());
				List<Variable> immed = new ArrayList<>();
				
				List<Variable> jimp = new ArrayList<>();
				List<Variable> operands = new ArrayList<>();
				Operation op = null;
				boolean ignore = false;
				for(ValueBox vb:sootUse)
				{
					
					String use = vb.getValue().toString();
					if(!use.contains("virtualinvoke")&&!use.contains("staticinvoke"))
					{
						
						Variable v;
						if(use.equals("null"))
							v = new ConstantString("null");
						else if(use.contains("\""))
							v = new ConstantString(use);
						else if(use.contains("r"))
						{
							
							if(regionDef.get(use)==null)
								v= new ExternalPara("Unknown@NODEF");
							else
							{
							if(regionDef.get(use))
								v = new InternalVar(use);
							else
								v = new ExternalPara(use);
							}
						}
						else if((use.contains("b")||use.contains("i"))||use.contains("c")||use.contains("z")||use.contains("d")||use.contains("l")&&!use.contains("\""))
						{
							
							if(regionDef.get(use))
								v = new InternalVar(use);
							else
								v = new ExternalPara(use);
						}
						
						else
						{
							if(actualNode.toString().contains("<java.lang.StringBuffer: java.lang.StringBuffer append(boolean)"))
							{
								if(use.equals("0"))
									v = new ConstantString("false");
								else
									v= new ConstantString("true");
							}
							else
								v = new ConstantInt(use);
						}
						
						if(vb instanceof ImmediateBox)
							immed.add(v);
						else if(vb instanceof JimpleLocalBox)
							jimp.add(v);
							
						//operands.add(v);
					
					}
					else
					{
					
						
						
						//System.out.println(use);
						if(use.contains("<java.lang.String: java.lang.String replace(char,char)>"))
							op = new Operation("replace(char,char)");
						else if(use.contains("<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)>"))
							op = new Operation("replace(java.lang.CharSequence,java.lang.CharSequence)");
						else if(use.contains("<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>"))
							op = new Operation("replaceAll");
						else if(use.contains("<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>"))
							op = new Operation("replaceFirst");
						else if(use.contains("<java.lang.String: java.lang.String toUpperCase()>"))
							op = new Operation("toUpperCase");
						else if(use.contains("<java.lang.String: java.lang.String toLowerCase()>"))
							op = new Operation("toLowerCase");
						else if(use.contains("<java.lang.String: java.lang.String substring(int,int)>"))
							op = new Operation("substring(int,int)");
						else if(use.contains("<java.lang.String: java.lang.String substring(int)>"))
							op = new Operation("substring(int)");
						else if(use.contains("<java.lang.String: java.lang.CharSequence subSequence(int,int)>"))
							op = new Operation("subSequence(int,int)");
						else if(use.contains("<java.lang.StringBuilder: java.lang.StringBuilder append")
								||use.contains("<java.lang.StringBuffer: java.lang.StringBuffer append")
								||use.contains("<java.lang.String: java.lang.String concat(java.lang.String)>"))							
						{
							
							op = new Operation("append");
						}
						else if(use.contains("<java.lang.String: java.lang.String trim()>"))
							op = new Operation("trim");
						else if(use.contains("<java.lang.String: java.lang.String intern()>"))
							op = new Operation("intern()");
						else if(use.contains("<java.lang.String: java.lang.String[] split(java.lang.String)>"))
							op = new Operation("split(java.lang.String)");
						else if(use.contains("<java.lang.String: java.lang.String[] split(java.lang.String,int)>"))
							op = new Operation("split(java.lang.String,int)");	
						else if(use.contains("<java.lang.String: boolean contains(java.lang.CharSequence)>"))
							op = new Operation("contains");
						else if(use.contains("<java.lang.String: char charAt(int)>"))
							op = new Operation("charAt");
						else if(use.contains("<java.lang.String: char[] toCharArray()>"))
							op = new Operation("toCharArray");
						
						//non java.lang method
						else if(use.contains("<java.net.URLEncoder: java.lang.String encode(java.lang.String,java.lang.String)>"))
						{
							op = new Operation("encode");
						}
						
						else 
						{
						
							ignore = true;
							lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown@MISSINVOKE"));
							//System.out.println("virtual invoke case missing: "+ use);
						}
						
					}
				}
				if(!ignore)
				{
					operands.addAll(jimp);
					operands.addAll(immed);
					List<List<Variable>> operandList = new ArrayList<>();
					for(Variable opd: operands)
					{
						List<Variable> temp = new ArrayList<>();
						temp.add(opd);
						operandList.add(temp);
					}
					lineUseMap.put(n.getOffset().toString(), new Expression(operandList, op));
	
				}
				
				
				
				
			}
			else if(sootUse.toString().contains("newarray"))
			{
				lineUseMap.put(n.getOffset().toString(), new ConstantString("null"));
				//Haven't handled ,if a array element is never assign, it will fail
				
				//lineUseMap.put(n.getOffset().toString(), new ConstantString("newarray"));
			}
			
			else if(actualNode.toString().matches("(.r|r)([0-9]*)(.*) = (.*)")&&!actualNode.toString().contains("staticinvoke")&&!actualNode.toString().contains("specialinvoke")&&!actualNode.toString().contains("virtualinvoke")&&!actualNode.toString().contains("interfaceinvoke"))
			{
				//System.out.println("yaya"+sootUse);
				
				//case: r = field
				if(sootUse.get(0).getValue() instanceof FieldRef)
				{
					Value v = sootUse.get(0).getValue();
					String signature = ((FieldRef)v).getField().getSignature();
					//System.out.println("GOT"+signature);
					
					lineUseMap.put(n.getOffset().toString(), new ExternalPara(signature));
					

				}
				if(sootUse.size()==1)
				{
					String use = sootUse.get(0).getValue().toString();
					
					//case: r = r
					if(use.matches("(.r|r)([0-9]*)"))
					{
					//System.out.println("Oh no: "+actualNode.toString());
						if(regionDef.get(use)==null)
							lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown@NODEF"));
						else
						{
							if(regionDef.get(use))
								lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
							else
								lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
						}
					}
					//case: r = null
					else
					{
						
						Value v=sootUse.get(0).getValue();
						if(v instanceof FieldRef)
						{
							lineUseMap.put(n.getOffset().toString(), new ExternalPara(((FieldRef)v).getField().getSignature()));
						//	System.out.println("Field:"+ ((FieldRef)v).getField().getTags());
						}
						else if(v.toString().equals("null"))
							lineUseMap.put(n.getOffset().toString(), new ConstantString("null"));
						
					}
				}
				
				else
				{
					//case: r = r.something
					if(actualNode.toString().matches("(.r|r)([0-9]*)(.*) = (.r|r)([0-9]*)(.*)"))
					{
						
						for(ValueBox vb:sootUse)
						{
							if(!(vb instanceof JimpleLocalBox || vb instanceof ImmediateBox))
							{
								String use = vb.getValue().toString();
								if(regionDef.get(use)==null)
								{
									if(use.contains("[")/*&&vb.getValue().getType().toString().equals("java.lang.String[]")*/)
									{
										
										String tmp = use.substring(0,use.indexOf("["));
										
										if(regionDef.get(tmp)==null)
											lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown@NODEF"));
										else
										{
										if(regionDef.get(tmp))
											lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
										else
											lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
										}
									}
								}
								else
								{
							
									if(regionDef.get(use))
										lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
									else
										lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
								}
								
							}
						}
					}
					else if(actualNode.toString().matches("(.r|r)([0-9]*)(.*) = \"(.*)\""))
					{
						for(ValueBox vb:sootUse)
						{
							if(!(vb instanceof JimpleLocalBox || vb instanceof ImmediateBox))
							{
								String use = vb.getValue().toString();
								if(use.contains("\""))
									lineUseMap.put(n.getOffset().toString(), new ConstantString(use));

							
							
							}
						}
					}
					else if(actualNode.toString().matches("(.r|r)([0-9]*)(.*) = (.)*(.r|r)([0-9]*)")&&actualNode.toString().contains("(java.lang.String[])"))
					{
						for(ValueBox vb:sootUse)
						{
							if(vb instanceof ImmediateBox)
							{
								String use = vb.getValue().toString();
								if(regionDef.get(use))
									lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
								else
									lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
							
							
							}
						}
					}


				
				}
			}
					

			else if(actualNode.toString().matches("(.c|c)(.*) = (.*)"))
			{
				
				String use = sootUse.get(0).getValue().toString();
				//Casting 
				if(use.contains("(char) b")||use.contains("(char) i"))
				{
					List<List<Variable>> operandList = new ArrayList<>();
					List<Variable> op1 = new ArrayList<>();
					for(ValueBox vb:sootUse)
					{
						if(vb instanceof ImmediateBox)
						{
							String use1 = vb.getValue().toString();
							if(regionDef.get(use1))
								op1.add( new InternalVar(use1));
							else
								op1.add( new ExternalPara(use1));
							
							break;
						}
					}
					operandList.add(op1);
					lineUseMap.put(n.getOffset().toString(), new Expression(operandList, new Operation("CastChar")));
				}
				else
				{
					
					if(use.matches("c[0-9]+"))
					{
						if(regionDef.get(use))
							lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
						else
							lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
					}
					//Only for JSA Test
					else if(use.matches("(.r|r)([0-9]*)(.*)"))
					{
						
						for(ValueBox vb:sootUse)
						{
							if(!(vb instanceof JimpleLocalBox || vb instanceof ImmediateBox))
							{
								String use1 = vb.getValue().toString();
								if(regionDef.get(use1)==null)
								{
									if(use1.contains("[")/*&&vb.getValue().getType().toString().equals("java.lang.String[]")*/)
									{
										
										String tmp = use1.substring(0,use1.indexOf("["));
										
										if(regionDef.get(tmp)==null)
											lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown@NODEF"));
										else
										{
										if(regionDef.get(tmp))
											lineUseMap.put(n.getOffset().toString(), new InternalVar(use1));
										else
											lineUseMap.put(n.getOffset().toString(), new ExternalPara(use1));
										}
									}
								}
								else
								{
							
									if(regionDef.get(use))
										lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
									else
										lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
								}
						
							}
						}
					}
					
					else
					{
						if(!use.contains("virtualinvoke"))
						{
							try
							{
						char dig = (char)(Integer.parseInt(use));
						lineUseMap.put(n.getOffset().toString(),new ConstantString(""+dig));
							}
							catch(NumberFormatException e)
							{
								
							}
						}
			
					}
				}
			}
			else if(actualNode.toString().matches("b(.*) = (.*)")&&sootUse.size()==1)
			{
				String use = sootUse.get(0).getValue().toString();
				if(use.matches("b[0-9]+"))
				{
					if(regionDef.get(use))
						lineUseMap.put(n.getOffset().toString(), new InternalVar(use));
					else
						lineUseMap.put(n.getOffset().toString(), new ExternalPara(use));
				}
				else
				{		
					lineUseMap.put(n.getOffset().toString(),new ConstantInt(use));
				}
			}
			

			// r = *invoke
			else if(actualNode.toString().contains("staticinvoke")||actualNode.toString().contains("specialinvoke")||actualNode.toString().contains("virtualinvoke")||actualNode.toString().contains("interfaceinvoke"))
			{
				

				String methodName = ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();
				Set<String> fileNameList = new HashSet<>();
				
				
				
				//JSA test only
				if(actualNode.toString().contains("interfaceinvoke"))
				{
					Value interV = null;
					for(ValueBox va : actualNode.getUseBoxes())
					{
						if(va.getValue().toString().contains("interfaceinvoke"))
							interV = va.getValue();
					}
					if(App != null)
					{
						for(String sss: App.getImplementMethodsSig(interV))
							fileNameList.add(Encoder.query(sss)+".ser");
						
					}

				}

				else
					fileNameList.add(Encoder.query(methodName)+".ser");
				
				File folder = new File(folderName);
				ArrayList<Variable> fromOtherMethod = new ArrayList<>();
				
				for(String file:fileNameList)
				{
				File[] listOfFiles = folder.listFiles();
				for(int i = 0; i < listOfFiles.length; i++){			   
				    if(listOfFiles[i].getName().equals(file)){
				
						try {
				        FileInputStream fis = new FileInputStream(folderName+file);
				        
				        //Create new ObjectInputStream object to read object from file
				        ObjectInputStream obj = new ObjectInputStream(fis);
				        
				            while (fis.available() != -1) {
				                //Read object from file
				            	
								try {
								fromOtherMethod.add((Variable) obj.readObject()); 
								
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
				               
				                
				            }
				            obj.close();
				        } catch (IOException ex) {
				            //ex.printStackTrace();
				        }
				    }
				}
				}
				
				ArrayList<Variable> temp = new ArrayList<>();
				//replace the externalPara with internal or the extenalPara in the current method
				for(Variable v: fromOtherMethod)
				{
		
					temp.add(replaceExternal(v,actualNode.getUseBoxes()));
				}
				
				if(!temp.isEmpty())
					lineUseMapFromOtherMethod.put(n.getOffset().toString(), temp);
				else
				{
					//if the method return an String but we don't have the summary for that method, return method name.
					if(actualNode.getUseBoxes().get(0).getValue().getType().toString().equals("java.lang.String"))
					{
						//lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown"));
						lineUseMap.put(n.getOffset().toString(), new ExternalPara("!!!"+methodName+"@"+this.methodName+"@"+n.getOffset().toString()+"@"+actualNode.getDefBoxes().get(0).getValue().toString()+"!!!"));
					}
				
					
				}
				//if it calls some api, save the node for later use
				String signature = methodName;
				if(!paraMap.containsKey(signature))
				{
					Set<NodeInterface> tempSet = new HashSet<>();
					tempSet.add(n);
					paraMap.put(signature, tempSet);
				}
				else
				{
					paraMap.get(signature).add(n);
				}
				
				
				
				
	
			}
			//case others
			//b0 = 1,$i1 = b0 + 1
			else
			{
				lineUseMap.put(n.getOffset().toString(), new ExternalPara("Unknown"));
			//	System.out.println("case missing: "+ actualNode.toString());
			//	System.out.println(actualNode.getDefBoxes()+"     "+actualNode.getUseBoxes());
			}
			

		}
		

		else if(actualNode!=null&&actualNode.toString().contains("return")&&!actualNode.toString().contains("goto")&&!actualNode.getUseBoxes().isEmpty())
		{
	
			if(actualNode.getUseBoxes().get(0).getValue().getType().toString().equals("java.lang.String"))		
			{
				String returnNodeName = actualNode.getUseBoxes().get(0).getValue().toString();
				if(returnNodeName.contains("\""))
					returnVarNodeAndName.put(n,new ConstantString(returnNodeName));
				else
				{
					if(regionDef.get(returnNodeName)==null)
						returnVarNodeAndName.put(n, new ExternalPara("Unknown@NODEF"));
					else
					{
					if(regionDef.get(returnNodeName))
						returnVarNodeAndName.put(n, new InternalVar(returnNodeName));
					
					else
					{	//IT SHOULD BE DEAD CODE
						returnVarNodeAndName.put(n, new ExternalPara(returnNodeName));
					}
					}
				}
					
					
				
			}
		}
		
		else if(actualNode!=null&&actualNode.toString().contains("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>")&&!actualNode.toString().contains("goto"))
		{
			if(!targetVarNodeAndName.keySet().contains(n))
			{
				List<String> nameAndLabel = new ArrayList<>();
				if(actualNode.getUseBoxes().size()!=3)
					System.out.println("Logger Para Error "+actualNode);
				else
				{
					nameAndLabel.add(actualNode.getUseBoxes().get(0).getValue().toString());
					nameAndLabel.add(actualNode.getUseBoxes().get(1).getValue().toString());
				}
				targetVarNodeAndName.put(n, nameAndLabel);
			
			}
			
		}
		//*invoke
		else if(actualNode!=null&&(actualNode.toString().contains("staticinvoke")||actualNode.toString().contains("virtualinvoke")||actualNode.toString().contains("specialinvoke")||actualNode.toString().contains("specialinvoke"))&&!actualNode.toString().contains("<init>")&&!actualNode.toString().contains("goto"))
		{

			String signature = ((Stmt)actualNode).getInvokeExpr().getMethod().getSignature();

			if(!paraMap.containsKey(signature))
			{
				Set<NodeInterface> tempSet = new HashSet<>();
				tempSet.add(n);
				paraMap.put(signature, tempSet);
			}
			else
			{	
				paraMap.get(signature).add(n);
			}
			
		}
	}
	
	private Variable replaceExternal(Variable v,List<ValueBox> valueBox)
	{
		if(v instanceof ExternalPara)
		{
			if(((ExternalPara) v).getName().contains("@parameter"))
			{
				String tmp = ((ExternalPara) v).getName().split(":")[0].replaceAll("@parameter", "");
				int index = Integer.parseInt(tmp)+1;
			
				
				String para = valueBox.get(index).getValue().toString();
				if(regionDef.get(para)!=null &&regionDef.get(para))
				{
					return new InternalVar(para);
				}
				else
				{
					if(para.contains("parameter"))
						return new ExternalPara(para+":"+methodName);
					else
						return new ConstantString(para);
				}
			}
			else
				return v;
			
		}
		else if(v instanceof Init)
		{
			List<Variable> temp = new ArrayList<>();
			for(Variable tmp:((Init) v).getInitVar())
			{
			
				temp.add(replaceExternal(tmp,valueBox));
			}
			((Init) v).setInitVar(temp);
			return v;
		}
		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				List<Variable> tempOperand = new ArrayList<>();
				for(Variable operand:operandList)
				{
					tempOperand.add( replaceExternal(operand, valueBox));
				}
				newOperandList.add(tempOperand);
			}
 			((Expression) v).setOperands(newOperandList);
			return v;
		}
		else if(v instanceof T)
		{
			((T) v).setVariable(replaceExternal(((T) v).getVariable(), valueBox));
			return v;
		}
		else
			return v;
	}
	
	private boolean contain(String target)
	{
		for(String s: stringvirtual)
			if(target.contains(s))
				return true;
		return false;
	}
	
}
