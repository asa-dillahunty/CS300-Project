package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

class Worker extends Thread {
	Trie textTrieTree;
	ArrayBlockingQueue<String> prefixRequestArray;
	ArrayBlockingQueue<String> resultsOutputArray;
	int id;
	String passageName;
	String passagePath;
	Boolean running;

	public Worker(String path,int id,ArrayBlockingQueue<String> prefix, ArrayBlockingQueue<String> results){
		// this.textTrieTree=new Trie(words);
		this.textTrieTree = null;
		this.prefixRequestArray=prefix;
		this.resultsOutputArray=results;
		this.id=id;
		
		String[] splitPath = path.split("/");
		this.passageName=splitPath[splitPath.length-1];//put name of passage here

		this.passagePath = path;
		this.running = true;
	}

	public ArrayList<String> getWordList() {
		ArrayList<String> goodWords = new ArrayList<String>();
		String word;
		Scanner passage;

		try {
			passage = new Scanner(new File(this.passagePath));
		} catch (Exception e) {
			return goodWords;
		}

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

				boolean found = this.textTrieTree.contains(prefix);
				
				if (!found){
					//System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> not found ");
					resultsOutputArray.put(passageName+":"+prefix+" not found");
				} else{
					//System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> "+word);
					resultsOutputArray.put(passageName+":"+prefix+" found : "+this.textTrieTree.longestWithPrefix(prefix));
				}
				
			} catch(InterruptedException e){
				System.out.println(e.getMessage());
			}
		}
	}

	public void kill() {
		running = false;
	}

}
