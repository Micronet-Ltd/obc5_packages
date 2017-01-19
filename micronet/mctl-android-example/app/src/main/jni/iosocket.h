
// See TLPI 57.6 The Linux Abstract Socket Namespace
// Use abstract socket namespace, set # to '\0' before bind
#define UD_FILENAME "#micronet_control"


#define SOCK_MAX_MSG 4096

int iosocket_connect();
void iosocket_disconnect(int * fd);
int iosocket_sendmsg(int * fd, uint8_t * data, size_t len);
int iosocket_recvmsg(int * fd, uint8_t * data, size_t len);

