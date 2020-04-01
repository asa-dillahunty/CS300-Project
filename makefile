# Author: Asa
# This was such a waste of time
# @Dr. Anderson Herzog: This is just for me, I didn't do any wierd stuff with it
#	so you can just run it with the normal stuff

J_OBJS = edu/cs300/PassageProcessor.class edu/cs300/PrefixManager.class edu/cs300/Worker.class edu/cs300/ResultMessage.class edu/cs300/SearchRequest.class edu/cs300/MessageJNI.class CtCILibrary/Trie.class CtCILibrary/TrieNode.class edu/cs300/TextSamples.class edu_cs300_MessageJNI.h
C_OBJS = searchmanager msgsnd msgrcv libsystem5msg.so edu_cs300_MessageJNI.o
C_COMP = -std=c99 -Wall -D_GNU_SOURCE
SEM_COMP = -lpthread -lrt

all: $(J_OBJS) $(C_OBJS)


edu_cs300_MessageJNI.h: edu/cs300/MessageJNI.java
	javac -h . edu/cs300/MessageJNI.java

edu/cs300/PassageProcessor.class: edu/cs300/PassageProcessor.java edu/cs300/Worker.class edu_cs300_MessageJNI.h libsystem5msg.so 
	javac edu/cs300/PassageProcessor.java

edu/cs300/PrefixManager.class: edu/cs300/PassageProcessor.class

edu/cs300/Worker.class: edu/cs300/Worker.java
	javac edu/cs300/Worker.java

edu/cs300/ResultMessage.class: edu/cs300/Worker.class

edu/cs300/SearchRequest.class: edu/cs300/SearchRequest.java
	javac edu/cs300/SearchRequest.java
	
edu/cs300/TextSamples.class: edu/cs300/TextSamples.java msgrcv msgsnd
	javac edu/cs300/TextSamples.java

edu/cs300/MessageJNI.class: edu/cs300/MessageJNI.java 
	javac edu/cs300/MessageJNI.java

CtCILibrary/Trie.class: CtCILibrary/Trie.java CtCILibrary/TrieNode.java
	javac CtCILibrary/Trie.java

CtCILibrary/TrieNode.class: CtCILibrary/TrieNode.java
	javac CtCILibrary/TrieNode.java

searchmanager: searchmanager.c $(J_OBJS)
	gcc $(C_COMP) $(SEM_COMP) searchmanager.c -o searchmanager

msgsnd: msgsnd_pr.c
	gcc $(C_COMP) msgsnd_pr.c -o msgsnd

msgrcv: msgrcv_lwr.c
	gcc $(C_COMP) msgrcv_lwr.c -o msgrcv

libsystem5msg.so: edu_cs300_MessageJNI.o
	gcc -shared -o libsystem5msg.so edu_cs300_MessageJNI.o -lc

edu_cs300_MessageJNI.o: system5_msg.c edu_cs300_MessageJNI.h
	export JAVA_HOME=/usr/java/latest
	gcc -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux system5_msg.c -o edu_cs300_MessageJNI.o

pp: $(J_OBJS)
	java -cp . -Djava.library.path=. edu.cs300.PassageProcessor 2>/dev/null

ts: $(J_OBJS) 
	java -cp . -Djava.library.path=. edu.cs300.TextSamples 2>/dev/null

sendj: SendMessage.java
	javac SendMessage.java
	java -cp . -Djava.library.path=. SendMessage

test: all
	./searchmanager 2 con pre wor

testFast: all
	./searchmanager 0 con pre wor gib fig tree node mad had cat

clean:
	rm -f $(J_OBJS)
	rm -f $(C_OBJS)
	rm -f PassageProcessor.log

deepclean:
	rm -f $(J_OBJS)
	rm -f $(C_OBJS)
	rm -f PassageProcessor.log
	rm -f edu/cs300/*.class
	rm -f CtCILibrary/*.class
	rm -f *.class
	rm -f *.o
