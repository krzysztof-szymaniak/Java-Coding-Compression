import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Decode {

	private File inputFile;
	private File outputFile;
	private FileInputStream fin;
	private FileOutputStream fout;
	private int numBits;		// liczba bitow wykorzystywana do kodowania
	private int buffer;			// buffer przechowujacy bity do wyslania
	private int bufferSize;		// liczba bitow w bufferze
	private int dictSize;		// rozmiar slownika
	private long bytesCovered;	// liczba przetrawionych bajtow z pliku wejscowego
	private Map<Integer, String> dictionary; // mapa (kod -> slowo)


	public Decode(String[] args) { // konstruktor, inicjalizuje strumienie IO oraz wartosci zmiennych
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--in")) {
					i++;
					inputFile = new File(args[i]);
				} else if (args[i].equals("--out")) {
					i++;
					outputFile = new File(args[i]);
				} else
					throw new Exception("Bad params");
			}
			fin = new FileInputStream(inputFile);
			fout = new FileOutputStream(outputFile);
			buffer = bufferSize = 0;
			numBits = 9;
			dictionary = new HashMap<Integer, String>();
			dictSize = 256;
			for (int i = 0; i < dictSize; i++) {
				dictionary.put(i, "" + (char) i);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void run() {
		long fileSize = inputFile.length();
		long start = System.currentTimeMillis();
		int code = readCode(); // odczytaj pierwszy znak i od razu go wypisz
		String oldWord = "" + (char) code;	
		writeString(oldWord);
		code = readCode();
		bytesCovered = 2;
		while (code != -1) {
			String newWord;
			if (dictionary.containsKey(code)) { // jesli slowo jest w slowniku to je znajdz
				newWord = dictionary.get(code);
			} else {
				newWord = oldWord + oldWord.charAt(0); // wpp zbuduj slowo jako poprzednie slowo + znak z poczatku
			}
			writeString(newWord);
			dictSize++;
			if (dictSize == (1 << numBits) - 2) // przeskok bitow musi nastapic o jedna wartosc wczesniej niz podczas zakodowania
				numBits++;
			
			dictionary.put(dictSize, oldWord + newWord.charAt(0)); // zapisz slowo w postaci starego + pierwszy znak nowego
			oldWord = newWord;
			printProgress(fileSize);
			code = readCode();
			
		}
		long stop = System.currentTimeMillis();
		System.out.println("W czasie: " + (double) (stop - start) / 1000 + " s");

	}

	private void writeString(String s) { // zapisuje string do pliku
		try {
			byte[] arr = s.getBytes();
			fout.write(arr);
			fout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int readCode() { // odczytuje kod w zaleznosci od aktualnie wykorzystywanej liczby bitow
		try {
			while (bufferSize < numBits) { 
				int b = fin.read();
				bytesCovered++;
				if (b == -1)
					return -1;
				
				buffer <<= 8; 	// zrob miejsce na nowy bajt
				buffer |= b;	// dopisz bajt do buffora
				bufferSize += 8;
			}
			int code = buffer >> (bufferSize - numBits); 	// odczytaj najstarsze bity z buffora
			buffer &= (1 << (bufferSize - numBits)) - 1;	// wyzeruj najstarsze bity
			bufferSize -= numBits;
			return code;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;

	}
	
	public void printProgress(long fileSize) { // pasek postepu
		System.out.print("Decoding progress: [");
		int ratio = (int) (100 * (double) bytesCovered / fileSize);
		for (int i = 0; i < 100; i++) {
			if (i < ratio)
				System.out.print('#');
			else
				System.out.print('-');
		}
		System.out.println("]");
	}

	public static void main(String[] args) { // start
		Decode dec = new Decode(args);
		dec.run();

	}

}
