 package edu.uwm.cs351.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import junit.framework.TestCase;

/**
 * @author * CHRISTIAN OROPEZA CS-351 ...RECIEVED HELP FROM BOOK, CS LIBRARY TUTORS, ONLINE CS TUTOR, AND ADVICE FROM FRIENDS ON HOW TO APPROACH FIXING PERSISTENT BUGS.
 * COLLABORATORS: JOSHUA KNIGHT, JULLIAN MURENO, BIJAY PANTA, JIAHUI YANG , MIGUEL GARCIA, MARAWAN SALAMA, ESTELLE BRADY (WHILE IN TUTORING LIBRARY SECTION) BUT NO CODE WAS SHARED.
 * Online Sources: findNextHelper-> https://stackoverflow.com/questions/22114903/how-to-find-the-next-in-order-successor-in-a-binary-tree
 * 				   get & get helper -> https://makeinjava.com/find-search-node-binary-search-tree-java-recursive-example/
 * 				   Remove Helper from HW9 solution & Put Helper from Add Helper HW9
 */

public class TreeMap<K,V>  extends AbstractMap<K,V> 
{

	// Here is the data structure to use.
	
	private static class Node<K,V> extends DefaultEntry<K,V> 
	{
		Node<K,V> left, right;
		Node<K,V> parent;
		
		Node(K k, V v) 
		{
			super(k,v);
			parent = left = right = null;
		}
	}
	
	private Comparator<K> comparator;
	private Node<K,V> dummy;
	private int numItems = 0;
	private int version = 0;
	
	
	/// Invariant checks:
	
	private static Consumer<String> reporter = (s) -> { System.err.println("Invariant error: " + s); };
	
	private boolean report(String error) 
	{
		reporter.accept(error);
		return false;
	}
	
	/**
	 * Return whether nodes in the subtree rooted at the given node have correct parent
	 * and have keys that are never null and are correctly sorted and are all in the range 
	 * between the lower and upper (both exclusive).
	 * If either bound is null, then that means that there is no limit at this side.
	 * The first problem is found will be reported.
	 * @param node root of subtree to examine
	 * @param p parent of subtree to examine
	 * @param lower value that all nodes must be greater than.  If null, then
	 * there is no lower bound.
	 * @param upper value that all nodes must be less than. If null,
	 * then there is no upper bound.
	 * @return whether the subtree is fine.  If false is 
	 * returned, there is a problem, which has already been reported.
	 */
	private boolean checkInRange(Node<K,V> node, Node<K, V> p, K lower, K upper) 
	{
		if(node == null)	return true;
		
		if(node.key == null)	return report("node.key is NULL");
		
		if(node.parent != p)	return report("nodes parent != p(dummy)");
		
		//	if lower bound is greater than node.key the list is not in range since lower bound is not truly the lowest in the list 
		if(lower != null && comparator.compare(lower, node.key) > 0)	return report("lower generic > node.key");
		
		//	if upper bound is lesser or equal to that of the node.key the list is not in range since upper bound is not the up most node in the list 
		if(upper != null && comparator.compare(upper, node.key) <= 0)	return report("upper generic <= node.key");
		
		//	here we do a recursive call to check the left and then the right of the subtree and the lower then the upper bound while passing the node parameter in 
		//	recursive call. We then return the data that we find that is in range.
		return checkInRange(node.left, node, lower, node.key) && checkInRange(node.right, node, node.key, upper); // TODO
	}
	
	/**
	 * Return the first node in a non-empty subtree.
	 * It doesn't examine the data in the nodes; 
	 * it just uses the structure.
	 * @param r subtree, must not be null
	 * @return first node in the subtree
	 * if r is null simply return that node
	 * if the left sub tree is not null do a
	 * recursive call to return the first/most left node in the tree
	 * otherwise return the root node as usual 
	 */
	private Node<K, V> firstInTree(Node<K, V> r) 
	{
		if(r == null)	return r;

		if(r.left != null)	return firstInTree(r.left);
		
		else	return r;
	}
	
	/**
	 * Return the number of nodes in a binary tree.
	 * @param r binary (search) tree, may be null but must not have cycles + 1 for the root node 
	 * @return number of nodes in this tree
	 */
	private int countNodes(Node<K,V> r) 
	{
		if (r == null) return 0;
		
		return 1 + countNodes(r.left) + countNodes(r.right);
	}
		
