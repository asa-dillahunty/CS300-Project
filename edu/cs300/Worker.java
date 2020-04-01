/**
 * The base for this was supplied by Dr. Anderson, but I
 * made many renovations.
 */

 package edu.cs300;

import CtCILibrary.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

class Worker extends Thread {
	Trie textTrieTree;
	ArrayBlockingQueue<String> prefixRequestArray;
	ArrayBlockingQueue<ResultMessage> resultsOutputArray;
	int id;
	String passageName;
	String passagePath;
	Boolean running;
	int prefixCount;

	public Worker(String path,int id,ArrayBlockingQueue<String> prefix, ArrayBlockingQueue<ResultMessage> results){
		this.textTrieTree = null;
		this.prefixRequestArray=prefix;
		this.resultsOutputArray=results;
		this.id=id;
		
		String[] splitPath = path.split("/");
		this.passageName=splitPath[splitPath.length-1]; // put name of passage here

		this.passagePath = path;
		this.running = true;
		this.prefixCount = 1;
	}

	/**
	 * This function returns a list of valid words contained in the worker's 
	 * assigned text file
	 * 
	 * @return list of valid words
	 */
	public ArrayList<String> getWordList() {
		ArrayList<String> goodWords = new ArrayList<String>();
		String word;
		Scanner passage;

		try {
			passage = new Scanner(new File(this.passagePath));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return goodWords;
		}

		// this sets every character that isn't in the alphabet and isn't "'" or "-"
		// to be read as a space and essentially ignored
		passage.useDelimiter("[^a-zA-Z\'-]");

		while (passage.hasNext()) {
			word = passage.next();
			if (word.length() < 3 || word.contains("\'") || word.contains("-")) continue;

			goodWords.add(word.toLowerCase());
		}

		return goodWords;
	}

	public void run() {
		System.out.println("Worker-"+this.id+" ("+this.passageName+") thread started ...");
		this.textTrieTree = new Trie(getWordList());
		// System.out.println("Worker-"+this.id+" Max Depth:"+this.textTrieTree.maxDepth());
		// System.out.println("Worker-"+this.id+" Longest String:"+this.textTrieTree.longestString());
		

		while (running) {
			try {
				// System.out.println("Worker");
				String prefix = this.prefixRequestArray.take();
				if (prefix.length()<3) break;
				String word;

				boolean found = this.textTrieTree.contains(prefix);
				
				if (!found){
					System.out.println("Worker-"+this.id+" "+prefixCount+":"+ prefix+" ==> not found ");
					resultsOutputArray.put(new ResultMessage(this.id, "",false));
				} else{
					word = this.textTrieTree.longestWithPrefix(prefix);
					System.out.println("Worker-"+this.id+" "+prefixCount+":"+ prefix+" ==> "+word);
					resultsOutputArray.put(new ResultMessage(this.id,word,true));
				}

				this.prefixCount++;
				
			} catch(InterruptedException e){
				System.err.println(e.getMessage());
			}
		}
	}

	public void kill() {
		running = false;
	}

}

/**
 * This class is used to simplify the process of getting results from a worker
 */
class ResultMessage {
	public int worker_id;
	public String longestWord;
	public boolean found;

	public ResultMessage(int id, String word, boolean found) {
		this.worker_id = id;
		this.longestWord = word;
		this.found = found;
	}
}