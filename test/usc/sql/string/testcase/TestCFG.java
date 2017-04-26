package usc.sql.string.testcase;

public class TestCFG {

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
			}
		}
	}
	/*
	 * public static void main(String[] args) { int a = 1; a = a + 2; int b = 3;
	 * if(a!=3) { b = b + 1; } //System.out.println(a+b); }
	 */
}