	/**
	 * Check the invariant, printing a message if not satisfied.
	 * @return whether invariant is correct
	 */
	private boolean wellFormed() 
	{
		// 1. check that comparator is not null
		if(comparator == null)	return report("comparator is NULL");		
		
		// 2. check that dummy is not null
		if(dummy == null)	return report("dummy is NULL");
		
		// 3. check that dummy's key, right subtree and parent are null
		if(dummy.right != null || dummy.key != null || dummy.parent != null)	return report("Dummy key or subtree or parent not NULL");
		
		// 4. check that all (non-dummy) nodes are in range & 5. check that all nodes have correct parents
	    if(!checkInRange(dummy.left, dummy, null, null))	return false;
		
		// 6. check that number of items matches number of (non-dummy) nodes - "checkInRange" will help with 4,5
		if(countNodes(dummy.left) != numItems)	return report("•countNodes != manyItems•");
		
		return true;
	}
	
	
	/// constructors
	
	private TreeMap(boolean ignored) { } // do not change this.
	
	public TreeMap() 
	{
		this(null);
		assert wellFormed() : "invariant broken after constructor()";
	}
	
	/**
	 * Update the parameter comparator if necessary
	 * Create the dummy node.
	 * create a new Comparator
	 * in which you cast the first argument to Comparable<E> so that you use compareTo.
	 * (Lambda syntax will make the code shorter, but is not required.)
	 */
	@SuppressWarnings("unchecked")
	public TreeMap(Comparator<K> c) 
	{
		if(c == null)	comparator = (K a, K b) -> ((Comparable<K>) a).compareTo(b);
		
		else	comparator = c;
		
		dummy = new Node<K, V>(null, null); 
		
		assert wellFormed() : "invariant broken after constructor(Comparator)";
	}

	@SuppressWarnings("unchecked")
	private K asKey(Object x) 
	{
		if (dummy.left == null || x == null) return null;
		
		try 
		{
			comparator.compare(dummy.left.key,(K)x);
			comparator.compare((K)x,dummy.left.key);
			return (K)x;
		} 
		
		catch (ClassCastException ex) 
		{
			return null;
		}
	}
	
	
	private Node<K, V> findKeyHelper(Node<K, V> r, K okay)
	{
		if(r == null || okay == null)	return null;
		
		//	a match has been found so return the root Node
		else if(comparator.compare(r.key, okay) == 0)	return r;
		
		//	if r.key is less than the node being compared to then check the right subtree of the root otherwise check the left sub tree if not found
		else if(comparator.compare(r.key, okay) < 0) r = findKeyHelper(r.right, okay);
		
		else	r = findKeyHelper(r.left, okay);
			
		return r;
	}

	/**
	 * Find the node for a given key.  Return null if the key isn't present
	 * in the tree.  This helper method assumes that the tree is well formed,
	 * but doesn't check that.
	 * @param o object treated as a key.
	 * @return node whose data is equal to o, 
	 * or null if no nodes in the tree have this property.
	 */
	private Node<K, V> findKey(Object o)
	{
		//	start at the left of the dummy node since dummy is at top and dummy.right is always null. We then know that dummy.left is where the root begins
		//	so we begin a recursive call to check there and cast our object being passed as a key
		return findKeyHelper(dummy.left, asKey(o));
	}


	// TODO: many methods to override here:
	// size, containsKey(Object), get(Object), clear(), put(K, V), remove(Object)
	// make sure to use @Override and assert wellformedness
	// plus any private helper methods.
	// Our solution has getNode(Object)
	// Increase version if data structure if modified.
	
	@Override	//	implementation
	public int size() 
	{
		assert wellFormed() : "invariant failed at start of size";

		return numItems;
	}
	
	@Override	//	efficiency
	public boolean containsKey(Object o) 
	{
		assert wellFormed() : "invariant failed at start of containsKey";
		
		//	return true if there is an entry for the given key
		return findKey(o) != null;
	}

	@Override	//	efficiency
	public V get(Object key)
	{
		assert wellFormed() : "invariant failed at start of get";
		
		//	if something is found in find key then return the value of that given key 
		if(findKey(key) != null)	return findKey(key).getValue();
		
		assert wellFormed() : "invariant failed at end of get";
		
		return null;
	}
	
	@Override	//	implementation
	public void clear() 
	{
		assert wellFormed() : "invariant failed at start of clear";
		
		if(numItems == 0)	return;
		
		numItems = 0;
		dummy.left =  null;
		++version;
		
		assert wellFormed() : "invariant failed at end of clear";
	}
	
