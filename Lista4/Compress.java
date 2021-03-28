import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Compress {
	int[][] bitMap;
	int imageHeight;
	int imageWidth;
	int pixelDepth;

	public Compress(byte[] bytes) {
		imageHeight = ((bytes[15] & 0xFF) << 8) | (bytes[14] & 0xFF);
		imageWidth = ((bytes[13] & 0xFF) << 8) | (bytes[12] & 0xFF);
		pixelDepth = (bytes[16] & 0xFF);
		System.out.println("Width: " + imageWidth + ", Height: " + imageHeight + ", PIXL:" + pixelDepth);
		bitMap = new int[imageWidth][imageHeight];
		int i = 2;
		int w = 0;
		int h = 0;
		int byteIndex = 18;
		while (byteIndex < bytes.length - 26) {
			int color = ((bytes[byteIndex + 2] & 0xFF) << 16) | ((bytes[byteIndex + 1] & 0xFF) << 8)
					| (bytes[byteIndex] & 0xFF); // red, green, blue
			byteIndex += 3;
			bitMap[w][h] = color;
			w++;
			i = 2;
			if (w == imageWidth) {
				w = 0;
				h++;
			}

		}
		double[] ents = entropia(bitMap);
		System.out.println("\nEntropia obrazu wejscowego:");
		System.out.println("Calkowita: " + ents[0]);
		System.out.println("Skladowa RED " + ents[1]);
		System.out.println("Skladowa GREEN " + ents[2]);
		System.out.println("Skladowa BLUE " + ents[3]);
		System.out.println();
		double[][] results = { predictor1(bitMap), predictor2(bitMap), predictor3(bitMap), predictor4(bitMap),
				predictor5(bitMap), predictor6(bitMap), predictor7(bitMap), predictor8(bitMap) };
		double min = Double.MAX_VALUE;
		int jMin = -1;
		double minR = Double.MAX_VALUE;
		int jR = -1;
		double minG = Double.MAX_VALUE;
		int jG = -1;
		double minB = Double.MAX_VALUE;
		int jB = -1;
		for (int j = 0; j < 8; j++) {
			System.out.println("Predyktor: " + (j+1));
			System.out.println("Entropia kodu: " + results[j][0]);
			System.out.println("Entropia kodu dla skladowej RED: " + results[j][1]);
			System.out.println("Entropia kodu dla skladowej GREEN: " + results[j][2]);
			System.out.println("Entropia kodu dla skladowej BLUE: " + results[j][3]);
			System.out.println();
			if (results[j][0] < min) {
				min = results[j][0];
				jMin = j;
			}
			if (results[j][1] < minR) {
				minR = results[j][1];
				jR = j;
			}
			if (results[j][2] < minG) {
				minG = results[j][2];
				jG = j;
			}
			if (results[j][3] < minB) {
				minB = results[j][3];
				jB = j;
			}

		}
		System.out.println("Minimalna entropia: " + min + " dla predyktora: " + (jMin+1));
		System.out.println("Minimalna entropia RED: " + minR + " dla predyktora: " + (jR+1));
		System.out.println("Minimalna entropia GREEN " + minG + " dla predyktora: " + (jG+1));
		System.out.println("Minimalna entropia BLUE: " + minB + " dla predyktora: " + (jB+1));

	}

	public double[] predictor1(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int W = 0;
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];
				map[w][h] = W;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor2(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int N = 0;
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				map[w][h] = N;
				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor3(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int NW = 0;
				if (h + 1 < imageHeight && w - 1 >= 0)
					NW = originalMap[w - 1][h + 1];
				map[w][h] = NW;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor4(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int NW = 0;
				int W = 0;
				int N = 0;

				if (h + 1 < imageHeight && w - 1 >= 0)
					NW = originalMap[w - 1][h + 1];
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];
				map[w][h] = N + W - NW;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor5(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int NW = 0;
				int W = 0;
				int N = 0;

				if (h + 1 < imageHeight && w - 1 >= 0)
					NW = originalMap[w - 1][h + 1];
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];
				map[w][h] = N + (W - NW) / 2;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor6(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int NW = 0;
				int W = 0;
				int N = 0;

				if (h + 1 < imageHeight && w - 1 >= 0)
					NW = originalMap[w - 1][h + 1];
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];
				map[w][h] = W + (N - NW) / 2;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor7(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int W = 0;
				int N = 0;
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];
				map[w][h] = (N + W) / 2;
				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}
		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double[] predictor8(int[][] originalMap) {
		int[][] map = new int[imageWidth][imageHeight];
		int[] result = new int[imageWidth * imageHeight];
		int[] resultRed = new int[imageWidth * imageHeight];
		int[] resultGreen = new int[imageWidth * imageHeight];
		int[] resultBlue = new int[imageWidth * imageHeight];
		int index = 0;
		for (int w = 0; w < imageWidth; w++) {
			for (int h = 0; h < imageHeight; h++) {
				int NW = 0;
				int W = 0;
				int N = 0;

				if (h + 1 < imageHeight && w - 1 >= 0)
					NW = originalMap[w - 1][h + 1];
				if (h + 1 < imageHeight)
					N = originalMap[w][h + 1];
				if (w - 1 >= 0)
					W = originalMap[w - 1][h];

				if (NW >= Math.max(W, N))
					map[w][h] = Math.max(W, N);
				else if (NW <= Math.min(W, N))
					map[w][h] = Math.min(W, N);
				else
					map[w][h] = N + W - NW;

				result[index] = (originalMap[w][h] - map[w][h]) % 256;
				resultRed[index] = ((originalMap[w][h] >> 16 & 0xFF) - (map[w][h] >> 16 & 0xFF)) % 256;
				resultGreen[index] = ((originalMap[w][h] >> 8 & 0xFF) - (map[w][h] >> 8 & 0xFF)) % 256;
				resultBlue[index] = ((originalMap[w][h] & 0xFF) - (map[w][h] & 0xFF)) % 256;
				if (result[index] < 0)
					result[index] += 256;
				if (resultRed[index] < 0)
					resultRed[index] += 256;
				if (resultGreen[index] < 0)
					resultGreen[index] += 256;
				if (resultBlue[index] < 0)
					resultBlue[index] += 256;
				index++;
			}

		}
		double[] ents = { entropia(result), entropia(resultRed), entropia(resultGreen), entropia(resultBlue) };
		return ents;
	}

	public double entropia(int[] set) {
		int[] freq = new int[256];
		for (int i = 0; i < set.length; i++)
			freq[set[i]]++;
		double ent = 0;
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] > 0) {
				double p = (double) freq[i] / set.length;
				ent += p * Math.log(1 / p) / Math.log(2);
			}
		}
		return ent;

	}

	public double[] entropia(int[][] set) {
		int[] freqRed = new int[256];
		int[] freqGreen = new int[256];
		int[] freqBlue = new int[256];
		int[] freqTotal = new int[16777216]; // 2^8 * 2^8 * 2^8
		for (int i = 0; i < set.length; i++) {
			for (int j = 0; j < set[i].length; j++) {
				int red = (set[i][j] >> 16) & 0xFF;
				int green = (set[i][j] >> 8 & 0xFF);
				int blue = set[i][j] & 0xFF;
				freqRed[red]++;
				freqGreen[green]++;
				freqBlue[blue]++;
				freqTotal[set[i][j]]++;
			}
		}
		double entR = 0;
		double entG = 0;
		double entB = 0;
		double entTotal = 0;
		int size = set.length * set[0].length;
		for (int i = 0; i < 256; i++) {
			if (freqRed[i] > 0) {
				double p = (double) freqRed[i] / size;
				entR += p * Math.log(1 / p) / Math.log(2);
			}
			if (freqGreen[i] > 0) {
				double p = (double) freqGreen[i] / size;
				entG += p * Math.log(1 / p) / Math.log(2);
			}
			if (freqBlue[i] > 0) {
				double p = (double) freqBlue[i] / size;
				entB += p * Math.log(1 / p) / Math.log(2);
			}
		}
		for (int i = 0; i < freqTotal.length; i++) {
			if (freqTotal[i] > 0) {
				double p = (double) freqTotal[i] / size;
				entTotal += p * Math.log(1 / p) / Math.log(2);
			}
		}
		double[] result = { entTotal, entR, entG, entB };
		return result;

	}

	public static void main(String[] args) {
		if (args[0].equals("--in")) {
			try {
				File file = new File(args[1]);
				FileInputStream fos = new FileInputStream(file);
				byte[] bytes = fos.readAllBytes();
				Compress cp = new Compress(bytes);

			} catch (FileNotFoundException e) {
				System.out.println("Nie ma takiego pliku");
			} catch (IOException e) {
				System.out.println("Blad odczytu.");
			}
		} else {
			System.out.println("Zly parametr");
		}

	}

}
