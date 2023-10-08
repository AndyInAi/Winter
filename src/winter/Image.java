package winter;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;

import org.apache.kafka.common.utils.Java;

public class Image {

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	public static void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + (o == null ? null : o.toString()));

	}

	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		log("Start ...");

		Image image = new Image();

		String base64 = image.imageToBase64("/tmp/andy.jpg");

		File file = image.base64ToImage(base64);

		log(file.getAbsolutePath());

	}

	/**
	 * Base64 转图片
	 * 
	 * @param base64
	 * @return
	 */
	public byte[] base64ToBytes(String base64) {

		if (base64 == null || (base64 = base64.trim()).length() == 0) {

			return null;

		}

		try {

			return Base64.getDecoder().decode(base64);

		} catch (Exception ex) {

			return null;

		}

	}

	/**
	 * Base64 转图片
	 * 
	 * @param base64
	 * @return
	 */
	public File base64ToImage(String base64) {

		File file = null;

		try {

			file = File.createTempFile(UUID.randomUUID().toString(), ".png");

		} catch (IOException ex) {

			ex.printStackTrace();

			return null;

		}

		return base64ToImage(base64, file);

	}

	/**
	 * Base64 转图片
	 * 
	 * @param base64
	 * @param file
	 * @return
	 */
	public File base64ToImage(String base64, File file) {

		if (file == null || (file.exists() && file.isDirectory())) {

			return null;

		}

		byte[] bytes = base64ToBytes(base64);

		if (bytes == null) {

			return null;

		}

		OutputStream os = null;

		try {

			os = new FileOutputStream(file);

			os.write(bytes);

			return file;

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			close(os);

		}

		return null;

	}

	/**
	 * Base64 转图片
	 * 
	 * @param base64
	 * @param file
	 * @return
	 */
	public File base64ToImage(String base64, String file) {

		if (file == null || (file = file.trim()).length() == 0) {

			return null;

		}

		return base64ToImage(base64, new File(file));

	}

	/**
	 * 关闭
	 * 
	 * @param o
	 */
	private void close(Closeable o) {

		if (o == null) {

			return;

		}

		try {

			o.close();

		} catch (IOException ex) {

			ex.printStackTrace();

		}

	}

	/**
	 * 图片文件转 Base64，文件最大限制为 64 M
	 * 
	 * @param file
	 * @return
	 */
	public String imageToBase64(File file) {

		if (file == null || !file.exists() || !file.isFile() || file.length() > (64 * 1024 * 1024)) {

			return null;

		}

		BufferedInputStream bis = null;

		try {

			bis = new BufferedInputStream(new FileInputStream(file));

			byte[] bytes = bis.readAllBytes();

			if (bytes.length == file.length()) {

				return Base64.getEncoder().encodeToString(bytes);

			}

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			close(bis);

		}

		return null;

	}

	/**
	 * 图片文件转 Base64
	 * 
	 * @param file
	 * @return
	 */
	public String imageToBase64(String file) {

		return (file == null || (file = file.trim()).length() == 0) ? null : imageToBase64(new File(file));

	}

}
