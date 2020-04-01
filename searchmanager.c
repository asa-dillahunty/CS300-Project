#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <signal.h>
#include <semaphore.h>
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

int validPrefix(char* prefix);
char** getValidPrefixes(char** argv, int* argc);
void sendMessage(char* message, int prefixID);
response_buf getMessage();
void sighandler(int);

char** prefixes;
int numPrefixes;

sem_t validPrefixIndex;
sem_t searchesCompleted;
sem_t count_of_passages;
// Format:
// 	./searchmanager <secs between sending prefix requests> <prefix1> <prefix2> ...

int main(int argc, char** argv) {

	if (argc < 3) {
		fprintf(stderr,"No valid prefixes.\n");
		return 0;
	}
	int i;
	sem_init(&validPrefixIndex,0,0);
	sem_init(&searchesCompleted,0,0);

	prefixes = argv;
	numPrefixes = argc;
	signal(SIGINT, sighandler);
	
	argv = getValidPrefixes(prefixes,&argc);

	
	
	// int newSize = argc;
	// char** new_argv = getValidPrefixes(argv,&newSize);


	/**
	 * Here is where. Idk anymore just kill me
	 * I'm tired of this fucking project
	 */
	// prefixes = argv;
	// numPrefixes = argc;


	int secs; // seconds between sending prefix requests

	// int rc = fork();
	int rc = 1;

	if (rc < 0) {
		fprintf(stderr, "fork failed\n");
		exit(1);
	} else if (rc == 0) {
		// child
		int status = system("make pp > PassageProcessor.log");
		// int status = system("make pp");
		if (status == -1) // I don't know what status is
			return 0;
		else
			return 0;
	}
	// parent

	secs = atoi(argv[1]);
	// printf("Parsed: %d, Original: %s\n",secs,argv[1]);
	response_buf* responses = NULL;
	response_buf response;

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
		sem_post(&searchesCompleted);

		if (responses == NULL) {
			sem_init(&count_of_passages,0,response.count);
			responses = (response_buf*) malloc(sizeof(response_buf)*response.count);
		}

		responses[response.index] = response;

		int j;
		for (j=1;j<response.count;j++) {
			response = getMessage();
			sem_post(&searchesCompleted);
			responses[response.index] = response;
		}

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
	free(argv);
	// free(new_argv);

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

int validPrefix(char* prefix) {
	int length = strlen(prefix);
	if (length < 3 || length > 20) return 0;
	
	char care;
	for (int i=0;i<length;i++) {
		care = prefix[i];
		if ((care >= 'A' && care <= 'Z') || (care >= 'a' && care <= 'z')) continue;
		else return 0;
	}
	return 1;
}

char** getValidPrefixes(char** argv, int* argc) {
	
	int validPrefixes = 0;
	int i;
	for (i=2;i<(*argc);i++)
		if (validPrefix(argv[i]) == 1)
 			validPrefixes++;

	if (validPrefixes == 0) {
		fprintf(stderr,"No valid prefixes.\n");
		return 0;
	}

	char** new_argv = (char**) malloc(sizeof(char*)*(validPrefixes+2));
	new_argv[0] = argv[0];
	new_argv[1] = argv[1];

	validPrefixes = 0;
	for (i=2;i<(*argc);i++) {
		if (validPrefix(argv[i]) == 1) {
			validPrefixes++;
			new_argv[1+validPrefixes] = argv[i];
		}
	}

	*argc = validPrefixes+2;
	return new_argv;
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
	// size_t buf_length;

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

	int completedSearches;
	int passageCount;
	sem_getvalue(&searchesCompleted,&completedSearches);
	sem_getvalue(&count_of_passages,&passageCount);

	int argc = numPrefixes;
	char** argv = getValidPrefixes(prefixes,&argc);

	int i;
	if (completedSearches == 0)
		for (i=2;i<argc;i++)
			printf("%s - pending\n",argv[i]);
	else
		for (i=2;i<argc;i++)
			if (completedSearches/passageCount > i-2) // passed
				printf("%s - done\n",argv[i]);
			else if (completedSearches/passageCount == i-2 && completedSearches%passageCount != 0) // current prefix
				printf("%s - %d of %d\n",argv[i],completedSearches%passageCount,passageCount);
			else
				printf("%s - pending\n",argv[i]); // future
	free(argv);
	
}
