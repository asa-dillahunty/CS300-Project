test:
	javac edu/cs300/*.java
	javac CtCILibrary/*.java
	javac -h . edu/cs300/MessageJNI.java

	java -cp . -Djava.library.path=. edu.cs300.PassageProcessor