import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Decode {

	File inputFile;
	File outputFile;
	FileInputStream fin;
	FileOutputStream fout;
	HuffmanTree tree;
	StringBuilder buffer; // Przechowuje ciag zer i jedynek
	long bytesCovered; 	// Stopien pokrycia pliku wejsciowego

	public int getNextBitFromBuffer() { // zwraca nastepny bit w buforze
		try {
			if (buffer.length() == 0) { // jesli w buforze nie ma juz bitow
				int b = fin.read();		
				bytesCovered++;
				if (b == -1) {
					return -1; 
				}
				// dopisz nastepny bajt do buffora w postaci 8 bitow
				buffer.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
			}
			if (buffer.charAt(0) == '1') { // usun znak z buffora i zwroc wartosc
				buffer.deleteCharAt(0);
				return 1;
			} else {
				buffer.deleteCharAt(0);
				return 0;
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		return -1;
	}

	public int getNextByteFormBuffer() { // zwraca nastepny bajt w bufforze
		try {
			int b;
			if (buffer.length() < 8) { // jesli buffor krotszy niz 8 bitow, przeczytaj nastepny bajt
				b = fin.read();
				bytesCovered++;
				if (b == -1) {
					return -1;  
				}
				// dopisz nastepny bajt do buffora w postaci 8 bitow
				buffer.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
			}
			b = Integer.parseInt(buffer.substring(0, 8), 2); // zdejmij 8 bitow z buffora i przeparsuj w liczbe
			buffer = buffer.delete(0, 8);
			return b;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void run() {
		try {
			int current = fin.read();
			fout.write(current); // pierwszy znak mozna od razu przepiac i dodac do drzewa
			tree.insertInto(current);
			buffer = new StringBuilder("");
			
			Node node = tree.root;
			current = getNextBitFromBuffer();

			while (current != -1) {
				if (current == 0) 
					node = node.left;
				if (current == 1)  
					node = node.right;
				int value = 0;
				if (node.isNYT) { // jesli trafiamy w NYT, oznacza to, ze w nastepnym bajcie jest kod nowego symbolu
					value = getNextByteFormBuffer();
					if (value == -1) { // koniec pliku
						break;
					}
					fout.write(value);
					tree.insertInto(value); // dodaj wartosc
					node = tree.root; 		// wroc do korzenia
				}
				if (node.isLeaf) { // jesli trafiamy w lisc, wypisz wartosc w tym lisciu
					value = node.val;
					fout.write(value);
					tree.insertInto(value);
					node = tree.root; // wroc do korzenia
				}
				fout.flush();
				current = getNextBitFromBuffer();

				System.out.print("Decoding progress: [");
				int ratio = (int) (100 * (double) bytesCovered / inputFile.length());
				for (int i = 0; i < 100; i++) {
					if (i < ratio)
						System.out.print('#');
					else
						System.out.print('-');
				}
				System.out.println("]");

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public Decode(String[] args) { // inicjalizacja strumieni IO oraz drzewa
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--in")) {
					i++;
					inputFile = new File(args[i]);
				} else if (args[i].equals("--out")) {
					i++;
					outputFile = new File(args[i]);
				}
			}
			tree = new HuffmanTree();
			fin = new FileInputStream(inputFile);
			fout = new FileOutputStream(outputFile);
			;
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) { // start
		Decode dc = new Decode(args);
		long start = System.currentTimeMillis();
		dc.run();
		long stop = System.currentTimeMillis();
		System.out.println("Time: " + (double) (stop - start) / 1000 + " s");
	}

}
