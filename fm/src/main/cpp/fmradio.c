#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <linux/ioctl.h>
#include <android/log.h>
#include "fmradio_jni.h"

#include <fcntl.h> // for open
#include <unistd.h> // for close

#define USE_FM_NEW_DRIVER	1  //1 -- use new driver; 0 -- use old driver

#if USE_FM_NEW_DRIVER
#include "fm_main.h"
#include "fm_ioctl.h"
#include "fm_cust_cfg.h"
#else
#include "fmradio_ioctl.h"
#define	FMRADIO_DEVNAME		"/dev/RDAFM"
#endif

#define	FM_DEVICE_NAME		"/dev/RDAFM"


#define	audioPath_DEVNAME	\
"/sys/devices/platform/lm49350-user-interface/Fm_route_switch"

#define LOGTAG	"FMRADIO"
#define	LOGD(fmt, args...) \
	__android_log_print(ANDROID_LOG_DEBUG, LOGTAG, fmt, ## args)
#define	LOGE(fmt, args...) \
	__android_log_print(ANDROID_LOG_ERROR, LOGTAG, fmt, ## args)
#define LOGI(str) \
	__android_log_print(ANDROID_LOG_DEBUG, LOGTAG, \
		"%s: %d: %s", __func__, __LINE__, str)

struct FMRadio {
	int fmfd;
	int volume;
	int audioPath;
	int freq;
};
static struct FMRadio fmradio = {-1, 0, 0, 875};

static int openFmRadio() {
	int fd;

#if USE_FM_NEW_DRIVER
	fd = open(FM_DEVICE_NAME, O_RDWR);
	if(fd  < 0){ 
		LOGE("FAIL open %s failed\n", FM_DEVICE_NAME);
		return -1; 
	}
#else
	LOGD("Open: %s", FMRADIO_DEVNAME);
	if ((fd = open(FMRADIO_DEVNAME, O_RDONLY)) < 0) {
		LOGE("%s: %d: %s", FMRADIO_DEVNAME, errno, strerror(errno));
		return -1;
	}
#endif

	fmradio.fmfd = fd;
	LOGD("fmradio.fmfd = %d", fmradio.fmfd);
	return 0;
}

static int closeFmRadio() {
	LOGD("fmradio.fmfd = %d", fmradio.fmfd);
	if (fmradio.fmfd > 0) {
		LOGI("close fmradio.fmfd");
		close(fmradio.fmfd);
		fmradio.fmfd = -1;
	}
	return 0;
}

static int switchAudioPath(int audioPath) {
	int fd;
	int iAudioPath;
	char cAudioPath;

	LOGD("switchAudioPath: audioPath = %d", audioPath);
	if ((fd = open(audioPath_DEVNAME, O_RDWR)) < 0) {
		LOGE("open audio path failed: %d: %s", errno, strerror(errno));
		return -1;
	}

	iAudioPath = (audioPath & 0x3) % 3;
	cAudioPath = '0' + iAudioPath;
	LOGD("iAudioPath = %d, cAudioPath = %c", iAudioPath, cAudioPath);
	if (write(fd, &cAudioPath, 1) != 1) {
		LOGE("set audio path failed: %d: %s", errno, strerror(errno));
		close(fd);
		return -1;
	}

	if (iAudioPath) {
		fmradio.audioPath = iAudioPath;
	}
	close(fd);
	return 0;
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    turnOnRadioNative
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_turnOnRadioNative(JNIEnv *env, jobject thiz) {
#if USE_FM_NEW_DRIVER
	int ret;
	struct fm_tune_parm parm;

	if (openFmRadio() < 0) {
		return -1;
	}

	memset(&parm, 0, sizeof(struct fm_tune_parm));

	parm.band = FM_BAND_UE;
	parm.freq = fmradio.freq;
	parm.hilo = FM_AUTO_HILO_OFF;
	parm.space = FM_SPACE_100K;

	ret = ioctl(fmradio.fmfd, FM_IOCTL_POWERUP, &parm);
	if(ret){
		LOGE("FAIL:%d:%d\n", ret, parm.err);
		closeFmRadio();
		return -1;
	}
	return 0;
#else
	jint ret = 0;
	int turnOn = 1;

	LOGI("init fmradio");
	fmradio.fmfd = -1;
	
	if (openFmRadio() < 0) {
		return -1;
	}

	LOGI("ioctl set enable");
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_SET_ENABLE, &turnOn) < 0) {
		LOGE("set enable failed: %d: %s", errno, strerror(errno));
		close(fmradio.fmfd);
		fmradio.fmfd = -1;
		return -1;
	}

	LOGI("ioctl set enable ok");
	return ret;
#endif
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    turnOffRadioNative
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_turnOffRadioNative(JNIEnv *env, jobject thiz) {
#if USE_FM_NEW_DRIVER
	int ret;

	if(fmradio.fmfd < 0){ 
		LOGE("WARN fd unavailable\n");
		return -2;
	} 

	ret = ioctl(fmradio.fmfd, FM_IOCTL_POWERDOWN, NULL);
	if(ret){
		LOGE("FAIL:%d\n", ret);
		closeFmRadio();
		return -1;
	} 
	closeFmRadio();

	return 0;
#else
	jint ret = 0;
	int turnOff = 0;

	if (fmradio.fmfd < 0) {
		LOGD("fmradio.fmfd = %d", fmradio.fmfd);
		return 0;
	}

	LOGI("set disable");
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_SET_ENABLE, &turnOff) < 0) {
		// LOGE("set disable failed: %d: %s", errno, strerror(errno));
		LOGI("set disable failed");
		closeFmRadio();
		return -1;
	}
	closeFmRadio();

	LOGD("set disable ok");
	return ret;
#endif
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    tuneRadioNative
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_tuneRadioNative(JNIEnv *env, jobject thiz, jint freq) {
#if USE_FM_NEW_DRIVER
	int ret;
	struct fm_tune_parm parm;

	if(fmradio.fmfd < 0){
		LOGE("WARN fd unavailable\n");
		return -2;
	}

	memset(&parm, 0, sizeof(struct fm_tune_parm));

	parm.band = FM_BAND_UE;
	parm.freq = (freq / 10);
	parm.hilo = FM_AUTO_HILO_OFF;
	parm.space = FM_SPACE_100K;
	fmradio.freq = parm.freq;

	ret = ioctl(fmradio.fmfd, FM_IOCTL_TUNE, &parm);
	if(ret){
		LOGE("FAIL:%d:%d\n", ret, parm.err);
		closeFmRadio();
		return -1;
	}
	LOGE("OK:%d\n", parm.freq);
	return 0;
#else
	jint ret = 0;
	uint16_t sfreq = freq / 10;

	LOGD("tuneRadioNative fmfd = %d, freq = %d", fmradio.fmfd, sfreq);
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_SET_TUNE, &sfreq) < 0) {
		LOGE("set tune failed: %d: %s", errno, strerror(errno));
		return -1;
	}
	fmradio.freq = sfreq;
	LOGI("set tune ok");

	return ret;
