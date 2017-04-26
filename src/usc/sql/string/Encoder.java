package usc.sql.string;

import java.util.HashMap;

public class Encoder {
	private static int id=0;
	private static HashMap<String,Integer> idtable=new  HashMap<String,Integer>();
	public static int add(String s){
	id++;
	idtable.put(s, id);
	return id;
	}
	public static int query(String s){
	if(idtable.containsKey(s))
	{
	return idtable.get(s);
	}
	return -1;
	}
}
