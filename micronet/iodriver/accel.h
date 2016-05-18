
struct accel_thread_context
{
	char name[PATH_MAX];
};

void * accel_proc(void * cntx);
