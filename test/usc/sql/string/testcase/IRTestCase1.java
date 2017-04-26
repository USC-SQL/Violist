package usc.sql.string.testcase;

public class IRTestCase1 {
	public static void main(String[] args)
	{		
		
		String a = "a";
		String b = "b";

				
		for(int i=0;i<10;i++)
		{	a = b + "e";
			b = a + "f";		
		}
		
		System.out.println(b );
	}
}
