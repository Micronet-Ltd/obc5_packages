/** Anthony Best <anthony.best@micronet-inc.com>
 *  Userspace driver for OBC MCU
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/select.h>
#include <libgen.h>
#include <linux/limits.h>

#include <limits.h>


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdbool.h>
#include <stdint.h>
#include <inttypes.h>

#include <pthread.h>
#include <sched.h>

#include <string.h>
#include <errno.h>


#include "log.h"
#include "misc.h"
#include "queue.h"
#include "frame.h"
#include "util.h"
#include "tty.h"
#include "control.h"
#include "accel.h"

/// structs and defines



int write_file(const char * filename, const char * str)
{
	int fd;
	int r;

	if(0 > (fd = open(filename, O_WRONLY)))
	{
		DERR("file='%s': %s", filename, strerror(errno));
		return -1;
	}
	r = write(fd, str, strlen(str));
	if(r < 0)
	{
		DERR("write: %s", strerror(errno));
	}
	close(fd);

	if(r < 0)
		return -1;

	return (unsigned)r == strlen(str) ? 0 : -1;
}

int write_file_int(const char * filename, int number)
{
	char str[12];

	snprintf(str, sizeof(str)-1, "%d", number);
	return	write_file(filename, str);
}

void set_otg(bool enable)
{
	// TODO: dont export if already exported
	if(!file_exists("/sys/class/gpio/gpio917/value"))
	{
		write_file_int("/sys/class/gpio/export", 917);
		sleep(1);
	}
	if(enable)
		write_file("/sys/class/gpio/gpio917/direction", "high");
	else
		write_file("/sys/class/gpio/gpio917/direction", "low");

	sleep(1);
}



/// main functions

void do_run()
{
	pthread_t control_thread;
	pthread_t accel_thread;

	struct control_thread_context controlctx /* = {0} */; 		// GCC bug #53119
	struct accel_thread_context accelctx /* = {0}*/; 	// GCC bug #53119

	memset(&controlctx, 0, sizeof(controlctx)); 				// GCC bug #53119
	memset(&accelctx, 0, sizeof(accelctx)); 			// GCC bug #53119

	// A8 rev C does switch is pulled high, this does nothing but add delay.
#if SWITCH_OTG
	DTRACE("Setting USB OTG Switch to FPC");
	set_otg(true);
	sleep(5); // wait for device...
#else
	DTRACE("Assuming OTG Switch is set in hardware");
#endif

	// TODO: the thread should dynamically detect the device name based on physical connection
	snprintf(controlctx.name, sizeof(controlctx.name)-1, "/dev/ttyACM0");
	snprintf(accelctx.name, sizeof(accelctx.name)-1, "/dev/ttyACM1");

	pthread_create(&control_thread, NULL, control_proc, &controlctx);
	pthread_create(&accel_thread, NULL, accel_proc, &accelctx);

    property_set("iodriver.boot_complete", "1");
    DINFO("%s: booot_complete\n", __func__);
	// TODO: main thread processing
	while(true) sleep(100);
}

int main(int argc __attribute__((unused)), char * argv[] __attribute__((unused)))
{
	pid_t pid;
	pid_t sid;
	bool daemonize = false;

	DTRACE("Enter main, daemonize = %s", (daemonize)?"Y":"N");

	if(daemonize)
	{
		pid = fork();

		if(pid > 0)
		{
			DINFO("iodriver pid = %d\n", pid);
			return EXIT_SUCCESS;
		}
		DINFO("iodriver running\n");

		sid = setsid();
		(void)sid; // avoid warning error
	}


	do_run();
	return EXIT_SUCCESS;
}

