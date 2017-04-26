package usc.sql.string.testcase;

public class TestCase2 {
	public static void main(String[] args) {
		String a = "abbba";
		String b = "b";
		String c = "";
		String d = "";
		for (int i = 0; i < 100; i++) {
			b = b + "b";
		}
		for (int i = 0; i < 100; i++) {
			a = a.replaceAll(b, c);
			d = d + a;
			for (int j = 0; j < 100; j++) {
				c = a + c;
				for(int k=1;k<2;k++)
					c=a+c;
				for(int k1=0;k1<1;k1++)
				{
					if(a.equals("ff"))
						break;
					else{
						c=c+"f";
					}
				}
			}
		}
	}
}
