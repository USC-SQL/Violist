package LoggerLib;

public class Logger {
	static int counter=0;
	public static void increase(int i)
	{
		counter+=i;
	}
	public static void report()
	{
		System.out.println(counter);
	}
	public static void reportString(String v, String log){
		System.out.println(log+"*"+v);

		
	}
}
