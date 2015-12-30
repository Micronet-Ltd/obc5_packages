#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include "QSEEComAPI.h"
#include "common_log.h"
#include <utils/Log.h>
#include "jni.h"
#include <android/log.h>

#define LOG_TAG "libsfs"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef enum {
	FILE_READ,
	FILE_WRITE,
	FILE_RM,
} iris_opt;

#define CLIENT_CMD1_BASIC_DATA  1

struct qsc_send_cmd {
     uint32_t cmd_id;
     uint32_t data;
     uint32_t data2;
     uint32_t len;
     uint32_t start_pkt;
     uint32_t end_pkt;
     uint32_t test_buf_size;
     char info[36]; 
     char buffer[512];
};

struct qsc_send_cmd_rsp {
  uint32_t data;
  int32_t status;
  char info[52];
  char buffer[512];
};


/*
int qrt_read(int file_id, char *buf);
int qrt_write(int file_id, char *buf);
int qrt_del(int file_id);
*/


int32_t sfs_start_app(struct QSEECom_handle **l_QSEEComHandle, 
                        const char *appname, int32_t buf_size)
{
	int32_t ret = 0;

	/* start the application */
	ret = QSEECom_start_app(l_QSEEComHandle, "/system/etc/firmware",
				appname, buf_size);
	if (ret) {
	   LOGE("Loading app -%s failed",appname);
	   printf("%s: Loading app -%s failed\n",__func__,appname);
	} else {
	   LOGD("Loading app -%s succeded",appname);
	}

	return ret;
}

int32_t qsc_shutdown_app(struct QSEECom_handle **l_QSEEComHandle)
{
	int32_t ret = 0;

	LOGD("qsc_shutdown_app: start");
	/* shutdown the application */
	if (*l_QSEEComHandle != NULL) {
	   ret = QSEECom_shutdown_app(l_QSEEComHandle);
	   if (ret) {
	      LOGE("Shutdown app failed with ret = %d", ret);
	      printf("%s: Shutdown app failed with ret = %d\n",__func__,ret);
	   } else {
	      LOGD("shutdown app: pass");
	   }
	} else {
		LOGE("cannot shutdown as the handle is NULL");
		printf("%s:cannot shutdown as the handle is NULL\n",__func__);
	}
	return ret;
}

