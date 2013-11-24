package util;

/**
 * Utility class to work with UUID's
 * 
 * @author david
 * 
 */
public class UUIDUtil {

	/**
	 * Method to reduce the length of a String to n characters
	 * 
	 * @param s
	 * @param n
	 * @return
	 */
	public static String cut(final String s, int n) {
		final byte[] utf8 = s.getBytes();
		if (utf8.length < n) {
			n = utf8.length;
		}
		int n16 = 0;
		boolean extraLong = false;
		int i = 0;
		while (i < n) {
			n16 += extraLong ? 2 : 1;
			extraLong = false;
			if ((utf8[i] & 0x80) == 0) {
				i += 1;
			}
			else if ((utf8[i] & 0xC0) == 0x80) {
				i += 2;
			}
			else if ((utf8[i] & 0xE0) == 0xC0) {
				i += 3;
			}
			else {
				i += 4;
				extraLong = true;
			}
		}
		return s.substring(0, n16);
	}

	/**
	 * Method to get a random UUID time based
	 * 
	 * @return
	 */
	public static java.util.UUID getTimeUUID() {
		return java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
	}
}