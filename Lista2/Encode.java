import java.io.*;

public class Encode {
	File inputFile;
	File outputFile;
	FileInputStream fin;
	FileOutputStream fout;
	HuffmanTree tree;
	StringBuilder buffer; // przechowuje ciag zer i jedynek

	public double entropia(int[] freq, int fileSize) { 
		double ent = 0;
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] > 0) {
				double p = (double) freq[i] / fileSize;
				ent += p * Math.log(1 / p) / Math.log(2);
			}
		}
		return ent;
	}

	public double avgCodeLen(int[] len, int fileSize) {
		double avg = 0;
		for (int i = 0; i < len.length; i++) {
			if (len[i] > 0)
				avg += (double) len[i] / fileSize; 
		}
		return avg;
	}

	public void writeBuffer() {
		try {
			while (buffer.length() >= 8) {
				// jesli buffer zawiera przynajmniej 8 bitow, wycina je i wysyla jako bajt do pliku
				fout.write(Integer.parseInt(buffer.substring(0, 8), 2));
				buffer = buffer.delete(0, 8);
				fout.flush();
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// kontruktor, inicjalizuje strumienie IO oraz drzewo Huffmana
	public Encode(String[] args) {
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
			tree = new HuffmanTree();
			fin = new FileInputStream(inputFile);
			fout = new FileOutputStream(outputFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void run() {
		try {
			int[] frequency = new int[256]; // przechowuje czestosci wystepowania danego symbolu
			int[] codeLength = new int[256]; // przechowuje sume dlugosci kodow danego symbolu

			
			long bytesCovered = 0;
			long fileSize = inputFile.length();
			buffer = new StringBuilder("");
			int current = fin.read();
			
			long start = System.currentTimeMillis();
			while (current != -1) {
				StringBuilder code;
				if (tree.contains(current)) {
					code = tree.getCode(current, true); // otrzymuje kod symbolu z drzewa
					buffer.append(code);

				} else {
					code = tree.getCode(current, false); // otrzymuje kod NYT
					buffer.append(code);
					// wyslij kod ASCII symbolu w formacie 8 bitow
					buffer.append(String.format("%8s", Integer.toBinaryString(current & 0xFF)).replace(' ', '0'));
					codeLength[current] += 8;

				}

				writeBuffer(); // wyslij buffer do pliku
				tree.insertInto(current); // dodaj symbol do drzewa
				frequency[current]++;
				codeLength[current] += code.length();
				current = fin.read(); // nastepny symbol
				bytesCovered++;

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
			if (buffer.length() > 0) {
				while (buffer.length() < 8)
					buffer.append(tree.getCode(' ', false)); // Uzupelnienie buffora do 8 bitow kodami NYT
				writeBuffer();
			}
			long stop = System.currentTimeMillis();
			System.out.println("\nRozmiar pliku wejsciowego: " + inputFile.length() + " B"); // statystyki
			System.out.println("Rozmiar pliku wyjsciowego: " + outputFile.length() + " B");
			System.out.println("Stopien kompresji: " + (double) inputFile.length() / outputFile.length());
			System.out.println("Entropia: " + entropia(frequency, (int) fileSize));
			System.out.println("Srednia dlugosc kodu: " + avgCodeLen(codeLength, (int) fileSize));
			System.out.println("W czasie: " + (double) (stop - start) / 1000 + " s");
			fin.close();
			fout.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) { // start
		Encode en = new Encode(args);	
		en.run();
	}

}
