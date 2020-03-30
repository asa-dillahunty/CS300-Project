package CtCILibrary;

import java.util.HashMap;
// import java.util.Iterator;
import java.util.Set;

/* One node in the trie. Most of the logic of the trie is implemented
 * in this class.
 */
public class TrieNode {
	/* The children of this node in the trie.*/
	private HashMap<Character, TrieNode> children;
	private boolean terminates = false;

	// The character stored in this node as data.
	private char character;	

	/* Constructs a trie node and stores this character as the node's value.
	 * Initializes the list of child nodes of this node to an empty hash map. */
	public TrieNode() {
		children = new HashMap<Character, TrieNode>();
		// this.character = null;
	}

	/* Constructs a trie node and stores in the node the char passed in
	 * as the argument. Initializes the list of child nodes of this
	 * node to an empty hash map.
	 */
	public TrieNode(char character) {
		this();
		this.character = character;
	}

	/* Returns the character data stored in this node. */
	public char getChar() {
		return character;
	}

	/* Add this word to the trie, and recursively create the child
	 * nodes. */
	public void addWord(String word) {
		if (word == null || word.isEmpty()) {
			return;
		}
		
		char firstChar = word.charAt(0);

		TrieNode child = getChild(firstChar);
		if (child == null) {
			child = new TrieNode(firstChar);
			children.put(firstChar, child);
		} 

		if (word.length() > 1) {
			child.addWord(word.substring(1));
		} else {
			child.setTerminates(true);
		}
	}

	/* Find a child node of this node that has the char argument as its
	 * data. Return null if no such child node is present in the trie.
	 */
	public TrieNode getChild(char c) {
		return children.get(c);
	}

	/* Returns whether this node represents the end of a complete word. */
	public boolean terminates() {
		return terminates;
	}
	
	/* Set whether this node is the end of a complete word.*/
	public void setTerminates(boolean t) {
		terminates = t;
	}

	public int maxDepth() {
		if (children == null) return 0;

		int max = 0;
		int depth;
		// Iterator<HashMap.Entry<Character, TrieNode>> entrySet = children.entrySet().iterator();
		Set<Character> keyset = children.keySet();
		for (char key : keyset) {
			depth = children.get(key).maxDepth();
			if (depth > max) max = depth;
		}

		return max+1;
	}

	/**
	 * We assume the prefix is in the tree because Trie should check
	 * contains first
	 * 
	 * @param prefix
	 * @return
	 */
	public int maxDepth(String prefix) {
		if (prefix.length() == 0) return maxDepth();

		// char care = prefix.charAt(0);
		// TrieNode temp = children.get(care);
		// String newPrefix = prefix.substring(1);
		// return temp.maxDepth(newPrefix) + 1;
		return children.get(prefix.charAt(0)).maxDepth(prefix.substring(1))+1;
	}

	public String longestSubString() {
		if (children == null) return "";

		String longestSubString = "";
		String subString;
		// Iterator<HashMap.Entry<Character, TrieNode>> entrySet = children.entrySet().iterator();
		Set<Character> keyset = children.keySet();
		for (char key : keyset) {
			subString = children.get(key).longestSubString();
			if (subString.length() > longestSubString.length()) longestSubString = subString;
			else if (subString.length() >= longestSubString.length()) {
				if (subString.compareTo(longestSubString) > 0) longestSubString = subString;
			}
		}

		return character+longestSubString;
	}

	public String longestWithPrefix(String prefix) {
		if (prefix.length() == 0) return longestSubString();

		// char care = prefix.charAt(0);
		// TrieNode temp = children.get(care);
		// String newPrefix = prefix.substring(1);
		// return this.character + temp.longestWithPrefix(newPrefix);
		return this.character + children.get(prefix.charAt(0)).longestWithPrefix(prefix.substring(1)); 
	}
}
