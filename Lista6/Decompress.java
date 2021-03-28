import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Decompress {

	private int imageHeight;
	private int imageWidth;
	private int[] input;
	private int[] result;
	private byte[] bytes;

	public Decompress(byte[] bytes) {
		this.bytes = bytes;
		imageHeight = ((bytes[15] & 0xFF) << 8) | (bytes[14] & 0xFF);
		imageWidth = ((bytes[13] & 0xFF) << 8) | (bytes[12] & 0xFF);
		result = new int[imageHeight * imageWidth];
		input = new int[imageWidth * imageHeight];
		int i = 0;
		int byteIndex = 18;
		while (byteIndex < bytes.length - 26) {
			int color = ((bytes[byteIndex + 2] & 0xFF) << 16) | ((bytes[byteIndex + 1] & 0xFF) << 8)
					| (bytes[byteIndex] & 0xFF); // red, green, blue
			byteIndex += 3;
			input[i] = color;
			i++;
		}
	}

	private void run() {
		int red = 0;
		int green = 0;
		int blue = 0;
		for(int i =0; i < input.length; i++) {
			// d_n^
			byte r = (byte) ((input[i] >> 16) & 0xFF);
			byte g = (byte) ((input[i] >> 8) & 0xFF);
			byte b = (byte) ((input[i] >> 0) & 0xFF);
			// x_n^ = d_n^ + x_n-1^
			red += r; 
			green += g;
			blue += b;
			
			if (red > 255)
				red = 255;
			else if (red < 0)
				red = 0;

			if (green > 255)
				green = 255;
			else if (green < 0)
				green = 0;

			if (blue > 255)
				blue = 255;
			else if (blue < 0)
				blue = 0;
			
			result[i] = ((red << 16) | (green << 8) | (blue << 0));
		}
	}

	public static void main(String[] args) { // start
		try {
			File file = new File(args[0]);
			File outputFile = new File(args[1]);
			FileInputStream fin = new FileInputStream(file);
			FileOutputStream fout = new FileOutputStream(outputFile);
			byte[] bytes = fin.readAllBytes();
			Decompress decompress = new Decompress(bytes);
			decompress.run();
			fout.write(decompress.getImage());
			System.out.println("Done");
			fin.close();
			fout.close();

		} catch (FileNotFoundException e) {
			System.err.println("Nie ma takiego pliku");
		} catch (IOException e) {
			System.err.println("Blad odczytu.");
		} catch (NumberFormatException e) {
			System.err.println("Bledny parametr");
		}
	}

	private byte[] getImage() {
		int i = 0;
		int byteIndex = 18;
		while (byteIndex < bytes.length - 26) {
			bytes[byteIndex] = (byte) (result[i] & 0xFF);
			bytes[byteIndex + 1] = (byte) ((result[i] >> 8) & 0xFF);
			bytes[byteIndex + 2] = (byte) ((result[i] >> 16) & 0xFF);
			byteIndex += 3;
			i++;
		}
		return bytes;
	}

}
