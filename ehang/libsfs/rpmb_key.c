#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include "QSEEComAPI.h"
#include "common_log.h"
#include <utils/Log.h>

int main() {
	int ret = 0;
	struct qseecom_rpmb_provision_key send_buf = {0};

	printf("\t-------------------------------------------------------\n");
	printf("\t WARNING!!! You are about to provision the RPMB key.\n");
	printf("\t This is a ONE time operation and CANNOT be reversed.\n");
	printf("\t-------------------------------------------------------\n");

	send_buf.key_type = 1;   // 1 for Test key

	ret = QSEECom_send_service_cmd((void*) &send_buf, sizeof(send_buf),
					NULL, 0, QSEECOM_RPMB_PROVISION_KEY_COMMAND);
	if(!ret)
		printf("RPMB key provisioning completed\n");
	else
		printf("RPMB key provisioning failed (%d)\n", ret);

	return ret;
}