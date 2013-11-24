package util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class to work with files
 * 
 * @author david
 * @version 1.0
 */
public final class FileUtil {

	/**
	 * Method to read a file from the FS and get its representation as a String object
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(final String path, final Charset encoding) throws IOException {
		final byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	private FileUtil() {
	}
}
