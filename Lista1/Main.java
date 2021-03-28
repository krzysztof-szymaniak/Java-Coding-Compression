

import java.io.*;
import java.lang.Math;

public class Main {

	public static double entropia(int[] arr, int filesize) {
		double ent = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 0) {
				double p = (double) arr[i] / filesize;
				ent += p * Math.log(1 / p) / Math.log(2);
			}
		}
		return ent;
	}

	public static double entropiaWar(int[][] prev, int[] freq, int filesize) {
		double entWar = 0;
		for (int i = 0; i < prev.length; i++) {
			if (freq[i] > 0) {
				double ent = 0;
				for (int j = 0; j < prev[i].length; j++) {
					if (prev[i][j] > 0) {
						double p = (double) prev[i][j] / freq[i];
						ent += p * Math.log(1 / p) / Math.log(2);
					}
				}
				entWar += (double) freq[i] * ent / filesize;
			}
		}
		return entWar;
	}

	public static void main(String[] args) {
		try {
			String inputFile = "";
			if (args[0].equals("--in"))
				inputFile = args[1];
			 else 
				throw new Exception("Brak argumentu");
			
			long start = System.currentTimeMillis();
			InputStream input = new FileInputStream(inputFile);
			int fileSize = (int) new File(inputFile).length();

			byte[] allBytes = new byte[fileSize]; 

			input.read(allBytes); 

			int[] freq = new int[256]; // czestosc zwykla
			

			int[][] prev = new int[256][256]; // czestosc warunkowana nastepnym symbolem
			

			for (int i = 0; i < fileSize; i++) { // +128 w celu pozbycia sie ujemnych wartosci
				freq[allBytes[i] + 128]++;
				if (i == 0) {
					prev[128][allBytes[i] + 128]++;
				} else {
					prev[allBytes[i - 1] + 128][allBytes[i] + 128]++;
				}
			}

			double ent = entropia(freq, fileSize);
			double entWar = entropiaWar(prev, freq, fileSize);
			System.out.println("Entropia: " + ent);
			System.out.println("Entropia Warunkowa: " + entWar);
			System.out.println("Roznica: " + (ent - entWar));
			long elapsedTime = System.currentTimeMillis() - start;
			System.out.println("W czasie: " + elapsedTime / 1000.0 + " s");

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
