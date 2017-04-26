package usc.sql.string.testcase;

import LoggerLib.Logger;
import usc.sql.ir.Init;

class A
{
	String a;
	public void setA(String a)
	{
		this.a = a;
	}
}
public class SimpleTest {
	
	static String aa = "A";

	
    public static String addA(String v)
	{
		return v+"A";
	}

	public static String tmp(String a)
	{
		for(int i=0;i<3;i++)
			a = a + "a";
		a = addA(a);
		return a; //t(sigma_r0|ext_a+"a")|| ext_a
	}
	public static void main(String[] args)
	{		
	
		/*
		String a="a";
		String b="b";
		for(int i=0;i<2;i++)
		{
			a=a+b+"a";
			Logger.reportString(a,"Concat.NestedLoop_Branch");

			for(int j=0;j<2;j++)
	                {
				long time=(int)(Math.random()*100); 
				if(time % 2 ==0)
				{
					b=b+"b";
				}
				else{
			   		b=b+"t";
				}
			}
		}
		*/

		/*
		String a="a";
		String b="b";
		String c="c";
		String e="l";
		for(int i=0;i<3;i++)
		{
			a=a+b;
			b=b+c;
			long time=(int)(Math.random()*100);
			if(time % 2 ==0)
			{
				c=c+a+"c";
			}
			else{
			   	c=c+a+"d";
			}
			Logger.reportString(c,"Concat.CircleLoop_Branch");
		}
		*/
		
		
		String a="a";
		String b="b";
		StringBuilder c = new StringBuilder();
		c.append("ccc");

		b = b + c;
			Logger.reportString(b,"Concat.NestedLoop");


		
		

		
		/*
		String a="";
		String b="b";
		for(int i=0;i<3;i++)
		{
			//a=a+b+"a";
			b=b+"a";
			Logger.reportString(b,"NestedLoop_Branch");
			
			
			for(int j=0;j<2;j++)
	        {
				
					b=b+"b";
				
			}
			
		}*/
		
		
		/*
		String[] array = new String[] {"a","c","d"};
		
		array[1] = "foo";
		array[2] = "bar";
		String [] a = new String[3];
		a[1]= array[1];
			Logger.reportString(a[1],"ArrayAssignment");
		*/
		
	
		
			
		//	System.out.println("$r2[0] = r3".matches("(.r)([0-9]*)(.*) = (r)([0-9]*)(.*)"));
		/*
		String a="a";
		String b="b";
		String c="c";
		String e="e";
		for(int i=0;i<3;i++)
		{
			long time=System.currentTimeMillis();
			if(time % 2 ==0)
			{
				a=a+"a";
				
			}
			else{
		   		a=a+"t";
				
			}
			c=a+c;
			Logger.reportString(c,"BranchLoop");
		}
	    */

	    
		
		
		


		

		/*
		String a="";
		String b="";
		for(int i=0;i<3;i++)
		{
			a=a+b+"a";
			Logger.reportString(a,"bb");

			for(int j=0;j<1;j++)
	                {
				b=b+"b";
			}
		}
		*/
		
		/*
		String a="a";
		String b=tmp(a);
		*/
		
		
		/*
		String a = "ccc";
		String b = "b";
		String c = "c";
		
		for(int i=0;i<2;i++)
		{	
			a =a+"d";//a28+"d"||t(sigmae_a31+"d")
			for(int j=0;j<2;j++)
			{	
				//a = a.replaceFirst(c, b);//a28=t(sigma_a28.repalceFirst())||a28=t((sigmae_a28+"d"||t(sigmae_a31+"d")).replaceFirst())
				
				a = a+b;
			}
			
			
			
		}
		
		*/
		
		/*
		for(int x=0;x<2;x++)
		{	a = tmp(a);//r1= t_r1(t_r0(sigma_r0|(sigma_r1|"ccc")+"a"))
					   //case 1	
			          //r1=t_r0(sigma_r0_Rtmp|(r1|"ccc")+"a")
		              //t1=t_r1(t_r0(sigma_r0_Rtmp-1|((sigma_r1_Rmain1-1)|"ccc")+"a"))
		
			///a=a+"e";
		}
	
	*/
		

		/*
		for(int i=0;i<2;i++)
		{	
					
			a = a + b +c;	
			for(int j=0;j<2;j++)
			{	
			b = b + "b";// + a;
			for(int k=0;k<1;k++)
			{	
				c = c+"c";
			}
			}
		}
		*/
		//System.out.println(b);
		
		/*
		String a = "ab";
		String b = "ba";
		String d = "e";
		for(int j=0;j<5;j++)
		{  a = d +"a";
		for(int i=0;i<10;i++)
		{	
			a = b + "e";
			b = d + "f";
			d = a + "g";
		}
		}
		
		System.out.println(a );*/
		
		
		/*
		String a="abbba";
		String b="b";
		String c="";
		String d="";

		for(int i=0;i<100;i++)
		
		{
		
			//if(i==1)
			b=b+"b";
			//else
			//	b=b+"c";
			
			//d= b+"a";
		
		}
		for(int i=0;i<10;i++)	
		{	
			a=a.replaceAll(b,c);
			d=d+a;
			
			for(int j=0;j<10;j++)
			{
				//a= a+"a";
				c=a+c;
			}
		}
		
		System.out.println(a);
		*/
		
		/*
		String a="abbba";		
		String b="b";
		String c="";
		String d="";
		for(int i=0;i<10;i++)
		{
			b=a+b;
			a=a+b;
			a = a.replaceFirst(b, "c");
	    }
		System.out.println(a);
		*/
		
		/*
		String a="abbba";		
		String b="b";
		String c="";
		String d="";
		for(int i=0;i<3;i++)
		{
		b=a.replaceAll(b,"b");
		a=a.replaceAll(b, "c");
	    }
		System.out.println(a);
		*/
		
		

		
	}
	