	//	add helper from the hw 9 but this time we use parent pointers
	private Node<K, V> putHelper(Node<K, V> r, K key, V value)
	{
		
		//	if node is null create a new node and assign r to new node. 
		// we assign r's parent to dummy since dummy.left is r 
		if(r == null)
		{
			r = new Node<K, V>(key, value);
			
			r.parent = dummy;
			
			return r;
		}
	
		//	if r.key string is greater than key node being compared to 
		//	put node to the left sub tree with a recursive call
		//	update r.left.parent pointer to r
		else if(comparator.compare(r.key, key) > 0)
		{
			r.left = putHelper(r.left, key, value);
			
			r.left.parent = r;
		}
		
			//	otherwise if r.key string is less than key node being compared to 
			//	put node to the right sub tree with a recursive call
			//	update r.right.parent pointer to r
		else
		{
			r.right = putHelper(r.right, key, value);
			
			r.right.parent = r;
		}
		
		return r;
	}
	
	/**
	 * Update node value to new value and return old value
	 */
	@Override	//	implementation
	public V put(K key, V value)
	{
		assert wellFormed() : "invariant failed at start of put";
		
		if(key == null)	throw new NullPointerException("key is NULL");
		
		//	we store the old value of the key being added into a variable so that it can be later returned
		V val = get(key);

		//	if our method containsKey is true when passing our desired key parameter we can then
		//	create a temp node and store the key found with our find key method into it
		//	then we update our temp node value to the value being added
		//	but return the old get key from the val variable we created above
		
		if(containsKey(key) == true)
		{
			Node<K, V> noddy = findKey(key);
			
			noddy.setValue(value);
			
			assert wellFormed() : "invariant failed at end of put";
			
			return val;
		}

		//	otherwise if our method containsKey returns false we 
		//	set the root (dummy.left) to our recursive put helper method method while passing root dummy.left paramter as usual
		//	increment version and number of items
		else if(containsKey(key) == false)
		{
			dummy.left = putHelper(dummy.left, key, value);
			
			++numItems;
			++version;
		}
		
		assert wellFormed() : "invariant failed at end of put";
		
		return val;
	}
	
	//	https://stackoverflow.com/questions/22114903/how-to-find-the-next-in-order-successor-in-a-binary-tree
	//	we also update with parent pointers
	private Node<K, V> findNextHelper(Node<K, V> r)
	{
		if(r != null)
		{
			if(r.right != null)	
	        {
	        	r = r.right;
	        	
	        	while(r.left != null)
	        	{
	        		r = r.left;
	        	}
	        	
	        	return r;
	        }
			
			else
			{
				if(r.parent == null)	return null;
				
				while(r.parent != null )
				{
					if(r.parent.left == r)	return r.parent;
					
					r = r.parent;
				}
			}
		}	//	end of if
		
		return null;
	}
	
	//	this is the efficient boyland remove helper from hw 9 solution
	//	must be modified to account for the updating of parent pointers
	private Node<K, V> removeHelper(Node<K,  V> r, K key)
	{
		//	if the key being passed is equal to the Node of type key and value
		//	we can then begin to check the left sub tree... if null return the right subtree node
		//	if the right sub tree is null.. return the left subtree node
		//	then we create a temp node of key and value type and set it to the first node in the right sub tree
		//	we set the right node of the temp we created to a recursive call of our remove helper and pass in the proper paramters... a node & key
		//	now we do our parent checks based on if the right of our temp node is not null the the parent is the temp node itself.
		//	and then we assing the left child of our temp node to the left child of the node being passed
		if(comparator.compare(key, r.key) == 0)
		{
			if(r.left == null)	return r.right;
			
			if(r.right == null)	return r.left;
			
			Node<K, V> opp = firstInTree(r.right);
					
			opp.right = removeHelper(r.right, opp.key);
			
			if(opp.right != null)	opp.right.parent = opp;
			
			opp.left = r.left;
			
			if(opp.left != null)	opp.left.parent = opp;
			
			r = opp;
		}
		
		else if(comparator.compare(key, r.key) < 0 )	
		{
			r.left = removeHelper(r.left, key);
			
			if(r.left != null)	r.left.parent = r;
		}
		
		else	
		{
			r.right = removeHelper(r.right, key);
			
			if(r.right != null)	r.right.parent = r;
		}
		
		return r;
	}
	
