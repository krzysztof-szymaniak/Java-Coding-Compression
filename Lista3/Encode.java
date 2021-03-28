import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Encode {

	private File inputFile;
	private File outputFile;
	private FileInputStream fin;
	private FileOutputStream fout;
	private int numBits; // liczba bitow wykorzystywana do kodowania
	private int buffer; // buffer przechowujacy bity do wyslania
	private int bufferSize; // liczba bitow w bufferze
	private int dictSize; // rozmiar slownika
	private Map<String, Integer> dictionary; // mapa (slowo -> jego kod)

	public Encode(String[] args) { // konstruktor, inicjalizuje strumienie IO oraz wartosci zmiennych
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
			dictionary = new HashMap<String, Integer>();
			dictSize = 256;
			for (int i = 0; i < dictSize; i++) {// inicjalizacja slownika z pojedycznycmi znakami
				dictionary.put("" + (char) i, i);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void run() {
		try {
			int[] freqChars = new int[256];
			Map<String, Integer> freqStrings = new HashMap<String, Integer>();

			long fileSize = inputFile.length();
			String p = "";

			long start = System.currentTimeMillis();

			int val = fin.read();
			long bytesCovered = 1;
			while (val != -1) {
				freqChars[val]++;
				char c = (char) val;
				String pc = p + c; // zbuduj slowo z nastepnego znaku
				if (freqStrings.containsKey(pc)) {
					int freq = freqStrings.get(pc);
					freq++;
					freqStrings.put(pc, freq);
				} else {
					freqStrings.put(pc, 1);
				}
				
				if (dictionary.containsKey(pc)) {
					p = pc;
				} else { // jesli slowa nie ma w slowniku to znajdz kod slowa bez ostaniego znaku i je wyslij
					int i = dictionary.get(p);
					addToBuffer(i);
					writeBuffer();
					dictSize++;
					if (dictSize == (1 << numBits) - 1) { // jesli slownik zbliza sie do max pojemnosci (2^numBits - 1),
						numBits++; // zwieksz liczbe bitow potrzebnych do kodowania
					}
					dictionary.put(pc, dictSize); // dodaj jako nowe slowo i rozbuduj slownik
					p = "" + c;
				}
				val = fin.read(); // nastepny symbol
				bytesCovered++;
				printProgress(bytesCovered, fileSize);
				System.out.println("Stopien kompresji: "+ (double) bytesCovered / outputFile.length());
				System.out.println("Entropia danych: "+ entropia(freqChars, bytesCovered));
				System.out.println("Entropia kodu: "+ entropia(freqStrings, bytesCovered));

			}
			if (!p.equals("")) { // wyslij ostatnie slowo
				addToBuffer(dictionary.get(p));
				writeBuffer();
			}
			if (bufferSize > 0) { // ewentualny padding zerami
				while (bufferSize < 8)
					addToBuffer(0);
				writeBuffer();
			}

			long stop = System.currentTimeMillis();
			System.out.println("\nRozmiar pliku wejsciowego: " + fileSize + " B"); // statystyki
			System.out.println("Rozmiar pliku wyjsciowego: " + outputFile.length() + " B");
			System.out.println("Stopien kompresji: "+ (double) bytesCovered / outputFile.length());
			System.out.println("Entropia danych: " + entropia(freqChars, bytesCovered));
			System.out.println("Entropia kodu: " + entropia(freqStrings, bytesCovered));
			System.out.println("W czasie: " + (double) (stop - start) / 1000 + " s");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void addToBuffer(int val) { // nauczylem sie wreszcie operacji na bitach :)
		buffer <<= numBits; // przsuniecie w lewo bitow w bufforze
		buffer |= val; 		// dopisanie do buffora nastepnej liczby
		bufferSize += numBits;
	}

	private void writeBuffer() {
		try {
			while (bufferSize >= 8) {
				int b = buffer >> (bufferSize - 8); // shift tak, aby wyslac 8 najstarszych bitow buffora
				fout.write(b);
				fout.flush();
				buffer &= (1 << (bufferSize - 8)) - 1; // zerowanie najstarszych bitow
				bufferSize -= 8;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double entropia(int[] freq, long fileSize) {
		double ent = 0;
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] > 0) {
				double p = (double) freq[i] / fileSize;
				ent += p * Math.log(1 / p) / Math.log(2);
			}
		}
		return ent;
	}

	public double entropia(Map<String, Integer> freq, long fileSize) {
		double ent = 0;
		for (String i : freq.keySet()) {
			double p = (double) freq.get(i) / fileSize;
			ent += p * Math.log(1 / p) / Math.log(2);
		}
		return ent;
	}

	public void printProgress(long bytesCovered, long fileSize) { // pasek postepu
		System.out.print("Encoding progress: [");
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
		Encode en = new Encode(args);
		en.run();

	}

}
