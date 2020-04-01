package edu.cs300;

import java.io.*;
import java.util.Scanner;

public class TextSamples extends Thread {
	public static void main(String[] args) {
		// TextSamples ts = new TextSamples();
		// ts.start();
		PassageProcessor.ProcessPassages();
		// try {
		// 	ts.join();
		// } catch(Exception e) {}
			
		System.out.println("Terminating ...");
	}

	public void run() {
		int passageCount = 0;
		try {
			Scanner reader = new Scanner(new File("passages.txt"));
	
			for (;reader.hasNextLine();passageCount++)
				reader.nextLine();
			reader.close();
	
			Runtime runtime = Runtime.getRuntime();
			
			runtime.exec("./msgsnd con 1");
			for (int i=0;i<passageCount;i++)
				runtime.exec("./msgrcv");
	
			runtime.exec("./msgsnd pre 2");
			for (int i=0;i<passageCount;i++)
				runtime.exec("./msgrcv");
			
			runtime.exec("./msgsnd wor 3");
			for (int i=0;i<passageCount;i++)
				runtime.exec("./msgrcv");
		
			runtime.exec("./msgsnd no 0");
			for (int i=0;i<passageCount;i++)
				runtime.exec("./msgrcv");
		} catch(Exception e) {}
	}
}
