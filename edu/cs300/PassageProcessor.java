package edu.cs300;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.*;

public class PassageProcessor {
	private static String PASSAGES_PATHS = "../../passages.txt";
	private static int BLOCK_QUEUE_SIZE = 10;

	public static void main(String[] args) {
		ArrayList<String> paths = new ArrayList<String>();
		// ArrayBlockingQueue prefixRequestArray;
		
		try {
			Scanner pass = new Scanner(new File(PASSAGES_PATHS));
			while (pass.hasNextLine())
				paths.add(pass.nextLine());
		}
		catch(Exception e){return;}

		Worker[] workers = new Worker[paths.size()];
		for (int i=0;i<paths.size();i++) {
			workers[i] = new Worker(paths.get(i), i+1, new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE),new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE));
			workers[i].start();
		}

		// while prefixes exist
		// Somehow get a prefix
		String prefix = "con";
		for (Worker slave : workers) {
			try {
				slave.prefixRequestArray.put(prefix);
			} catch (Exception e) {System.out.println("Cannot put prefix \"" + prefix + "\" in worker("+slave.id+")'s array");}
		}

		// after all the prefixes
		// kill the worker threads
		for (Worker slave : workers) {
			slave.running = false;
		}
		
	}
}
