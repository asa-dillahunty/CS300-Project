package edu.cs300;

import java.util.*;
import java.io.*;

public class PassageProcessor {
	static String passagePath = "../../passages.txt";

	public static void main(String[] args) {
		System.out.println("Hello World");
		
		try {
			Scanner pass = new Scanner(new File(passagePath));
			while (pass.hasNextLine())
				System.out.println(pass.nextLine());
		}
		catch(Exception e){}
	}
}