int32_t sfs_send_cmd_req(int file_id, char *buf, iris_opt opt)
{
	int32_t ret = 0;
	int32_t req_len = 0;
	int32_t rsp_len = 0;
	int32_t count = 0;
	struct qsc_send_cmd *msgreq;	/* request data sent to QSEE */
	struct qsc_send_cmd_rsp *msgrsp;	/* response data sent from QSEE */
	struct QSEECom_handle *l_QSEEComHandle = NULL;
	struct qsc_send_cmd send_cmd;

	ret = sfs_start_app(&l_QSEEComHandle, "sample", 1024);
	if (ret) {
		LOGE("Start app: fail");
		printf("Start app: fail\n");
		return ret;
	}
	 
	 /* Send data using send command to QSEE application */
	send_cmd.cmd_id = CLIENT_CMD1_BASIC_DATA;
	send_cmd.start_pkt = 0;
	send_cmd.end_pkt = 0;
	send_cmd.test_buf_size = 0;

	LOGD("send sfs cmd: start");
	/* populate the data in shared buffer */
	msgreq=(struct qsc_send_cmd *)l_QSEEComHandle->ion_sbuffer;
	msgreq->cmd_id = send_cmd.cmd_id;
	msgreq->data = 100;
	req_len = sizeof(struct qsc_send_cmd);
	rsp_len = sizeof(struct qsc_send_cmd_rsp);

	if (req_len & QSEECOM_ALIGN_MASK)
		req_len = QSEECOM_ALIGN(req_len);

	if (rsp_len & QSEECOM_ALIGN_MASK)
		rsp_len = QSEECOM_ALIGN(rsp_len);

	msgrsp=(struct qsc_send_cmd_rsp *)l_QSEEComHandle->ion_sbuffer;

	LOGD("req data = %d",msgreq->data);
	printf("req data = %d\n",msgreq->data);
	LOGD("req len = %d bytes",req_len);
	printf("req len = %d bytes\n",req_len);
	LOGD("rsp len = %d bytes",rsp_len);
	printf("rsp len = %d bytes\n",rsp_len);

	if(opt == FILE_READ){
		msgreq->info[0] = file_id;  
		msgreq->info[1] = 'r';  // r for read
		/* send request from HLOS to QSEApp */
		for(count=0; count<2; count++){
			msgreq->info[2] = count; 
			msgreq->cmd_id = send_cmd.cmd_id;
 			ret = QSEECom_send_cmd(l_QSEEComHandle, msgreq, req_len, msgrsp, rsp_len);
			if (ret || (0 != msgrsp->status)) {
			   LOGE("send command failed with ret = %d\n", ret);
			   printf("%s: Send command failed with ret = %d\n",__func__,ret);
               qsc_shutdown_app(&l_QSEEComHandle);
               return -1;
			}
			LOGD("rsp data = %d", msgrsp->data);
			//printf("Howard	msgrsp->buffer %s\n", msgrsp->buffer);
			memcpy(buf+count*512, msgrsp->buffer, 512);
		}
	}
	else if(opt == FILE_WRITE){
		msgreq->info[0] = file_id;
		msgreq->info[1] = 'w';  // w for write
		for(count=0; count<2; count++){
			msgreq->info[2] = count; 
			msgreq->cmd_id = send_cmd.cmd_id;
			memcpy(msgreq->buffer, buf+count*512, 512);
 			ret = QSEECom_send_cmd(l_QSEEComHandle, msgreq, req_len, msgrsp, rsp_len);
			if (ret || (0 != msgrsp->status)) {
			   LOGE("send command failed with ret = %d\n", ret);
			   printf("%s: Send command failed with ret = %d\n",__func__,ret);
               qsc_shutdown_app(&l_QSEEComHandle);
               return -1;
			}
			LOGD("rsp data = %d", msgrsp->data);
			//printf("Howard	msgrsp->buffer %s\n", msgrsp->buffer);
		}
	}
	else if(opt == FILE_RM){
		msgreq->info[0] = file_id;
		msgreq->info[1] = 'm';  // m for rm
		ret = QSEECom_send_cmd(l_QSEEComHandle, msgreq, req_len, msgrsp, rsp_len);
		if (ret || (0 != msgrsp->status)) {
			LOGE("send command failed with ret = %d\n", ret);
			printf("%s: Send command failed with ret = %d\n",__func__,ret);
            qsc_shutdown_app(&l_QSEEComHandle);
            return -1;
		}
	}
	
	ret = qsc_shutdown_app(&l_QSEEComHandle);
	if (ret) {
		LOGE("Failed to shutdown app: %d",ret);
		printf("Failed to shutdown app: %d\n",ret);
	}
	return ret;

}

jbyteArray
Java_com_arccra_file_option_qrtread(JNIEnv* env,jobject thiz,int file_id)
{
	LOGI("read begin ");
	int len = 1024;
	char buf[1024]={0};
	int ret = 0;
	ret = sfs_send_cmd_req(file_id, buf, FILE_READ);
	//LOGI("read buf= %s",buf);
	if(ret){
		printf("qrt_read error %d\n", ret);
		return NULL;
	}
		
	jbyteArray jBuf = NULL;
	jsize arrsize = (jsize)len;
	jBuf = (*env)->NewByteArray(env,arrsize);	
	(*env)->SetByteArrayRegion(env,jBuf,0,len,(jbyte *)buf);
	return jBuf;
}

jint 
Java_com_arccra_file_option_qrtwrite(JNIEnv* env,jobject thiz,int file_id,jbyteArray byarray)
{    
	LOGI("write begin ");
	char* buf_iris = (char*)(*env)->GetByteArrayElements(env,byarray,NULL);
	char buf[1024]={0};

	memcpy(buf, buf_iris, 1024);
	//LOGI("write buf= %s",buf);
	int ret = 0;
	ret = sfs_send_cmd_req(file_id, buf, FILE_WRITE);
	if(ret)
		printf("qrt_write error %d\n", ret);
	LOGI("write end ");	
	return ret;
}

jint 
Java_com_arccra_file_option_qrtdel(JNIEnv* env,jobject thiz,int file_id)
{

	int ret = 0;
	char *buf = NULL;
	ret = sfs_send_cmd_req(file_id, buf, FILE_RM);
	if(ret)
		printf("qrt_write error %d\n", ret);
	return ret;

}
