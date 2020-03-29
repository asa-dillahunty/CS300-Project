package edu.cs300;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;

public class PassageProcessor {
	private static String PASSAGES_PATHS = "passages.txt";
	private static int BLOCK_QUEUE_SIZE = 10;

	public static void main(String[] args) {
		ArrayList<String> paths = new ArrayList<String>();
		// ArrayBlockingQueue prefixRequestArray;
		
		try {
			Scanner pass = new Scanner(new File(PASSAGES_PATHS));
			while (pass.hasNextLine())
				paths.add(pass.nextLine());
			pass.close();
		}
		catch(Exception e){return;}

		ArrayBlockingQueue<String> results = new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE);

		Worker[] workers = new Worker[paths.size()];
		for (int i=0;i<paths.size();i++) {
			workers[i] = new Worker(paths.get(i), i, new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE),results);
			workers[i].start();
		}

		// while prefixes exist
		// Somehow get a prefix
		String prefix = "wor";
		for (Worker slave : workers) {
			try {
				slave.prefixRequestArray.put(prefix);
			} catch (Exception e) {System.out.println("Cannot put prefix \"" + prefix + "\" in worker("+slave.id+")'s array");}
		}


		
		// boolean finished = false;
		// while (!finished) {
		// 	finished = true;
		// 	for (Worker slave : workers) {
		// 		if (slave.resultsOutputArray.isEmpty()) {
		// 			finished = false;
		// 			break;
		// 		}
		// 	}
		// }

		for (int i=0;i<workers.length;i++)
			try {
				System.out.println(results.take());
			} catch (Exception e) {}

		// for (Worker slave : workers) {
		// 	try {
		// 		System.out.println("Worker("+slave.id+"): "+slave.resultsOutputArray.take());
		// 	} catch (Exception e) {
		// 		System.out.println("Worker("+slave.id+"): Threw an error.");
		// 	}
		// }

		// after all the prefixes
		// kill the worker threads
		for (Worker slave : workers) {
			slave.kill();

			// this gives an invalid prefix, so the thread knows to stop
			slave.prefixRequestArray.add("");
			// slave.stop();
		}

		for (Worker slave : workers) {
			try {
				slave.join();
			} catch (Exception e) {
				System.out.println("Worker("+slave.id+") failed to join.");
			}
		}
	}

	class PrefixManager extends Thread {
		public ArrayBlockingQueue<String> prefixes;
		public ArrayList<Worker> workers;

		public PrefixManager(ArrayBlockingQueue<String> prefixes, ArrayList<Worker> workers) {
			this.prefixes = prefixes;
		}

		public void run() {
			String prefix;
			
			while (true) {
				try {
					prefix = prefixes.take(); 
				} catch (Exception e) {break;}

				if (prefix.length() < 3) break;

				for (Worker slave : workers) {
					slave.prefixRequestArray.add(prefix);
				}
			}
			
			for (Worker slave : workers) {
				slave.prefixRequestArray.add("");
			}
		}
	}
}
