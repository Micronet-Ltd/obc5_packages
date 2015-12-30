#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include<stdlib.h>

int qrt_read(int file_id, char *buf);
int qrt_write(int file_id, char *buf);
int qrt_del(int file_id);

