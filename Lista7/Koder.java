import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Koder {
	FileInputStream fin;
	FileOutputStream fout;
	public static final int[][] G = { // macierz generujaca dla kodu (7,4)
			{1,0,0,0},
			{0,1,0,0},
			{0,0,1,0},
			{0,0,0,1},
			{0,1,1,1},
			{1,0,1,1},
			{1,1,0,1},
	};

	public Koder(String[] args) {
		try {
			File in = new File(args[0]);
			File out = new File(args[1]);
			fin = new FileInputStream(in);
			fout = new FileOutputStream(out);

		} catch (FileNotFoundException e) {
			System.err.println("Nie ma takiego pliku");
		} catch (NumberFormatException e) {
			System.err.println("Bledny parametr");
		}

	}

	public int getCode(int x) {
		int[] res = new int[7];
		int[] arr = new int[4];
		for (int k = 0; k < 4; k++) {
			arr[3 - k] = (x >> k) & 1;
		}

		for (int i = 0; i < G.length; i++) { // G*arr
			for (int j = 0; j < G[i].length; j++)
				res[i] = (res[i] + G[i][j] * arr[j]) % 2;
		}
		int r = res[0];
		int parity = res[0];
		for (int k = 1; k < 7; k++) { // zbudowanie kodu, i doczepienie parzystosci na koncu
			r = (r << 1) | res[k];
			parity ^= res[k];
		}

		r = (r << 1) | (parity);
		return r;
	}

	public void run() {
		try {
			int b = fin.read();
			while (b != -1) {
				int msb = (b >> 4) & 0x0F; // pierwsze 4 bity
				int lsb = b & 0x0F; // ostatnie 4 bity
				fout.write(getCode(msb));
				fout.write(getCode(lsb));
				b = fin.read();
			}
			System.out.println("Zrobione.");
		} catch (IOException e) {
			System.err.println("Blad odczytu.");
		}
	}

	public static void main(String args[]) {
		Koder koder = new Koder(args);
		koder.run();
	}
}