	/**
	 * Update node value to new value and return old value
	 * Remove the entry for this key (if any). Return the previous value associated with
     * this key, or null.      
	 */
	@Override	//	implementation
	public V remove(Object key)
	{
		assert wellFormed() : "invariant failed at start of remove";
	
		if(containsKey(key) == false)	return null;
		
		//	we store the old value of the key being removed into a variable so that it can be later returned
		V val = get(key);

		//	create a temp node and store the key found with our find key method into it
		Node<K, V> noddy = findKey(key);

		//	set the root (dummy.left) to our recursive put helper method method while passing root dummy.left parameter as usual 
		//	& a key per our remove helper parameters 
		//	we must also update the parent pointer for the root if it is not null to dummy since dummy.left is the root
		//	increment version and decrement number of items
		dummy.left = removeHelper(dummy.left, noddy.key);
		
		if(dummy.left != null) dummy.left.parent = dummy;
		
		--numItems;	
		++version;
		
		assert wellFormed() : "invariant failed at end of remove";
		
		//Return the previous value associated with this key, or null.
		return val;
	}
	
	private volatile Set<Entry<K,V>> entrySet;
	
	@Override	//	required
	public Set<Entry<K, V>> entrySet() 
	{
		assert wellFormed() : "invariant broken at beginning of entrySet";
		
		if (entrySet == null)	entrySet = new EntrySet();
		
		return entrySet;
	}

	/**
	 * The set for this map, backed by the map.
	 * By "backed: we mean that this set doesn't have its own data structure:
	 * it uses the data structure of the map.
	 */
	private class EntrySet extends AbstractSet<Entry<K,V>> 
	{
		// Do NOT add any fields! 
		
		@Override	//	required
		public int size() 
		{
			assert wellFormed() : "Invariant broken at start of EntrySet.size";
			
			return TreeMap.this.size();
		}

		@Override	//	required
		public Iterator<Entry<K, V>> iterator() 
		{
			return new MyIterator();
		}
		
		/**
		 * if o is not an entry (instanceof Entry<?,?>), return false
		 * Otherwise, check the entry for this entry's key.
		 * If there is no such entry return false;
		 * Otherwise return whether the entries match (use the equals method of the Node class). 
		 * N.B. You can't check whether the key is of the correct type
		 * because K is a generic type parameter.  So you must handle any
		 * Object similarly to how "get" does.
		 * if(o == null)	return false;
		 */
		@Override	//	efficiency
		public boolean contains(Object o) 
		{
			assert wellFormed() : "Invariant broken at start of EntrySet.contains";
			
			if(!(o instanceof Entry<?,?>) || o == null)	return false;
			
			//	create a temp node and set it to findkey but we must cast our object being passed as a Entry 
			//	and call . get key to get the key from the casted object
			Node<K, V> noddy = findKey(((Entry<?, ?>) o).getKey());
			
			if(noddy == null)	return false;
			
			assert wellFormed() : "Invariant broken at end of EntrySet.contains";
			
			//	return true of our temp node is equal to the object being passed otherwise return false
			return (noddy.equals(o));
		}

		/**
		 * if the tree doesn't contain x, return false
		 * otherwise do a TreeMap remove.
		 * make sure that the invariant is true before returning.
		 */
		@Override	//	efficiency
		public boolean remove(Object x) 
		{
			assert wellFormed() : "Invariant broken at start of EntrySet.remove";
			
			if(contains(x) == false || x  == null)	return false;
			
			TreeMap.this.remove(((Entry<?, ?>) x).getKey());
			
			assert wellFormed() : "Invariant broken at end of EntrySet.clear";
			
			return true;
		}
		
		@Override	//	efficiency
		public void clear() 
		{
			assert wellFormed() : "Invariant broken at start of EntrySet.clear";

			TreeMap.this.clear();
			
			assert wellFormed() : "Invariant broken at end of EntrySet.clear";
		}
	}
	
	/**
	 * Iterator over the map.
	 * We use parent pointers.
	 * current points to node (if any) that can be removed.
	 * next points to dummy indicating no more next.
	 */
	private class MyIterator implements Iterator<Entry<K,V>> 
	{
		
		Node<K, V> current, next;
		int colVersion = version;
		
		boolean wellFormed() 
		{
			// TODO: See Homework description for more details.  Here's a summary:
			// (1) check the outer wellFormed()
			if (!TreeMap.this.wellFormed())	return false;
			
			// (2) If version matches, do the remaining checks:
			if (!(version == colVersion))	return true;
			
			//     (a) current should either be null or a non-dummy node in the tree
			if(current != null && foundCurrent(dummy.left) == false)	return report("current not null & not a valid node in tree");
			
			//     (b) next should never be null and should be in the tree (maybe dummy).
			if(next == null && next != dummy)	return report("next is null & next != dummy");
			
			//     (c) if current is not null, make sure it is the last node before where next is.
			if(current != null && next != findNextHelper(current))	return report("current not null but next not last node b4 next");
			
			return true;
		}
		
