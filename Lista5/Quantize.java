import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

public class Quantize {

	private int imageHeight;
	private int imageWidth;
	private int[] bitMap;
	private int[] result;
	private byte[] bytes;

	public Quantize(byte[] bytes) {
		this.bytes = bytes;
		imageHeight = ((bytes[15] & 0xFF) << 8) | (bytes[14] & 0xFF);
		imageWidth = ((bytes[13] & 0xFF) << 8) | (bytes[12] & 0xFF);
		result = new int[imageHeight * imageWidth];
		bitMap = new int[imageWidth * imageHeight];
		int i = 0;
		int byteIndex = 18;
		while (byteIndex < bytes.length - 26) {
			int color = ((bytes[byteIndex + 2] & 0xFF) << 16) | ((bytes[byteIndex + 1] & 0xFF) << 8)
					| (bytes[byteIndex] & 0xFF); // red, green, blue
			byteIndex += 3;
			bitMap[i] = color;
			i++;
		}
	}

	// bruteforce jest malo elegancki, ale skuteczny
	private void run(int bitSize, String type) {
		HashMap<Double, int[]> map = new HashMap<Double, int[]>(); // mapa trojek bitow RGB -> ich mse/snr
		for (int i = bitSize; i >= 0; i--) { // i + j + k = bitSize
			for (int j = bitSize - i; j >= 0; j--) {
				int k = bitSize - j - i;
				if (i <= 8 && j <= 8 && k <= 8) { 
					quantize(i, j, k);
					if (type.equals("mse")) {
						double max = Math.max(Math.max(mseRed(bitMap, result), mseGreen(bitMap, result)),
								mseBlue(bitMap, result)); // najwiekszy mse sposrod trzech kanalow RGB
						int[] bits = { i, j, k };
						map.put(max, bits);
					} else if (type.equals("snr")) {
						double min = Math.min(Math.min(snrRed(bitMap, result), snrGreen(bitMap, result)),
								snrBlue(bitMap, result)); // najmniejszy snr sposrod trzech kanalow RGB
						int[] bits = { i, j, k };
						map.put(min, bits);
					} else {
						System.err.println("Niewlasciwy parametr, oczekiwany \'mse\' lub \'snr\'");
						System.exit(1);
					}
				}
			}
		}
		int[] bits; //
		if (type.equals("mse")) {
			double min = Collections.min(map.keySet()); // najmniejszy z najwiekszych mse
			bits = map.get(min);
		} else {
			double max = Collections.max(map.keySet()); // najwiekszy z najmniejszych snr
			bits = map.get(max);
		}
		//int[] bits = {0, 0, 0};
		System.out.println("RGB bits: 	" + bits[0] + "-" + bits[1] + "-" + bits[2]);
		quantize(bits[0], bits[1], bits[2]);
		printMse();
		printScr();
	}

	private void printMse() {
		System.out.println("mse:		" + mse(bitMap, result));
		System.out.println("mse(r): 	" + mseRed(bitMap, result));
		System.out.println("mse(g): 	" + mseGreen(bitMap, result));
		System.out.println("mse(b): 	" + mseBlue(bitMap, result));
	}

	private void printScr() {
		double snr = snr(bitMap, result);
		double snrRed = snrRed(bitMap, result);
		double snrGreen = snrGreen(bitMap, result);
		double snrBlue = snrBlue(bitMap, result);
		System.out.println("\nsnr: 		" + snr + "	(" + 10 * Math.log10(snr) + " dB)");
		System.out.println("snr(r): 	" + snrRed + "	(" + 10 * Math.log10(snrRed) + " dB)");
		System.out.println("snr(g): 	" + snrGreen + "	(" + 10 * Math.log10(snrGreen) + " dB)");
		System.out.println("snr(b): 	" + snrBlue + "	(" + 10 * Math.log10(snrBlue) + " dB)");
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

	private void quantize(int redBits, int greenBits, int blueBits) {
		 // palety kolorow
		int redShades = (int) Math.pow(2, redBits);
		int greenShades = (int) Math.pow(2, greenBits);
		int blueShades = (int) Math.pow(2, blueBits);

		// szerokosci krokow kwantyzacji
		double qR = 256.0 / redShades;
		double qG = 256.0 / greenShades;
		double qB = 256.0 / blueShades;

		for (int i = 0; i < bitMap.length; i++) {
			int r = (bitMap[i] >> 16) & 0xFF;
			int g = (bitMap[i] >> 8) & 0xFF;
			int b = (bitMap[i] >> 0) & 0xFF;

			// Q(k) = floor(k/q)*q + q/2
			r = (int) (Math.floor(r / qR) * qR + qR / 2);
			g = (int) (Math.floor(g / qG) * qG + qG / 2);
			b = (int) (Math.floor(b / qB) * qB + qB / 2);

			result[i] = (r << 16) | (g << 8) | (b);
		}
	}

	public static void main(String[] args) { // start
		try {
			File file = new File(args[0]);
			File outputFile = new File(args[1]);
			FileInputStream fin = new FileInputStream(file);
			FileOutputStream fout = new FileOutputStream(outputFile);
			byte[] bytes = fin.readAllBytes();
			Quantize q = new Quantize(bytes);
			q.run(Integer.parseInt(args[2]), args[3]);
			fout.write(q.getImage());
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

	private double mse(int[] x, int[] y) {
		double mse = 0;
		for (int i = 0; i < x.length; i++)
			mse += Math.pow((x[i] - y[i]), 2);
		return mse / x.length;
	}

	private double mseRed(int[] x, int[] y) {
		double mse = 0;
		for (int i = 0; i < x.length; i++)
			mse += Math.pow(((x[i] >> 16) & 0xFF) - ((y[i] >> 16) & 0xFF), 2);
		return mse / x.length;
	}

	private double mseGreen(int[] x, int[] y) {
		double mse = 0;
		for (int i = 0; i < x.length; i++)
			mse += Math.pow(((x[i] >> 8) & 0xFF) - ((y[i] >> 8) & 0xFF), 2);
		return mse / x.length;
	}

	private double mseBlue(int[] x, int[] y) {
		double mse = 0;
		for (int i = 0; i < x.length; i++)
			mse += Math.pow(((x[i] >> 0) & 0xFF) - ((y[i] >> 0) & 0xFF), 2);
		return mse / x.length;
	}

	private double snr(int[] x, int[] y) {
		double snr = 0;
		for (int i = 0; i < x.length; i++)
			snr += Math.pow(x[i], 2);
		return snr / (x.length * mse(x, y));
	}

	private double snrRed(int[] x, int[] y) {
		double snr = 0;
		for (int i = 0; i < x.length; i++)
			snr += Math.pow((x[i] >> 16) & 0xFF, 2);
		return snr / (x.length * mseRed(x, y));
	}

	private double snrGreen(int[] x, int[] y) {
		double snr = 0;
		for (int i = 0; i < x.length; i++)
			snr += Math.pow((x[i] >> 8) & 0xFF, 2);
		return snr / (x.length * mseGreen(x, y));
	}

	private double snrBlue(int[] x, int[] y) {
		double snr = 0;
		for (int i = 0; i < x.length; i++)
			snr += Math.pow((x[i] >> 0) & 0xFF, 2);
		return snr / (x.length * mseBlue(x, y));
	}

}
