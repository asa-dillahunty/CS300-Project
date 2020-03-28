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
		this.passageName="Passage-"+Integer.toString(id)+".txt";//put name of passage here
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

			goodWords.add(word);
		}

		return goodWords;
	}

	public void run() {
		System.out.println("Worker-"+this.id+" ("+this.passageName+") thread started ...");
		this.textTrieTree = new Trie(getWordList());

		while (running){
			try {
				String prefix=(String)this.prefixRequestArray.take();
				boolean found = this.textTrieTree.contains(prefix);
				
				if (!found){
					//System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> not found ");
					resultsOutputArray.put(passageName+":"+prefix+" not found");
				} else{
					//System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> "+word);
					resultsOutputArray.put(passageName+":"+prefix+" found");
				}
			} catch(InterruptedException e){
				System.out.println(e.getMessage());
			}
		}
	}

}
