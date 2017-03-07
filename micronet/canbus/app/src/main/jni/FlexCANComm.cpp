//
// Created by eemaan.siddiqi on 3/3/2017.
//

#include <unistd.h>
/*#include "FlexCANcomm.h"*/
#include "can.h"
//#include "canbus.cpp"
extern int fd;
int serial_send_data(BYTE *mydata, DWORD bytes_to_write)
{
    DWORD numwr = 0;

    numwr = write(fd, mydata, bytes_to_write);
    //TODO: this may not be an error
    if( numwr != bytes_to_write ){
        return -1;
    }
    return 0;
}