import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Szum {
	public static Random random;
	public static int times =0;

	public static int togle(int i, double p) {
		if (i == -1)
			return -1;
		for (int j = 0; j < 8; j++) {
			if (random.nextDouble() < p) {
				i = i ^ (1 << j); // odwrocenie j-tego bitu
				times ++;
			}
		}
		return i;
	}

	public static void main(String[] args) {
		try {
			double p = Double.parseDouble(args[0]);
			random = new Random();
			File file = new File(args[1]);
			File outputFile = new File(args[2]);
			FileInputStream fin = new FileInputStream(file);
			FileOutputStream fout = new FileOutputStream(outputFile);
			int b = fin.read();
			while(b != -1) {
				fout.write(togle(b, p));
				b = fin.read();
			}
			System.out.println("Odwrocono "+times+" bitow");
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
