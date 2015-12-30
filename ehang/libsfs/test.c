#include "libsfs.h"

void main(int argc, char *argv[])
{
  int fd = 0;
  int ret = 0;
  char buf[768] ={0};
  int count = 0;

  printf("sfs_test %c, %d\n", argv[1][0], atoi(argv[2]));
  if('r' == argv[1][0])
  {
  	ret = qrt_read((int)atoi(argv[2]), buf);
    printf("qrt_read return %d\n", ret);
	fd= open("/data/read_result.txt", O_CREAT | O_RDWR | O_SYNC, 777);
	write(fd, buf, 768);
	close(fd);
  }
  else if('w' == argv[1][0])
  {
    fd= open("/data/test.txt", O_CREAT | O_RDWR | O_SYNC, 777);
    count = read(fd, buf, 768);
    ret = qrt_write((int)atoi(argv[2]), buf);
	printf("qrt_write return %d \n", ret);
	close(fd);
  }
  else if('m' == argv[1][0])
  {
    ret = qrt_del( (int)atoi(argv[2]));
    printf("qrt_del return %d \n", ret);
  }

}