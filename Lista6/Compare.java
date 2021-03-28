import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Compare {
	
	private int[] orgPixels;
	private int[] compPixels;

	public Compare(byte[] orgBytes, byte[] compBytes) {
		int imageHeight = ((orgBytes[15] & 0xFF) << 8) | (orgBytes[14] & 0xFF);
		int imageWidth = ((orgBytes[13] & 0xFF) << 8) | (orgBytes[12] & 0xFF);
		compPixels = new int[imageHeight * imageWidth];
		orgPixels = new int[imageWidth * imageHeight];
		int i = 0;
		int byteIndex = 18;
		while (byteIndex < orgBytes.length - 26) {
			int color = ((orgBytes[byteIndex + 2] & 0xFF) << 16) | ((orgBytes[byteIndex + 1] & 0xFF) << 8)
					| (orgBytes[byteIndex] & 0xFF); // red, green, blue
			byteIndex += 3;
			orgPixels[i] = color;
			i++;
		}
		i = 0;
		byteIndex = 18;
		while (byteIndex < compBytes.length - 26) {
			int color = ((compBytes[byteIndex + 2] & 0xFF) << 16) | ((compBytes[byteIndex + 1] & 0xFF) << 8)
					| (compBytes[byteIndex] & 0xFF); // red, green, blue
			byteIndex += 3;
			compPixels[i] = color;
			i++;
		}
	}
	public void run() {
		printMse();
		printScr();
	}

	public static void main(String[] args) { // start
		try {
			File orginal = new File(args[0]);
			File compressed = new File(args[1]);
			FileInputStream finOrg = new FileInputStream(orginal);
			FileInputStream finCom = new FileInputStream(compressed);
			byte[] orginalBytes = finOrg.readAllBytes();
			byte[] compressedBytes = finCom.readAllBytes();
			Compare compare = new Compare(orginalBytes, compressedBytes);
			compare.run();
			finOrg.close();
			finCom.close();

		} catch (FileNotFoundException e) {
			System.err.println("Nie ma takiego pliku");
		} catch (IOException e) {
			System.err.println("Blad odczytu.");
		} catch (NumberFormatException e) {
			System.err.println("Bledny parametr");
		}

	}
	
	private void printMse() {
		System.out.println("mse:		" + mse(orgPixels, compPixels));
		System.out.println("mse(r): 	" + mseRed(orgPixels, compPixels));
		System.out.println("mse(g): 	" + mseGreen(orgPixels, compPixels));
		System.out.println("mse(b): 	" + mseBlue(orgPixels, compPixels));
	}

	private void printScr() {
		double snr = snr(orgPixels, compPixels);
		double snrRed = snrRed(orgPixels, compPixels);
		double snrGreen = snrGreen(orgPixels, compPixels);
		double snrBlue = snrBlue(orgPixels, compPixels);
		System.out.println("\nsnr: 		" + snr + "	(" + 10 * Math.log10(snr) + " dB)");
		System.out.println("snr(r): 	" + snrRed + "	(" + 10 * Math.log10(snrRed) + " dB)");
		System.out.println("snr(g): 	" + snrGreen + "	(" + 10 * Math.log10(snrGreen) + " dB)");
		System.out.println("snr(b): 	" + snrBlue + "	(" + 10 * Math.log10(snrBlue) + " dB)");
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
