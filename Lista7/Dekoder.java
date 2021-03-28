import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Dekoder {
	FileInputStream fin;
	FileOutputStream fout;
	int doubleErrs;
	int singleErrs;
	public static final int[][] H = { // parity checker dla kodu (7,4)
			{0,0,0,1,1,1,1},
			{0,1,1,0,0,1,1},
			{1,0,1,0,1,0,1},
	};

	public Dekoder(String[] args) {
		try {
			File in = new File(args[0]);
			File out = new File(args[1]);
			fin = new FileInputStream(in);
			fout = new FileOutputStream(out);
			doubleErrs = 0;
			singleErrs = 0;

		} catch (FileNotFoundException e) {
			System.err.println("Nie ma takiego pliku");
		} catch (NumberFormatException e) {
			System.err.println("Bledny parametr");
		}
	}

	private int getValue(int code) {
		int[] arr = new int[7];
		int[] res = new int[4];
		int parity = code & 1;
		int c = code >>> 1;

		for (int k = 0; k < 7; k++) {
			arr[6 - k] = (c >> k) & 1;
			parity ^= arr[6 - k];
		}
		for (int i = 0; i < H.length; i++) { // H*arr
			for (int j = 0; j < H[i].length; j++)
				res[i] = (res[i] + H[i][j] * arr[j]) % 2;
		}
		int syndrom = (res[3] << 3) | (res[2] << 2) | (res[1] << 1) | (res[0] << 0);

		// jesli syndrom jest zerem, to blad nie wystapil, lub wystapil w bicie
		// parzystosci
		if (parity == 1 && syndrom != 0) {
			singleErrs++;
			arr[syndrom - 1] ^= 1;

		} else if (parity == 0 && syndrom != 0) {
			doubleErrs++;
		}
		return (arr[0] << 3) | (arr[1] << 2) | (arr[2] << 1) | (arr[3] << 0);

	}

	public void decode(int x, int y) {
		try {
			fout.write((getValue(x) & 0x0F) << 4 | (getValue(y) & 0x0F));
		} catch (IOException e) {
			System.err.println("Blad zapisu.");
		}

	}

	public void run() {
		try {
			int b = fin.read();
			while (b != -1) {
				int b2 = fin.read();
				decode(b, b2); // pobranie dwoch kolejnych bajtow, aby zdekodowac je w jeden caly bajt
				b = fin.read();
			}
			System.out.println(
					"Naprawiono " + singleErrs + " pojedynczych bledow, wykryto " + doubleErrs + " podwojnych.");
		} catch (IOException e) {
			System.err.println("Blad odczytu.");
		}
	}

	public static void main(String[] args) {
		Dekoder dekoder = new Dekoder(args);
		dekoder.run();
	}

}
