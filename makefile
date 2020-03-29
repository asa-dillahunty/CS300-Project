J_OBJS = PassageProcessor.class Worker.class Trie.class TrieNode.class TextSamples.class edu_cs300_MessageJNI.h
C_OBJS = searchmanager msgsnd msgrcv

all: $(J_OBJS) $(C_OBJS)


edu_cs300_MessageJNI.h: edu/cs300/MessageJNI.java
	javac -h . edu/cs300/MessageJNI.java

PassageProcessor.class: edu/cs300/PassageProcessor.java edu/cs300/Worker.java
	javac edu/cs300/PassageProcessor.java

Worker.class: edu/cs300/Worker.java
	javac edu/cs300/Worker.java

TextSamples.class: edu/cs300/TextSamples.java
	javac edu/cs300/TextSamples.java

Trie.class: CtCILibrary/Trie.java CtCILibrary/TrieNode.java
	javac CtCILibrary/Trie.java

TrieNode.class: CtCILibrary/TrieNode.java
	javac CtCILibrary/TrieNode.java

searchmanager: searchmanager.c
	gcc -std=c99 -D_GNU_SOURCE searchmanager.c -o searchmanager

msgsnd: msgsnd_pr.c
	gcc -std=c99 -D_GNU_SOURCE msgsnd_pr.c -o msgsnd

msgrcv: msgrcv_lwr.c
	gcc -std=c99 -D_GNU_SOURCE msgrcv_lwr.c -o msgrcv

testp: all
	java -cp . -Djava.library.path=. edu.cs300.PassageProcessor

test: all
	./searchmanager 2 con pre wor
