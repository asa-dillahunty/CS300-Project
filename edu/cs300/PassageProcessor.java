package edu.cs300;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;

public class PassageProcessor {
	private static String PASSAGES_PATHS = "passages.txt";
	private static int BLOCK_QUEUE_SIZE = 10;

	public static void main(String[] args) {
		boolean printing = true;
		if (args.length > 0) printing = false;
		// int prefixCount = 0;

		ArrayList<Worker> workers = new ArrayList<Worker>();
		ArrayBlockingQueue<String> results = new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE);
		ArrayBlockingQueue<String> prefixQueue = new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE);
		// ArrayBlockingQueue prefixRequestArray;
		
		try {
			Scanner pass = new Scanner(new File(PASSAGES_PATHS));
			while (pass.hasNextLine()) {
				workers.add(new Worker(pass.nextLine(),workers.size(),new ArrayBlockingQueue<String>(BLOCK_QUEUE_SIZE), results));
				workers.get(workers.size()-1).start();
			}
			pass.close();
		}
		catch(Exception e){return;}

		PrefixManager pManager = new PrefixManager(prefixQueue, workers);
		pManager.start();
		
		
		// while prefixes exist
		// Somehow get a prefix
		while(true) {
			//get prefix from this statement
			SearchRequest message = MessageJNI.readPrefixRequestMsg();
			if (message == null) {
				System.out.println("null message");
				try {
					Thread.sleep(1);
				} catch (Exception e) {}
				break;
			}
			if (message.prefix.length() < 3) break;
			System.out.println("**prefix("+message.requestID+") "+message.prefix+" recieved");
			// **prefix(1) con received
			try {
				prefixQueue.put(message.prefix);
				// prefixCount++;
				// prefixQueue.put("con");
			} catch (Exception e) {}

			String msg;
			int worker_id;
			String longestWord;
			String[] msg_split;
			String[] split_again;
			for (int i=0;i<workers.size();i++)
				try {
					if (printing) {
						msg = results.take();

						msg_split = msg.split("-");
						split_again = msg_split[1].split(" ");
						worker_id = Integer.parseInt(split_again[0]);

						if (!msg.contains("not found")) {
							msg_split = msg.split("==> ");
							longestWord = msg_split[1];
							
							// System.out.println(msg+"\n"+"Worker ID: "+worker_id+"\nLongest Word: "+longestWord);
							// System.out.println(results.take());
							MessageJNI.writeLongestWordResponseMsg(workers.get(worker_id).prefixCount, message.prefix, worker_id, workers.get(worker_id).passageName, longestWord, workers.size(), 1);
						}
						else {
							MessageJNI.writeLongestWordResponseMsg(workers.get(worker_id).prefixCount, message.prefix, worker_id, workers.get(worker_id).passageName, "----", workers.size(), 0);
						}
						// MessageJNI.writeLongestWordResponseMsg(prefixID, prefix, passageIndex, passageName, longestWord, passageCount, present);
					}
					else {
						// send results
						// I'm not sure how
						results.take();
					}
				} catch (Exception e) {}
				break;

		}

		// after all the prefixes
		// kill the worker threads
		// this gives an invalid prefix, so the thread knows to stop
		prefixQueue.add("");

		for (Worker slave : workers) {
			try {
				slave.join();
			} catch (Exception e) {
				System.out.println("Worker("+slave.id+") failed to join.");
			}
		}
	}

	static class PrefixManager extends Thread {
		public ArrayBlockingQueue<String> prefixes;
		public ArrayList<Worker> workers;

		public PrefixManager(ArrayBlockingQueue<String> prefixes, ArrayList<Worker> workers) {
			this.prefixes = prefixes;
			this.workers = workers;
		}

		public void run() {
			String prefix = "*";
			
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
}
