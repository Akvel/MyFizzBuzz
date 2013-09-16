public class MyFuzz2 {

	public static void main(String[] args) {
		for (int i = 1; i < 101; i++)
			System.out.println("FizzBuzz".substring(i % 3 == 0 ? 0 : 4, i % 5 == 0 ? 8 : 4).replaceAll("^$", "" + i));
	}

}
