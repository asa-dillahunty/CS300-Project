#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include "longest_word_search.h"
#include "queue_ids.h"

#ifndef mac
size_t                  /* O - Length of string */
strlcpy(char       *dst,        /* O - Destination string */
		const char *src,      /* I - Source string */
		size_t      size)     /* I - Size of destination string buffer */
{
	size_t srclen;         /* Length of source string */


	/*
	 * Figure out how much room is needed...
	 */

	size --;

	srclen = strlen(src);

	/*
	 * Copy the appropriate amount...
	 */
	if (srclen > size)
		srclen = size;

	memcpy(dst, src, srclen);
	dst[srclen] = '\0';

	return (srclen);
}
#endif

void sendMessage(char* message, int prefixID);

// Format:
// 	./searchmanager <secs between sending prefix requests> <prefix1> <prefix2> ...

int main(int argc, char** argv) {
	if (argc < 3) return 0;
	int secs; // seconds between sending prefix requests

	int rc = fork();

	if (rc < 0) {
		fprintf(stderr, "fork failed\n");
		exit(1);
	} else if (rc == 0) {
		// child
		// int status = system("java -cp . -Djava.library.path=. edu.cs300.PassageProcessor");
		return 0;
	}
	// parent

	secs = atoi(argv[1]);
	// printf("Parsed: %d, Original: %s\n",secs,argv[1]);
	char* msg_send_command = "./msgsnd ";
	char final_command[100];


	int i;
	for (i=2;i<argc;i++) {
		// final_command[0] = '\0';
		// strcat(final_command,msg_send_command);
		// strcat(final_command,argv[i]);
		// int status = system(final_command);
		sendMessage(argv[i], i-1);
		// printf("%s\n",final_command);
		// wait secs
		sleep(secs);

		// printf("%s\n",argv[i]);
	}
	// char* msg = "no";
	// final_command[0] = '\0';
	// strcat(final_command,msg_send_command);
	// strcat(final_command,msg);
	// int status = system(final_command);
	// let passageProcessor know I am done
	sendMessage("no",0);
	
	return 0;
}

void sendMessage(char* message, int prefixID) {
	int msqid;
	int msgflg = IPC_CREAT | 0666;
	key_t key;
	prefix_buf sbuf;
	size_t buf_length;

	if (strlen(message) <2) {
		printf("Error: please provide prefix of at least two characters for search\n");
		return;
	}

	key = ftok(CRIMSON_ID,QUEUE_NUMBER);
	if ((msqid = msgget(key, msgflg)) < 0) {
		int errnum = errno;
		fprintf(stderr, "Value of errno: %d\n", errno);
		perror("(msgget)");
		fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
	}
	else
		fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);

	// We'll send message type 1
	sbuf.mtype = 1;
	strlcpy(sbuf.prefix,message,WORD_LENGTH);
	sbuf.id= prefixID;
	buf_length = strlen(sbuf.prefix) + sizeof(int)+1;//struct size without long int type

	// Send a message.
	if((msgsnd(msqid, &sbuf, buf_length, IPC_NOWAIT)) < 0) {
		int errnum = errno;
		fprintf(stderr,"%d, %ld, %s, %d\n", msqid, sbuf.mtype, sbuf.prefix, (int)buf_length);
		perror("(msgsnd)");
		fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
		exit(1);
	}
	else
		fprintf(stderr,"Message(%d): \"%s\" Sent (%d bytes)\n", sbuf.id, sbuf.prefix,(int)buf_length);
}
