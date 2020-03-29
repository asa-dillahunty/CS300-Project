J_OBJS = edu/cs300/PassageProcessor.class edu/cs300/Worker.class CtCILibrary/Trie.class CtCILibrary/TrieNode.class edu/cs300/TextSamples.class edu_cs300_MessageJNI.h
C_OBJS = searchmanager msgsnd msgrcv

all: $(J_OBJS) $(C_OBJS)


edu_cs300_MessageJNI.h: edu/cs300/MessageJNI.java
	javac -h . edu/cs300/MessageJNI.java

edu/cs300/PassageProcessor.class: edu/cs300/PassageProcessor.java edu/cs300/Worker.java
	javac edu/cs300/PassageProcessor.java

edu/cs300/Worker.class: edu/cs300/Worker.java
	javac edu/cs300/Worker.java

edu/cs300/TextSamples.class: edu/cs300/TextSamples.java
	javac edu/cs300/TextSamples.java

CtCILibrary/Trie.class: CtCILibrary/Trie.java CtCILibrary/TrieNode.java
	javac CtCILibrary/Trie.java

CtCILibrary/TrieNode.class: CtCILibrary/TrieNode.java
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
