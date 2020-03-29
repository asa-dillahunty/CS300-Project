all:
	javac edu/cs300/*.java
	javac CtCILibrary/*.java
	javac -h . edu/cs300/MessageJNI.java
	gcc -o searchmanager searchmanager.c

testp: all
	java -cp . -Djava.library.path=. edu.cs300.PassageProcessor

test: all
	./searchmanager 2 con pre wor
