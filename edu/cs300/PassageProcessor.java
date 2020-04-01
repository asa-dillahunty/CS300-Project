/**
 * @Author: Asa Dillahunty
 * 
 * This class sends and receives messsages
 * over an IPC queue. The messages received 
 * contain "prefixes," which this program takes
 * and searches txt files for their longest word.
 */

package edu.cs300;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;

public class PassageProcessor {
	private static String PASSAGES_PATHS = "passages.txt"; // This text file contains the paths of all the text files to be searched
	private static int BLOCK_QUEUE_SIZE = 10;

	/**
	 * This static method processes passages stored in the text file "passages.txt"
	 * What it means to process is to read the passage, accept a prefix, and return the longest 
	 * word with that prefix
	 * 
	 * This method communicates over IPCS queues so that it can be fed prefixes from another 
	 * program (searchmanager.c)
	 */
	public static void ProcessPassages() {
		ArrayList<Worker> workers = new ArrayList<Worker>();
		ArrayBlockingQueue<ResultMessage> results = new ArrayBlockingQueue<ResultMessage>(BLOCK_QUEUE_SIZE);
		ArrayBlockingQueue<String> prefixQueue = new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE);
		// ArrayBlockingQueue prefixRequestArray;
		
		try {
			Scanner pass = new Scanner(new File(PASSAGES_PATHS));
			String passPath;
			File passage;
			while (pass.hasNextLine()) {
				passPath = pass.nextLine();
				try {
					passage = new File(passPath);
					if (!passage.exists()) {
						System.err.println("File - \""+passPath+"\" does not exist.s");
						continue;
					}
				} catch (NullPointerException e) {
					System.err.println(e.getMessage());
					continue;
				}
				workers.add(new Worker(passPath,workers.size(),new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE), results));
				workers.get(workers.size()-1).start();
			}
			pass.close();
			if (workers.size() == 0) {
				System.err.println("No valid passages to process.");
				return;
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
			return;
		}

		// resposnible for handling prefix distribution
		PrefixManager pManager = new PrefixManager(prefixQueue, workers);
		pManager.start();
		
		
		// while prefixes exist
		while(true) {
			//get prefix from this statement
			SearchRequest message = MessageJNI.readPrefixRequestMsg();
			System.out.println("**prefix("+message.requestID+") "+message.prefix+" recieved");

			// Kills the process if invalid prefix
			// **prefix(1) con received

			if (message.prefix.length() < 3 || message.prefix.compareTo("   ") == 0) break;
			try {
				prefixQueue.put(message.prefix);
			} catch (Exception e) {
				System.err.println("Error putting: \""+message.prefix+"\" on the Prefix Queue");
			}

			ResultMessage msg;
			for (int i=0;i<workers.size();i++)
				try {
					msg = results.take();

					// Message format: 
					// MessageJNI.writeLongestWordResponseMsg(prefixID, prefix, passageIndex, passageName, longestWord, passageCount, present);
					if (msg.found) {
						MessageJNI.writeLongestWordResponseMsg(message.requestID, message.prefix, msg.worker_id, workers.get(msg.worker_id).passageName, msg.longestWord, workers.size(), 1);
					}
					else {
						MessageJNI.writeLongestWordResponseMsg(message.requestID, message.prefix, msg.worker_id, workers.get(msg.worker_id).passageName, "----", workers.size(), 0);
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
		}

		// after all the prefixes
		// kill the worker threads
		// this gives an invalid prefix, so the thread knows to stop
		prefixQueue.add("");

		for (Worker slave : workers) {
			try {
				slave.join();
			} catch (Exception e) {
				System.err.println("Worker("+slave.id+") failed to join.");
			}
		}
	}

	public static void main(String[] args) {
		ProcessPassages();
	}
}

/**
 * This class was written to handle the distribution of prefixes to all of the workers
 * in the Process Passages function written above
 */
class PrefixManager extends Thread {
	private ArrayBlockingQueue<String> prefixes;
	private ArrayList<Worker> workers;

	public PrefixManager(ArrayBlockingQueue<String> prefixes, ArrayList<Worker> workers) {
		this.prefixes = prefixes;
		this.workers = workers;
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
			slave.kill();
			slave.prefixRequestArray.add("");
		}
	}
}
