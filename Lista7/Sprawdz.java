import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Sprawdz {
	static int totalErrs = 0;

	public static int check(int i1, int i2, int startingBit) {
		for (int j = 0; j < 4; j++) {
			if ((i1 & (1 << (j + startingBit))) != (i2 & (1 << (j + startingBit)))) {
				return 1;
			}
		}
		return 0;
	}

	public static void main(String[] args) {
		try {
			File in1 = new File(args[0]);
			File in2 = new File(args[1]);
			FileInputStream fin1 = new FileInputStream(in1);
			FileInputStream fin2 = new FileInputStream(in2);

			int i1 = fin1.read();
			int i2 = fin2.read();
			while (i1 != -1 && i2 != -1) {
				totalErrs += check(i1, i2, 0); // sprawdzenie bloku 0-3
				totalErrs += check(i1, i2, 4); // sprawdzenie bloku 4-7
				i1 = fin1.read();
				i2 = fin2.read();
			}
			System.out.println("Wykryto blad w " + totalErrs + " blokach 4-bitowych");
			fin1.close();
			fin2.close();

		} catch (FileNotFoundException e) {
			System.err.println("Nie ma takiego pliku");
		} catch (IOException e) {
			System.err.println("Blad odczytu.");
		} catch (NumberFormatException e) {
			System.err.println("Bledny parametr");
		}

	}

}