		MyIterator(boolean ignored) {} // do not change this
		
		/**
		 * initialize next to the leftmost node
		 */
		MyIterator() 
		{
			next = firstInTree(dummy);
			
			assert wellFormed() : "invariant broken after iterator constructor";
		}
		
		/**
		 * Return whether the cursor was found in the tree.
		 * If the cursor is null, it should always be found since 
		 * a binary search tree has many null pointers in it.
		 * This method doesn't examine any of the data elements;
		 * it works fine even on trees that are badly structured, as long as
		 * they are height-bounded.
		 * @param r subtree to check, may be null, but must have bounded height
		 * @return true if the cursor was found in the subtree either left or right sub-tree
		 * if r is not but is not bounded to height of a cursor return false
		 */
		private boolean foundCurrent(Node<K, V> r) 
		{
			if(current == null || current == r)	return true;
			
			if(r == null)	return false;
			
			return foundCurrent(r.left) || foundCurrent(r.right); 
		}
		
		public void checkVersion() 
		{
			if (version != colVersion) throw new ConcurrentModificationException("stale iterator");
		}
		
		@Override	//	required
		public boolean hasNext() 
		{
			assert wellFormed() : "invariant broken before hasNext()";
			checkVersion();

			return next != dummy;
		}

		@Override //	required
		public Entry<K, V> next() 
		{
			assert wellFormed() : "invariant broken at start of next()";
			if (!hasNext()) throw new NoSuchElementException("There is no next element");
			
			current = next;
			
			if(current.right != null)	next = firstInTree(current.right);
			
			else if(current.right == null)	next = findNextHelper(next);
			
			assert wellFormed() : "invariant broken at end of next()";
			
			return current;
		}

		/**
		 * check that there is something to remove.
		 * Use the remove method from TreeMap to remove it.
		 * After removal, record that there is nothing to remove any more.
		 * Handle versions.
		 */
		@Override	//	implementation
		public void remove() 
		{
			assert wellFormed() : "invariant broken at start of iterator.remove()";
			checkVersion();
			if(current == null)	throw new IllegalStateException("current is null");
			
			TreeMap.this.remove(current.key);
			
			current = null;	
			colVersion = ++version;

			assert wellFormed() : "invariant broken at end of iterator.remove()";
		}
		
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	/// Junit test case of private internal structure.
	// Do not change this nested class.
	
	public static class TestSuite extends TestCase {
		
		protected Consumer<String> getReporter() {
			return reporter;
		}
		
		protected void setReporter(Consumer<String> c) {
			reporter = c;
		}

		protected static class Node<K,V> extends TreeMap.Node<K, V> {
			public Node(K k, V v) {
				super(k,v);
			}
			
			public void setLeft(Node<K,V> n) {
				this.left = n;
			}
			
			public void setRight(Node<K,V> n) {
				this.right = n;
			}
			
			public void setParent(Node<K,V> n) {
				this.parent = n;
			}
		}
		
		protected class MyIterator extends TreeMap<Integer,String>.MyIterator {
			public MyIterator() {
				tree.super(false);
			}
			
			public void setCurrent(Node<Integer,String> c) {
				this.current = c;
			}
			public void setNext(Node<Integer,String> nc) {
				this.next = nc;
			}
			public void setColVersion(int cv) {
				this.colVersion = cv;
			}
			
			@Override // make visible
			public boolean wellFormed() {
				return super.wellFormed();
			}
		}
		
		protected TreeMap<Integer,String> tree;
		
		@Override // implementation
		protected void setUp() {
			tree = new TreeMap<>(false);
		}

		protected boolean wellFormed() {
			return tree.wellFormed();
		}
		
		protected void setDummy(Node<Integer,String> d) {
			tree.dummy = d;
		}
		
		protected void setNumItems(int ni) {
			tree.numItems = ni;
		}
		
		protected void setComparator(Comparator<Integer> c) {
			tree.comparator = c;
		}
		
		protected void setVersion(int v) {
			tree.version = v;
		}

		protected Node<Integer,String> findKey(Object key) {
			return (Node<Integer,String>)tree.findKey(key);
		}
	}
}
