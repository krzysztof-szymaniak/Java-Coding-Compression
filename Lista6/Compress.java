import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Compress {

	private int imageHeight;
	private int imageWidth;
	private int[] input;
	private int[] result;
	private byte[] bytes;
	int k;

	public Compress(byte[] bytes, int k) {
		this.bytes = bytes;
		this.k = k;
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

	public void run() {
		// x_n-1^
		int red = 0;
		int green = 0;
		int blue = 0;
		for (int i = 0; i < input.length; i++) {
			// d_n^ = Q(d_n) = Q(x_n - x_n-1^)
			int rd = quantizeRed(input[i], red);
			int gd = quantizeGreen(input[i], green);
			int bd = quantizeBlue(input[i], blue);
			result[i] = ((rd & 0xFF) << 16) | ((gd & 0xFF) << 8) | ((bd & 0xFF) << 0);
			// x_n^ = d_n^ + x_n-1^ 
			red += rd;
			green += gd;
			blue += bd;
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

		}
	}

	private int quantizeRed(int pixel, int prev) {
		int range = (int) Math.pow(2, k);
		double q = 512.0 / range;
		int r = (pixel >> 16) & 0xFF;
		int diffR = r - prev + 256;
		diffR = (int) (Math.floor(diffR / q) * q + q / 2 - 256);
		if (diffR > 127)
			diffR = 127;
		else if (diffR < -128)
			diffR = -128;
		return diffR;
	}

	private int quantizeGreen(int pixel, int prev) {
		int range = (int) Math.pow(2, k);
		double q = 512.0 / range;
		int g = (pixel >> 8) & 0xFF;
		int diffG = g - prev + 256;
		diffG = (int) (Math.floor(diffG / q) * q + q / 2 - 256);
		if (diffG > 127)
			diffG = 127;
		else if (diffG < -128)
			diffG = -128;
		return diffG;
	}

	private int quantizeBlue(int pixel, int prev) {
		int range = (int) Math.pow(2, k);
		double q = 512.0 / range;
		int b = (pixel >> 0) & 0xFF;
		int diffB = b - prev + 256;
		diffB = (int) (Math.floor(diffB / q) * q + q / 2 - 256);
		if (diffB > 127)
			diffB = 127;
		else if (diffB < -128)
			diffB = -128;
		return diffB;
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

	public static void main(String[] args) {
		try {
			File file = new File(args[0]);
			File outputFile = new File(args[1]);
			FileInputStream fin = new FileInputStream(file);
			FileOutputStream fout = new FileOutputStream(outputFile);
			byte[] bytes = fin.readAllBytes();
			Compress compress = new Compress(bytes, Integer.parseInt(args[2]));
			compress.run();
			fout.write(compress.getImage());
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

}