#endif
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    getRadioIsOnNative
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_service_fm_FmReceiver_getRadioIsOnNative(JNIEnv *env, jobject thiz) {
	return fmradio.fmfd > 0;
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    muteAudioNative
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_muteAudioNative(JNIEnv *env, jobject thiz, jboolean mute) {
#if USE_FM_NEW_DRIVER
	int ret;

	if(fmradio.fmfd < 0){ 
		LOGE("WARN fd unavailable\n");
		return -2; 
	}   

	ret = ioctl(fmradio.fmfd, FM_IOCTL_MUTE, &mute);
	if(ret){
		LOGE("FAIL:%d\n", ret);
		closeFmRadio();
		return -1; 
	}   

	return 0;
#endif
	LOGD("muteAudioNative: mute = %d", mute ? 1 : 0);
	return switchAudioPath(mute ? 0 : fmradio.audioPath);
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    seekStationNative
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_seekStationNative(JNIEnv *env, jobject thiz, jint scanMode) {
#if USE_FM_NEW_DRIVER
	   int ret;
	   struct fm_seek_parm parm;

	   if (fmradio.fmfd < 0){
		   LOGE("WARN fd unavailable\n");
		   return -2;
	   }
	   memset(&parm, 0, sizeof(struct fm_seek_parm));

	   if(scanMode){
		   parm.seekdir = FM_SEEK_UP;
	   }else{
		   parm.seekdir = FM_SEEK_DOWN;
	   }

	   parm.band = FM_BAND_UE;
	   parm.freq = fmradio.freq;
	   parm.hilo = FM_AUTO_HILO_OFF;
	   parm.space = FM_SPACE_100K;
	   parm.seekth = FM_SEEKTH_LEVEL_DEFAULT;
	   ret = ioctl(fmradio.fmfd, FM_IOCTL_SEEK, &parm);

#if 0
	   if(ret){
		   LOGE("FAIL:%d:%d\n", ret, parm.err);
		   closeFmRadio();
		   return -1;
	   }
#endif
        if (ret < 0)
            LOGE("RDAFM: can't find a station\n");
        else
            printf("RDAFM: find a station: %d\n",parm.freq);
	   fmradio.freq = parm.freq;

	   return (10 * parm.freq);
#else
	jint ret = -1;
	int args[4];

	args[0] = fmradio.freq;	// start freq
	args[1] = scanMode;	// direction
	args[2] = 1000;		// timeout
	args[3] = 0;		// return freq

	LOGD("seekStation fmradio.fmfd = %d, startfreq = %d, direction= %d, timeout = %d", \
		fmradio.fmfd, args[0], args[1], args[2]);
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_SEARCH, args) < 0) {
		LOGE("search failed: %d: %s", errno, strerror(errno));
		return -1;
	}

	LOGD("search ok: freq = %d", args[3]);
	return args[3] * 10;
#endif
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    seekStationAbortNative
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_seekStationAbortNative(JNIEnv *env, jobject thiz) {
#if USE_FM_NEW_DRIVER
	return 0;
#else
	jint ret = 0;

	LOGD("seekStationAbort fmradio.fmfd = %d", fmradio.fmfd);
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_STOP_SEARCH) < 0) {
		LOGE(" stop search failed: %d: %s", errno, strerror(errno));
		return -1;
	}

	LOGD("stop search ok");
	return ret;
#endif
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    setAudioPathNative
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_setAudioPathNative(JNIEnv *env, jobject thiz, jint audioPath) {
	return switchAudioPath(audioPath);
}

/*
 * Class:     com_service_fm_FmReceiver
 * Method:    setFMVolumeNative
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_service_fm_FmReceiver_setFMVolumeNative(JNIEnv *env, jobject thiz, jint volume) {
#if USE_FM_NEW_DRIVER
	int ret;

	if(fmradio.fmfd < 0){
		printf("WARN fd unavailable\n");
		return -2;
	}

	ret = ioctl(fmradio.fmfd, FM_IOCTL_SETVOL, &volume);
	if(ret){
		printf("FAIL:%d\n", ret);
		closeFmRadio();
		return -1;
	}

	return 0;
#else
	jint ret = 0;

	LOGD("setFMVolumeNative fmradio.fmfd = %d, volume = %d", fmradio.fmfd, volume);
	if (ioctl(fmradio.fmfd, RDAFM_IOCTL_SET_VOLUME, &volume) < 0) {
		LOGE(" set volume failed: %d: %s", errno, strerror(errno));
		return -1;
	}

	fmradio.volume = volume;
	LOGD("set volume ok");
	return ret;
#endif
}

