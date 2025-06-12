package usc.sql.string.testcase;

public class TestCase1 {
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
}