	//topoSort indicates that line 10 or line 13 can be executed first. But in line 10 should be the first
	void wrong()
	{
		String a = "cccccccc";      
		String b = "b";
		String c = "c";
		for(int x=0;x<2;x++)
		{
			a=a+"e";             //line 10
		}
		a = tmp(a);     //line 13 
		for(int i=0;i<2;i++)
		{	
			for(int j=0;j<2;j++)
			{	
				a = a.replaceFirst(c, b);  //line 18
			
			}	
		
		}
	}
	void run0()
	{
		String a = "ab";
		String b = "ba";
		String d = "e";
				
		for(int i=0;i<10;i++)
		{	a = b + d;
			b = d + "f";
			d = a + "g";
		}
		
		System.out.println(a );
	}
	public static void run1(String k,String j) {

		String a = "ab";
		String b = "ba";
		String c = "cd";
		String d = "e";
		
		int x =0;
		if(x<0)
			a= b.replaceAll(c, d);
		else
			a = b.replaceAll(d,c);
		
		
		c = d.replaceAll(a, b);
		a = c.replaceAll(b, d);
		b = a.replace(a, d);
		int i =0 ;
		d = a.replace('c', 'd');
		d = d.toUpperCase().substring(i,i+i).replaceFirst("B",c );

		for(String t:(b+d).split("c", 1))
		System.out.println(t);
		
	}
	public void run2()
	{
		//run("d","a");
		String a = "ab";
		String b = "ba";
		String d = "e";
				
		for(int i=0;i<10;i++)
		{	a = b + "e";
			b = d + "f";
			d = a + "g";
		}
		
		System.out.println(a );
	}
	void run3()
	{
		String a = "ab";
		String b = "ba";
		String d = "e";
				
		for(int i=0;i<10;i++)
		{	a = b + "e";
			b = d + "f";
			d = a + "g";
		}
		
		System.out.println(a );
	}
	void run4()
	{
		String a="abbba";		
		String b="b";
		String c="";
		String d="";
		for(int i=0;i<100;i++)
		{
		b=b+"b";
		a=b+a;
	    }
		System.out.println(a);
	}

}
