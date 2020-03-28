#include <stdio.h>
#include <stdlib.h>
// Format:
// 	./searchmanager <secs between sending prefix requests> <prefix1> <prefix2> ...

int main(int argc, char** argv) {
	if (argc < 3) return 0;
	int secs; // seconds between sending prefix requests

	secs = atoi(argv[1]);
	printf("Parsed: %d, Original: %s\n",secs,argv[1]);

	int i;
	for (i=2;i<argc;i++) {
		// send prefix
		// wait secs
		printf("%s\n",argv[i]);
	}

	
	return 0;
}
