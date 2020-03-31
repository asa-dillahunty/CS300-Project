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
response_buf getMessage();
void sighandler(int);

pthread_mutex_t lock;
char** prefixes;
int passageCount;
int prefixIndex;
int completedSearches;
int numPrefixes;

// Format:
// 	./searchmanager <secs between sending prefix requests> <prefix1> <prefix2> ...

int main(int argc, char** argv) {
	if (argc < 3) return 0;
	prefixes = argv;
	numPrefixes = argc;

	pthread_mutex_init(&lock, NULL);
	prefixIndex = 0;
	completedSearches = 0;

	signal(SIGINT, sighandler);

	int secs; // seconds between sending prefix requests

	int rc = fork();

	if (rc < 0) {
		fprintf(stderr, "fork failed\n");
		exit(1);
	} else if (rc == 0) {
		// child
		int status = system("make pp > PassageProcessor.log");
		// int status = system("make pp");
		return 0;
	}
	// parent

	secs = atoi(argv[1]);
	// printf("Parsed: %d, Original: %s\n",secs,argv[1]);
	response_buf* responses = NULL;
	response_buf response;

	int i;
	for (i=2;i<argc;i++) {
		// final_command[0] = '\0';
		// strcat(final_command,msg_send_command);
		// strcat(final_command,argv[i]);
		// int status = system(final_command);
		printf("\n");
		sendMessage(argv[i], i-1);
		printf("\n");
		// printf("%s\n",final_command);
		// wait secs
		sleep(secs);

		response = getMessage();

		pthread_mutex_lock(&lock);
		passageCount = response.count;
		completedSearches++;
		pthread_mutex_unlock(&lock);

		if (responses == NULL)
			responses = (response_buf*) malloc(sizeof(response_buf)*response.count);

		responses[response.index] = response;

		int j;
		for (j=1;j<response.count;j++) {
			response = getMessage();

			pthread_mutex_lock(&lock);
			completedSearches++;
			pthread_mutex_unlock(&lock);

			responses[response.index] = response;
		}

		pthread_mutex_lock(&lock);
		prefixIndex++;
		pthread_mutex_unlock(&lock);

		printf("Report \"%s\"\n",argv[i]);
		for (j=0;j<responses[0].count;j++) {

			// printf("%s\n",responses[j].longest_word);
			// memmove(responses[j].longest_word, responses[j].longest_word+1, strlen(responses[j].longest_word));
			if (responses[j].present == 1)
				printf("Passage %d - %s - %s\n", responses[j].index,responses[j].location_description,responses[j].longest_word);
			else
				printf("Passage %d - %s - not found\n", responses[j].index,responses[j].location_description);
		}
		// printf("%s\n",argv[i]);
	}
	free(responses);

	pthread_mutex_destroy(&lock);
	// char* msg = "no";
	// final_command[0] = '\0';
	// strcat(final_command,msg_send_command);
	// strcat(final_command,msg);
	// int status = system(final_command);
	// let passageProcessor know I am done
	printf("\n");
	sendMessage("   ",0);
	printf("\n");

	printf("Exiting ...\n");
	
	return 0;
}

void sendMessage(char* message, int prefixID) {
	int msqid;
	int msgflg = IPC_CREAT | 0666;
	key_t key;
	prefix_buf sbuf;
	size_t buf_length;

	key = ftok(CRIMSON_ID,QUEUE_NUMBER);
	if ((msqid = msgget(key, msgflg)) < 0) {
		int errnum = errno;
		fprintf(stderr, "Value of errno: %d\n", errno);
		perror("(msgget)");
		fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
	}
	// else
	// 	fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);

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
		printf("Message(%d): \"%s\" Sent (%d bytes)\n", sbuf.id, sbuf.prefix,(int)buf_length);
}

response_buf getMessage() {
	int msqid;
	int msgflg = IPC_CREAT | 0666;
	key_t key;
	response_buf rbuf;
	size_t buf_length;

	key = ftok(CRIMSON_ID,QUEUE_NUMBER);
	if ((msqid = msgget(key, msgflg)) < 0) {
		int errnum = errno;
		fprintf(stderr, "Value of errno: %d\n", errno);
		perror("(msgget)");
		fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
	}
	// else
	// 	fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);


	// msgrcv to receive message
	int ret;
	do {
		ret = msgrcv(msqid, &rbuf, sizeof(response_buf), 2, 0);//receive type 2 message
		int errnum = errno;
		if (ret < 0 && errno !=EINTR){
			fprintf(stderr, "Value of errno: %d\n", errno);
			perror("Error printed by perror");
			fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
		}
	} while ((ret < 0 ) && (errno == 4));
	//fprintf(stderr,"msgrcv error return code --%d:$d--",ret,errno);

	// if (rbuf.present == 1)
	// 	fprintf(stderr,"%ld, %d of %d, %s, size=%d\n", rbuf.mtype, rbuf.index,rbuf.count,rbuf.longest_word, ret);
	// else
	// 	fprintf(stderr,"%ld, %d of %d, not found, size=%d\n", rbuf.mtype, rbuf.index,rbuf.count, ret);

	return rbuf;
}

void sighandler(int x) {
	pthread_mutex_lock(&lock);
	int i;
	if (completedSearches == 0) {
		for (i=2;i<numPrefixes;i++) {
			printf("%s - pending\n",prefixes[i]);
		}
	}
	else {
		if (completedSearches/passageCount > i-2) // passed
			printf("%s - done\n",prefixes[i]);
		else if (completedSearches/passageCount == i-2) // current prefix
			printf("%s - %d of %d\n",prefixes[i],completedSearches%passageCount,passageCount);
		else
			printf("%s - pending\n"); // future
	}
	pthread_mutex_unlock(&lock);
	exit(1);
}
