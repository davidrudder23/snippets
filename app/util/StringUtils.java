package util;

public class StringUtils {
	public static boolean isEmpty(String test) {
		if (test == null)
			return true;
		if (test.length() <= 0)
			return true;

		return false;
	}

}
