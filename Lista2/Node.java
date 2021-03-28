
public class Node {
	
	Node right;
	Node left;
	Node parent;
	int val;
	int weight;
	int index;
	boolean isNYT;
	boolean isLeaf;


	public Node(Node parent) { //Konstruktor nowego NYT
		this.parent = parent;
		this.weight = 0;
		this.index = 0;
		this.isNYT = true;
	}
	
	
	public Node(Node parent, int c) { //Konstruktor liscia
		this.parent = parent;
		this.weight = 1;
		this.index = 1;
		this.val = c;
		this.isLeaf = true;
		this.isNYT = false;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + val;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + index;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result + weight;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (val != other.val)
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (index != other.index)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		if (weight != other.weight)
			return false;
		return true;
	}

}
