import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HuffmanTree {

	public Node root; 
	public Node NYT; 

	// Mapa laczaca symbol z wezlem drzewa, ktory go reprezentuje. Kontroluje czy symbol pojawil sie w drzewie
	private Map<Integer, Node> registry = new HashMap<Integer, Node>(); 
	
	// Lista wezlow w kolejnosci ich indeksow
	private List<Node> nodesInOrder = new ArrayList<Node>();

	public HuffmanTree() { // inicjalizaja
		this.root = new Node(null);
		this.NYT = root;
		nodesInOrder.add(root);
	}

	public void insertInto(int c) {
		if (registry.containsKey(c)) { // jesli symbol znajduje sie juz w drzewie, zmodyfikuj je
			Node toUpdate = registry.get(c);
			updateTree(toUpdate);
		} else {
			Node parent = addNewNode(c); // wpp. dodaj nowy wezel i zmodyfikuj drzewo
			updateTree(parent);
		}
	}

	public boolean contains(int value) { // meoda przydatna przy dekodowaniu
		return registry.containsKey(value);
	}

	public StringBuilder getCode(int c, boolean registered) { 
		StringBuilder code = new StringBuilder("");
		if (!registered) {
			if (!NYT.equals(root))
				code = generateCode(NYT); // jesli symbolu nie ma w drzewie, wyslij kod NYT
		} else {
			code = generateCode(registry.get(c)); // wpp. wyslij jego kod
		}
		return code;
	}


	public boolean isEmpty() {
		return root.equals(NYT);
	}

	public void printTree() { // pomocnicza metoda drukujaca drzewo w postaci listy zagniezdzonej
		printTree(root);
		System.out.print("\n");
	}

	private Node addNewNode(int c) {
		Node newNYT = new Node(NYT); // stworzenie nowego wezla jako dziecko NYT
		Node leaf = new Node(NYT, c); // stworzenie nowego wezla jako dziecko NYT z podan wartoscia
		registry.put(c, leaf);   // zarejestruj nowy symbol
		nodesInOrder.add(0, leaf); // dodaj nowy wezel na poczatku listy,  nastepne indeksy zwieksza sie o 1
		nodesInOrder.add(0, newNYT);

		Node oldNYT = NYT; // przepiecie wskaznikow
		NYT.isNYT = false; 
		NYT.left = newNYT;
		NYT.right = leaf;
		NYT = newNYT; 
		updateNodesIndexes(); // aktualizuj indeksy wezlow
		return oldNYT; // zwroc ojca
	}

	private void updateTree(Node node) {
		while (!node.equals(root)) { 
			if (! isMaxInBlock(node)) { // czy wezel ma nie ma najwiekszego indeksu sposrod tych z taka sama waga?
				Node max = maxNodeInBlock(node); 	// znajdz tego z najwiekszym indeksem
				swap(max, node); 					// i zamien
			}
			node.weight++; 		// zwieksz wage po drodze
			node = node.parent; // i przejdz do wezla wyzej
		}
		node.weight++; // zwieksz korzen
		node.index = nodesInOrder.indexOf(node); // aktualizuj indeks korzenia
	}

	private boolean isMaxInBlock(Node node) {
		int index = nodesInOrder.indexOf(node);
		int weight = node.weight;
		for (int i = index + 1; i < nodesInOrder.size(); i++) {
			Node next = nodesInOrder.get(i);
			if (!next.equals(node.parent) && next.weight == weight) { // odrzucamy relacje syn-rodzic
				return false;
			} else if (!next.equals(node.parent) && next.weight > weight) {
				return true;
			}
		}
		return false;
	}

	private Node maxNodeInBlock(Node node) {
		int index = node.index; 
		int weight = node.weight;
		Node next;
		do{
			index++;
			next = nodesInOrder.get(index);
		}
		while (next.weight == weight);
		index--; // szukamy poprzedniego
		return nodesInOrder.get(index);

	}

	private void swap(Node n, Node m) {

		int newIndex = n.index;
		int oldIndex = m.index;

		Node parentN = n.parent; // pomocnicze wskazniki na rodzicow
		Node parentM = m.parent;

		if (parentN.left.equals(n)) // trzeba okreslic czy swapowane wezly sa lewymi czy prawymi dziecmi
			parentN.left = m;
		else
			parentN.right = m;

		if (parentM.left.equals(m))
			parentM.left = n;
		else
			parentM.right = n;
		n.parent = parentM;
		m.parent = parentN;

		nodesInOrder.set(newIndex, m);
		nodesInOrder.set(oldIndex, n);
		updateNodesIndexes();
	}

	private void updateNodesIndexes() { // przepisuje wezlom ich indeksy wed�ug kolejnosci w liscie
		for (int i = 0; i < nodesInOrder.size(); i++) {
			Node node = nodesInOrder.get(i);
			node.index = nodesInOrder.indexOf(node);
		}

	}

	private StringBuilder generateCode(Node in) { // buduje kod symbolu od tylu, zaczynajac od liscia 
		StringBuilder code = new StringBuilder("");
		Node n = in;
		Node p = in.parent; 
		while (p != null) {
			if (p.left.equals(n)) {
				code.insert(0, '0');
			} else {
				code.insert(0, '1');
			}
			n = p;
			p = p.parent;
		}
		return code;
	}

	public void printTree(Node root) {
		if (root == null)
			return;
		System.out.print("[");
		System.out.print("(" + root.index + "-" + root.val + "-" + root.weight + ")" + ",");
		printTree(root.left);
		printTree(root.right);
		System.out.print("] ");
	}

}
