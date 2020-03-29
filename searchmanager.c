#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
// Format:
// 	./searchmanager <secs between sending prefix requests> <prefix1> <prefix2> ...

int main(int argc, char** argv) {
	if (argc < 3) return 0;
	int secs; // seconds between sending prefix requests

	secs = atoi(argv[1]);
	printf("Parsed: %d, Original: %s\n",secs,argv[1]);
	char* msg_send_command = "./msgsnd ";
	char final_command[100];

	int i;
	for (i=2;i<argc;i++) {
		final_command[0] = '\0';
		strcat(final_command,msg_send_command);
		strcat(final_command,argv[i]);
		int status = system(final_command);
		// printf("%s\n",final_command);
		// wait secs
		sleep(secs);
		// printf("%s\n",argv[i]);
	}

	// let passageProcessor know I am done
	final_command[0] = '\0';
	strcat(final_command,msg_send_command);
	strcat(final_command,"*");
	int status = system(final_command);

	
	return 0;
}
