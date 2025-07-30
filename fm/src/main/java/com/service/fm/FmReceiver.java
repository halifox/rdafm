package com.service.fm;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.content.Context;

/**
 * FmReceiver is the Java API entry point to issue commands to FM receiver
 * hardware. After a command is issued one or more FmReceiverEvents will be
 * issued to the requested client application handler. The application must
 * implement the {@link IFmReceiverEventHandler} interface to receive the
 * results of requested operations.
 * <p>
 * PUBLIC FM RECEIVER API
 * <p>
 * An FmReceiver object acts as a proxy to the FmReceiverService.
 * <p>
 * Usage:
 * <p>
 * First create a reference to the FM receiver system service:
 * <p>
 * <code> FmReceiver mFmReceiver = (FmReceiver) getSystemService(Context.FM_RECEIVER_SERVICE); </code>
 * <p>
 * Then register as an event handler:
 * <p>
 * <code> mFmReceiver.registerEventHandler(this); </code>
 * <p>
 * The application should then call the turnOnRadio() function and wait for a
 * confirmation status event before calling further functions.
 * <p>
 * On closing the high level application, turnOffRadio() should be called to
 * disconnect from the FmReceiverService. A confirmation status event should be
 * received before the high level application is terminated.
 * <p>
 * This class first acquires an interface to the FmReceiverService module using
 * the ServiceManager (Binder is used here). This allows the FmReceiver instance
 * to act as a proxy to the FmReceiverService through which all FM Receiver
 * related commands are relayed. The FmReceiverService answers the FmReceiver
 * instance by issuing FmReceiverServiceEvents to the FmReceiver instance. (In
 * practice using multiple synchronized callback functions.)
 */
public final class FmReceiver {
	
	static {
		try {
		System.loadLibrary("rdafmradio");
		} catch(Exception e) {
			Log.e("FMRADIO", "load librdafmradio", e);

		}
	}

    private static final String TAG = "FmReceiver";
    /**
     * Name of this service
     */
    public static final String SERVICE_NAME = "bluetooth_fm_receiver_service";

    public static final String FM_RECEIVER_PERM = "android.permission.ACCESS_FM_RECEIVER";

    /**
     * Event Actions
     */

    /**
     * @hide
     */
    private static final String ACTION_PREFIX = "com.app.fm.action.";

    private static final int ACTION_PREFIX_LENGTH = ACTION_PREFIX.length();

	/**
	 * The default priority for a Broadcast Receiver created by the
	 * proxy object, if Broadcast Intents are used to for events and
	 * a receiver priority is not specified.
	 */
	public static final int DEFAULT_BROADCAST_RECEIVER_PRIORITY = 200;
	
    /**
     * Broadcast Intent action for fm status event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RADIO_ON}</th>
     * <td>boolean</td>
     * <td>If true, the FM Receiver is on.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_MUTED}</th>
     * <td>boolean</td>
     * <td>If true, the FM Receiver is muted.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_FREQ}</th>
     * <td>int</td>
     * <td>FM Frequency</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RSSI}</th>
     * <td>int</td>
     * <td>Received signal strength indicator.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_PRGM_TYPE}</th>
     * <td>int</td>
     * <td>The RDS program type.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_PRGM_TYPE_NAME}</th>
     * <td>String</td>
     * <td>The RDS program name.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_PRGM_SVC}</th>
     * <td>String</td>
     * <td>The RDS program service.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_TXT}</th>
     * <td>String</td>
     * <td>The RDS program text.</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_STATUS = ACTION_PREFIX + "ON_STATUS";

    /**
     * Broadcast Intent action for fm seek complete event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <th>{@link #EXTRA_FREQ}</th>
     * <td>int</td>
     * <td>FM Frequency</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RSSI}</th>
     * <td>int</td>
     * <td>Received signal strength indicator.</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_SUCCESS}</th>
     * <td>boolean</td>
     * <td>If true, the seek was successful.</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_SEEK_CMPL = ACTION_PREFIX + "ON_SEEK_CMPL";

    /**
     * Broadcast Intent action for RDS modes event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_MODE}</th>
     * <td>int</td>
     * <td>The RDS mode</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_ALT_FREQ_MODE}</th>
     * <td>int</td>
     * <td>The alternative frequency mode.</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_RDS_MODE = ACTION_PREFIX + "ON_RDS_MODE";

    /**
     * Broadcast Intent action for fm rds data event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <th>{@link #EXTRA_RDS_IDX}</th>
     * <td>int</td>
     * <td>RDS index</td>
     * </tr>
     * <tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_DATA_TYPE}</th>
     * <td>int</td>
     * <td>RDS data type</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RDS_TXT}</th>
     * <td>int</td>
     * <td>RDS txt</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_RDS_DATA = ACTION_PREFIX + "ON_RDS_DATA";

    /**
     * Broadcast Intent action for fm audio mode data event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_AUDIO_MODE}</th>
     * <td>int</td>
     * <td>Audio mode</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_AUDIO_MODE = ACTION_PREFIX + "ON_AUDIO_MODE";

    /**
     * Broadcast Intent action for fm audio path data event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_AUDIO_PATH}</th>
     * <td>int</td>
     * <td>Audio mode</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_AUDIO_PATH = ACTION_PREFIX + "ON_AUDIO_PATH";

    /**
     * Broadcast Intent action for fm world region event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_WRLD_RGN}</th>
     * <td>int</td>
     * <td>World Region</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_WRLD_RGN = ACTION_PREFIX + "ON_WRLD_RGN";

    /**
     * Broadcast Intent action for fm estimate nfl event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_NFL}</th>
     * <td>int</td>
     * <td>NFL</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_EST_NFL = ACTION_PREFIX + "ON_EST_NFL";

    /**
     * Broadcast Intent action for fm audio quality event
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_RSSI}</th>
     * <td>int</td>
     * <td>Rssi</td>
     * </tr>
     * </table>
     */
    public static final String ACTION_ON_AUDIO_QUAL = ACTION_PREFIX + "ON_AUDIO_QUAL";

    /**
     * Broadcast Intent action for fm volumeevent
     * 
     * This broadcast intent has the following return values
     * <table>
     * <tr>
     * <th>Extra Param Name</th>
     * <th>Data Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_VOL}</th>
     * <td>int</td>
     * <td>Volume</td>
     * </tr>
     * <tr>
     * <th>{@link #EXTRA_STATUS}</th>
     * <td>int</td>
     * <td>status</td>
     * </tr>
     * </table>
     */

    public static final String ACTION_ON_VOL = "ON_VOL";

    public static final String EXTRA_FREQ = "FREQ";
    public static final String EXTRA_RSSI = "RSSI";
    public static final String EXTRA_RADIO_ON = "RADIO_ON";
    public static final String EXTRA_MUTED = "MUTED";
    public static final String EXTRA_RDS_MODE = "RDS_MODE";
    public static final String EXTRA_RDS_PRGM_TYPE = "RDS_PRGM_TYPE";
    public static final String EXTRA_RDS_PRGM_TYPE_NAME = "RDS_PRGM_TYPE_NAME";
    public static final String EXTRA_RDS_PRGM_SVC = "RDS_PRGM_SVC";
    public static final String EXTRA_RDS_TXT = "RDS_TXT";
    public static final String EXTRA_RDS_DATA_TYPE = "RDS_DATA_TYPE";
    public static final String EXTRA_RDS_IDX = "RDS_IDX";
    public static final String EXTRA_SUCCESS = "RDS_SUCCESS";
    public static final String EXTRA_ALT_FREQ_MODE = "ALT_FREQ_MODE";
    public static final String EXTRA_AUDIO_MODE = "AUDIO_MODE";
    public static final String EXTRA_AUDIO_PATH = "AUDIO_PATH";
    public static final String EXTRA_WRLD_RGN = "WRLD_RGN";
    public static final String EXTRA_NFL = "NFL";

    public static final String EXTRA_STATUS = "STATUS";
    public static final String EXTRA_VOL = "VOL";

    /* FM functionality mask bit constants. */

    /** This mask sets the world region to North America. */
    public static final int FUNC_REGION_NA = 0x00; /*
                                                    * bit0/bit1/bit2: North
                                                    * America.
                                                    */
    /** This mask sets the world region to Europe. */
    public static final int FUNC_REGION_EUR = 0x01; /* bit0/bit1/bit2: Europe. */
    /** This mask sets the world region to Japan. */
    public static final int FUNC_REGION_JP = 0x02; /* bit0/bit1/bit2: Japan. */
    /** This mask enables RDS. */
    public static final int FUNC_RDS = 1 << 4; /* bit4: RDS functionality */
    /** This mask enables RDBS. */
    public static final int FUNC_RBDS = 1 << 5; /*
                                                 * bit5: RDBS functionality,
                                                 * exclusive with RDS bit
                                                 */
    /** This mask enables the Alternate Frequency RDS feature. */
    public static final int FUNC_AF = 1 << 6; /* bit6: AF functionality */

    /* FM audio output mode. */
    /**
     * Allows the radio to automatically select between Mono and Stereo audio
     * output.
     */
    public static final int AUDIO_MODE_AUTO = 0; /* Auto blend by default. */
    /** Forces Stereo mode audio. */
    public static final int AUDIO_MODE_STEREO = 1; /* Manual stereo switch. */
    /** Forces Mono mode audio. */
    public static final int AUDIO_MODE_MONO = 2; /* Manual mono switch. */
    /** Allows Stereo mode audio with blend activation. */
    public static final int AUDIO_MODE_BLEND = 3; /* Deprecated. */
    // TODO: phase out previous line in favor of next line.
    public static final int AUDIO_MODE_SWITCH = 3; /* Switch activated. */
    /** No FM routing */
    public static final int AUDIO_PATH_NONE = 0; /* No FM routing */
    /** FM routing over speaker */
    public static final int AUDIO_PATH_SPEAKER = 1; /* FM routing speaker */
    /** FM routing over wired headset */
    public static final int AUDIO_PATH_WIRE_HEADSET = 2; /*
                                                          * FM routing over
                                                          * wired headset
                                                          */
    /** FM routing over I2S */
    public static final int AUDIO_PATH_DIGITAL = 3; /* FM routing over I2S */

    /* FM audio quality. */
    /**
     * The audio quality of reception.
     */
    /** Using Stereo mode audio quality. */
    public static final int AUDIO_QUALITY_STEREO = 1; /* Manual stereo switch. */
    /** Using Mono mode audio quality. */
    public static final int AUDIO_QUALITY_MONO = 2; /* Manual mono switch. */
    /** Using Blend mode audio quality. */
    public static final int AUDIO_QUALITY_BLEND = 4; /*
                                                      * Auto stereo, and switch
                                                      * activated.
                                                      */

    /* FM scan mode. */
    /** This sets default direction scanning when seeking stations. */
    public static final int SCAN_MODE_NORMAL = 0x00;
    /** This sets scanning to go downwards when seeking stations. */
    public static final int SCAN_MODE_DOWN = 0x00;
    /** This sets scanning to go upwards when seeking stations. */
    public static final int SCAN_MODE_UP = 0x01;
    /**
     * This sets scanning to cover the whole bandwidth and return multiple hits.
     */
    public static final int SCAN_MODE_FULL = 0x82;

    /* Deemphasis time */
    /** This sets deemphasis to the European default. */
    public static final int DEEMPHASIS_50U = 0; /*
                                                 * 6th bit in FM_AUDIO_CTRL0 set
                                                 * to 0, Europe default
                                                 */
    /** This sets deemphasis to the US default. */
    public static final int DEEMPHASIS_75U = 1 << 6; /*
                                                      * 6th bit in
                                                      * FM_AUDIO_CTRL0 set to 1,
                                                      * US default
                                                      */

    /* Step type for searching */
    /** This sets the frequency interval to 100 KHz when seeking stations. */
    public static final int FREQ_STEP_100KHZ = 0x00;
    /** This sets the frequency interval to 50 KHz when seeking stations. */
    public static final int FREQ_STEP_50KHZ = 0x10;

    public static final int FM_VOLUME_MAX = 0xFF;

    /* Noise floor level */
    /** This sets the Noise Floor Level to LOW. */
    public static final int NFL_LOW = 0;
    /** This sets the Noise Floor Level to MEDIUM. */
    public static final int NFL_MED = 1;
    /** This sets the Noise Floor Level to FINE. */
    public static final int NFL_FINE = 2;

    /* RDS RDBS type */
    /** This deactivates all RDS and RDBS functionality. */
    public static final int RDS_MODE_OFF = 0;
    /** This activates RDS or RDBS as appropriate. */
    public static final int RDS_MODE_DEFAULT_ON = 1;
    /** This activates RDS. */
    public static final int RDS_MODE_RDS_ON = 2;
    /** This activates RDBS. */
    public static final int RDS_MODE_RBDS_ON = 3;

    /* RDS condition type */
    /** Selects no PTY or TP functionality. */
    public static final int RDS_COND_NONE = 0;
    /** Activates RDS PTY capability. */
    public static final int RDS_COND_PTY = 1;
    /** Activates RDS TP capability. */
    public static final int RDS_COND_TP = 2;

    /* RDS feature values. */
    /** Specifies the Program Service feature. */
    public static final int RDS_FEATURE_PS = 4;
    /** Specifies the Program Type feature. */
    public static final int RDS_FEATURE_PTY = 8;
    /** Specifies the Traffic Program feature. */
    public static final int RDS_FEATURE_TP = 16;
    /** Specifies the Program Type Name feature. */
    public static final int RDS_FEATURE_PTYN = 32;
    /** Specifies the Radio Text feature. */
    public static final int RDS_FEATURE_RT = 64;

    /* AF Modes. */
    /** Disables AF capability. */
    public static final int AF_MODE_OFF = 0;
    /** Enables AF capability. */
    public static final int AF_MODE_ON = 1;

    /* The default constants applied on system startup. */
    /**
     * Specifies default minimum signal strength that will be identified as a
     * station when scanning.
     * */
    public static final int MIN_SIGNAL_STRENGTH_DEFAULT = 105;
    /** Specifies default radio functionality. */
    public static final int FUNCTIONALITY_DEFAULT = FUNC_REGION_NA;
    /** Specifies default world frequency region. */
    public static final int FUNC_REGION_DEFAULT = FUNC_REGION_NA;
    /** Specifies default frequency scanning step to use. */
    public static final int FREQ_STEP_DEFAULT = FREQ_STEP_100KHZ;
    /** Specifies if live audio quality sampling is enabled by default. */
    public static final boolean LIVE_AUDIO_QUALITY_DEFAULT = false;
    /** Specifies the default estimated Noise Floor Level. */
    public static final int NFL_DEFAULT = NFL_MED;
    /** Specifies the default signal poll interval in ms. */
    public static final int SIGNAL_POLL_INTERVAL_DEFAULT = 100;
    /** Specifies the default signal poll interval in ms. */
    public static final int DEEMPHASIS_TIME_DEFAULT = DEEMPHASIS_75U;
    /** Default Alternate Frequency mode (DISABLED). */
    public static final int AF_MODE_DEFAULT = AF_MODE_OFF;

    /* Return status codes. */
    /** Function executed correctly. Parameters checked OK. */
    public static final int STATUS_OK = 0;
    /** General nonspecific error occurred. */
    public static final int STATUS_FAIL = 1;
    /** Server call resulted in exception. */
    public static final int STATUS_SERVER_FAIL = 2;
    /** Function could not be executed at this time. */
    public static final int STATUS_ILLEGAL_COMMAND = 3;
    /** Function parameters are out of allowed range. */
    public static final int STATUS_ILLEGAL_PARAMETERS = 4;

	/**
	 * @hide
	 */
	protected Context mContext;
	
	/**
	 * @hide
	 */
	protected EventCallbackHandler mEventCallbackHandler;
	
	/**
	 * @hide
	 */
	protected BroadcastReceiver mBroadcastReceiver;
	
	/**
	 * @hide
	 */
	protected int mReceiverPriority=DEFAULT_BROADCAST_RECEIVER_PRIORITY;
    
	/**
	 * @hide
	 */
	protected synchronized Handler initEventCallbackHandler() {
		//TODO: what's this
        //return null;//initEventCallbackHandler(null);
		if (mEventCallbackHandler == null) {
			mEventCallbackHandler = new EventCallbackHandler();
			mEventCallbackHandler.start();
			while (mEventCallbackHandler.mHandler ==null) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {}
				
			}
		}
		return mEventCallbackHandler.mHandler;
	}
	
	/**
	 * @hide
	 */ 
	protected synchronized void finishEventCallbackHandler() {
		if (mEventCallbackHandler != null) {
			mEventCallbackHandler.finish();
			mEventCallbackHandler=null;
		}
	}

    public FmReceiver() {
    }

	/**
	 * Fast comparison function to determine if two Bluetooth action strings
	 * are equal. The first 'offset' characters are ignored.
	 * @hide
	 * @param a1
	 * @param a2
	 * @param offset
	 * @return
	 * @hide
	 */
	protected static boolean actionsEqual(String a1, String a2, int offset) {
		if (a1 == a2)
			return true;
		int a1length = a1.length();
		if (a1length != a2.length())
			return false;

		return (a1.regionMatches(offset, a2, offset, a1length - offset));
	}

	/**
	 * @hide
	 */
	protected class EventCallbackHandler extends Thread {
	    
		public Handler mHandler;
		
		public EventCallbackHandler() {
			super();
			setPriority(Process.THREAD_PRIORITY_BACKGROUND);
		}
		public void run() {
			Looper.prepare();
			//TODO: what's this
			//mHandler = (mCreator == null? new Handler(): mCreator.create());
			Looper.loop();
		}
		
		
		public void finish() {
			if (mHandler != null) {
				Looper l = mHandler.getLooper();
				if (l != null) {
					l.quit();
				}
			}
		}
	}
	
    /**
     * Creates a filter for all FTP events
     * 
     * @param filter
     * @return
     */
    public static IntentFilter createFilter(IntentFilter filter) {
        if (filter == null) {
            filter = new IntentFilter();
        }
        filter.addAction(ACTION_ON_AUDIO_MODE);
        filter.addAction(ACTION_ON_AUDIO_PATH);
        filter.addAction(ACTION_ON_AUDIO_QUAL);
        filter.addAction(ACTION_ON_EST_NFL);
        filter.addAction(ACTION_ON_RDS_DATA);
        filter.addAction(ACTION_ON_RDS_MODE);
        filter.addAction(ACTION_ON_SEEK_CMPL);
        filter.addAction(ACTION_ON_STATUS);
        filter.addAction(ACTION_ON_VOL);
        filter.addAction(ACTION_ON_WRLD_RGN);
        return filter;
    }

    public synchronized void finish() {
        if (FmReceiverServiceConfig.USE_BROADCAST_INTENTS) {
            if (mBroadcastReceiver != null) {
                mContext.unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            }
        }
        
		if (mEventCallbackHandler != null) {
			mEventCallbackHandler.finish();
			mEventCallbackHandler=null;
		}
		
		if (mContext != null) {
			mContext = null;
		}
    }

    /**
     * Turns on the radio and plays audio using the specified functionality
     * mask.
     * <p>
     * After executing this function, the application should wait for a
     * confirmatory status event callback before calling further API functions.
     * Furthermore, applications should call the {@link #turnOffRadio()}
     * function before shutting down.
     * 
     * @param functionalityMask
     *            is a bitmask comprised of one or more of the following fields:
     *            {@link #FUNC_REGION_NA}, {@link #FUNC_REGION_JP},
     *            {@link #FUNC_REGION_EUR}, {@link #FUNC_RDS},
     *            {@link #FUNC_RBDS} and {@link #FUNC_AF}
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int turnOnRadio() {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = turnOnRadioNative();
        if (returnCode != STATUS_OK) {
        	Log.e(TAG, "turnOnRadio() failed: returnCode = " + returnCode);
        }

        return returnCode;
    }
    
    private native int turnOnRadioNative();

    /**
     * Turns off the radio.
     * <p>
     * After executing this function, the application should wait for a
     * confirmatory status event callback before shutting down.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int turnOffRadio() {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = turnOffRadioNative();
        if (returnCode != STATUS_OK) {
        	Log.e(TAG, "turnOffRadio() failed: returnCode = " + returnCode);
        }

        return returnCode;
    }
    
    private native int turnOffRadioNative();

    /**
     * Tunes radio to a specific frequency. If successful results in a status
     * event callback.
     * 
     * @param freq
     *            the frequency to tune to.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int tuneRadio(int freq) {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = tuneRadioNative(freq);
        if (returnCode != STATUS_OK) {
        	Log.e(TAG, "tuneRadio() failed: returnCode = " + returnCode);
        }

        return returnCode;
    }
    
    private native int tuneRadioNative(int freq);

    /**
     * Gets current radio status. This results in a status event callback.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int getStatus() {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: getStatus
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "getStatus() failed", e);
        }

        return returnCode;
    }

    /**
     * Get the On/Off status of FM radio receiver module.
     * 
     * @return true if radio is on, otherwise returns false.
     * 
     */

    public boolean getRadioIsOn() {
    	return getRadioIsOnNative();
    }

    private native boolean getRadioIsOnNative();

    /**
     * Mutes/unmutes radio audio. If muted the hardware will stop sending audio.
     * This results in a status event callback.
     * 
     * @param mute
     *            True to mute audio, False to unmute audio.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int muteAudio(boolean mute) {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = muteAudioNative(mute);
        if (returnCode != STATUS_OK) {
        	Log.e(TAG, "muteAudio() failed: returnCode = " + returnCode);
        }

        return returnCode;
    }

    private native int muteAudioNative(boolean mute);

    /**
     * Scans FM toward higher/lower frequency for next clear channel. Will
     * result in a seek complete event callback.
     * <p>
     * 
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param minSignalStrength
     *            Minimum signal strength, default =
     *            {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekStation(int scanMode, int minSignalStrength) {
        int freq = seekStationNative(scanMode);
        if (freq == -1) {
        	Log.e(TAG, "SeekStation() failed: freq = " + freq);
        }
        
        return freq;
    }

    private native int seekStationNative(int scanMode);
    
    /**
     * Scans FM toward higher/lower frequency for next clear channel. Will
     * result in a seek complete event callback.
     * <p>
     * Scans with default signal strength setting =
     * {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * 
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public int seekStation(int scanMode) {
        return seekStation(scanMode, MIN_SIGNAL_STRENGTH_DEFAULT);
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel that
     * supports the requested RDS functionality. Will result in a seek complete
     * event callback.
     * <p>
     * 
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param minSignalStrength
     *            Minimum signal strength, default =
     *            {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @param rdsCondition
     *            the type of RDS condition to scan for.
     * @param rdsValue
     *            the condition value to match.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekRdsStation(int scanMode, int minSignalStrength, int rdsCondition, int rdsValue) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: seekRdsStation
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "seekRdsStation() failed", e);
        }

        return returnCode;
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel that
     * supports the requested RDS functionality.. Will result in a seek complete
     * event callback.
     * <p>
     * Scans with default signal strength setting =
     * {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * 
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param rdsCondition
     *            the type of RDS condition to scan for.
     * @param rdsValue
     *            the condition value to match.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public int seekRdsStation(int scanMode, int rdsCondition, int rdsValue) {
        return seekRdsStation(scanMode, MIN_SIGNAL_STRENGTH_DEFAULT, rdsCondition, rdsValue);
    }

    /**
     * Aborts the current station seeking operation if any. Will result in a
     * seek complete event containing the last scanned frequency.
     * 
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekStationAbort() {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = seekStationAbortNative();
        if (returnCode != STATUS_OK) {
        	Log.e(TAG, "seekStationAbort() failed: returnCode = " + returnCode);
        }

        return returnCode;
    }

    private native int seekStationAbortNative();

    /**
     * Enables/disables RDS/RDBS feature and AF algorithm. Will result in a RDS
     * mode event callback.
     * <p>
     * 
     * @param rdsMode
     *            Turns on the RDS or RBDS. See {@link #RDS_MODE_OFF},
     *            {@link #RDS_MODE_DEFAULT_ON}, {@link #RDS_MODE_RDS_ON},
     *            {@link #RDS_MODE_RBDS_ON}
     * @param rdsFeatures
     *            the features to enable in RDS parsing.
     * @param afMode
     *            enables AF algorithm if True. Disables it if False
     * @param afThreshold
     *            the RSSI that the AF should jump to an alternate frequency on.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onRdsModeEvent().
     */
    public synchronized int setRdsMode(int rdsMode, int rdsFeatures, int afMode, int afThreshold) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: setRdsMode
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "setRdsMode() failed", e);
        }

        return returnCode;
    }

    /**
     * Configures FM audio mode to be mono, stereo or blend. Will result in an
     * audio mode event callback.
     * 
     * @param audioMode
     *            the audio mode such as stereo or mono. The following values
     *            should be used {@link #AUDIO_MODE_AUTO},
     *            {@link #AUDIO_MODE_STEREO}, {@link #AUDIO_MODE_MONO} or
     *            {@link #AUDIO_MODE_BLEND}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onAudioModeEvent().
     */
    public synchronized int setAudioMode(int audioMode) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: setAudioMode
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "setAudioMode() failed", e);
        }

        return returnCode;
    }

    /**
     * Configures FM audio path to AUDIO_PATH_NONE, AUDIO_PATH_SPEAKER,
     * AUDIO_PATH_WIRED_HEADSET or AUDIO_PATH_DIGITAL. Will result in an audio
     * path event callback.
     * 
     * @param audioPath
     *            the audio path such as AUDIO_PATH_NONE, AUDIO_PATH_SPEAKER,
     *            AUDIO_PATH_WIRED_HEADSET or AUDIO_PATH_DIGITAL. The following
     *            values should be used {@link #AUDIO_PATH_NONE},
     *            {@link #AUDIO_PATH_SPEAKER}, {@link #AUDIO_PATH_WIRE_HEADSET}
     *            or {@link #AUDIO_PATH_DIGITAL}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onAudioPathEvent().
     */
    public synchronized int setAudioPath(int audioPath) {
        int returnCode = STATUS_SERVER_FAIL;

        returnCode = setAudioPathNative(audioPath);
        if (returnCode != STATUS_OK) {
            Log.e(TAG, "setAudioPath() failed: returnCode = " + returnCode);        	
        }

        return returnCode;
    }

    private native int setAudioPathNative(int audioPath);
    /**
     * Sets the minimum frequency step size to use when scanning for stations.
     * This function does not result in a status callback and the calling
     * application should therefore keep track of this setting.
     * 
     * @param stepSize
     *            a frequency interval set to {@link #FREQ_STEP_100KHZ} or
     *            {@link #FREQ_STEP_50KHZ}.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     */
    public synchronized int setStepSize(int stepSize) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: setStepSize
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "setStepSize() failed", e);
        }

        return returnCode;
    }

    /**
     * Sets the FM volume.
     * 
     * @param volume
     *            range from 0 to 0x100
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onVolumeEvent().
     */
    public synchronized int setFMVolume(int volume) {
        int returnCode = STATUS_SERVER_FAIL;
        
        returnCode = setFMVolumeNative(volume);
        if (returnCode != STATUS_OK) {
            Log.e(TAG, "setFMVolume() returnCode = " + returnCode);
        }

        return returnCode;
    }
    
    private native int setFMVolumeNative(int volume);

    /**
     * Sets a the world frequency region and the deemphasis time. This results
     * in a world frequency event callback.
     * 
     * @param worldRegion
     *            the world region the FM receiver is located. Set to
     *            {@link #FUNC_REGION_NA}, {@link #FUNC_REGION_EUR} or
     *            {@link #FUNC_REGION_JP}.
     * @param deemphasisTime
     *            the deemphasis time that can be set to either
     *            {@link #DEEMPHASIS_50U} or {@link #DEEMPHASIS_75U}.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onWorldRegionEvent().
     */
    public synchronized int setWorldRegion(int worldRegion, int deemphasisTime) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: setWorldRegion
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "setWorldRegion() failed", e);
        }

        return returnCode;
    }

    /**
     * Estimates the noise floor level given a specific type request. This
     * function returns an RSSI level that is useful for specifying as the
     * minimum signal strength for scan operations.
     * 
     * @param nflLevel
     *            estimate noise floor for {@link #NFL_LOW}, {@link #NFL_MED} or
     *            {@link #NFL_FINE}.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onEstimateNflEvent().
     */
    public synchronized int estimateNoiseFloorLevel(int nflLevel) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: estimateNoiseFloorLevel
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "estimateNoiseFloorLevel() failed", e);
        }

        return returnCode;
    }

    /**
     * Enables or disables the live polling of audio quality on the currently
     * tuned frequency using a specific poll interval.
     * 
     * @param liveAudioPolling
     *            enables/disables live polling of audio quality.
     * @param signalPollInterval
     *            the sample interval for live polling of audio quality.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onLiveAudioQualityEvent().
     */
    public synchronized int setLiveAudioPolling(boolean liveAudioPolling, int signalPollInterval) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
        	//TODO: setLiveAudioPolling
        	returnCode = 0;
        } catch (Exception e) {
            Log.e(TAG, "setLiveAudioPolling() failed", e);
        }

        return returnCode;
    }

//    /**
//     * The class containing all the FmReceiver callback function handlers. These
//     * functions will be called by the FmReceiverService module when callback
//     * events occur. They in turn relay the callback information back to the
//     * main applications callback handler.
//     */
//    private class FmReceiverCallback extends IFmReceiverCallback.Stub {
//
//        // 
//        public synchronized void onStatusEvent(int freq, int rssi, boolean radioIsOn, int rdsProgramType,
//                String rdsProgramService, String rdsRadioText, String rdsProgramTypeName, boolean isMute)
//                throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler) {
//                mEventHandler.onStatusEvent(freq, rssi, radioIsOn, rdsProgramType, rdsProgramService, rdsRadioText,
//                        rdsProgramTypeName, isMute);
//            }
//        }
//
//        // 
//        public synchronized void onSeekCompleteEvent(int freq, int rssi, boolean seeksuccess) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onSeekCompleteEvent(freq, rssi, seeksuccess);
//        }
//
//        // 
//        public synchronized void onRdsModeEvent(int rdsMode, int alternateFreqHopEnabled) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onRdsModeEvent(rdsMode, alternateFreqHopEnabled);
//        }
//
//        // 
//        public synchronized void onRdsDataEvent(int rdsDataType, int rdsIndex, String rdsText) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onRdsDataEvent(rdsDataType, rdsIndex, rdsText);
//        }
//
//        // 
//        public synchronized void onAudioModeEvent(int audioMode) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onAudioModeEvent(audioMode);
//        }
//
//        // 
//        public synchronized void onAudioPathEvent(int audioPath) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onAudioPathEvent(audioPath);
//        }
//
//        // 
//        public synchronized void onEstimateNflEvent(int nfl) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onEstimateNoiseFloorLevelEvent(nfl);
//        }
//
//        // 
//        public synchronized void onLiveAudioQualityEvent(int rssi) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onLiveAudioQualityEvent(rssi);
//        }
//
//        // 
//        public synchronized void onWorldRegionEvent(int worldRegion) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onWorldRegionEvent(worldRegion);
//        }
//
//        public synchronized void onVolumeEvent(int status, int volume) throws RemoteException {
//            /* Process and hand this event information to the application. */
//            if (null != mEventHandler)
//                mEventHandler.onVolumeEvent(status, volume);
//        }
//    };
//
//    private class FmBroadcastReceiver extends BroadcastReceiver {
//
//        public void onReceive(Context context, Intent intent) {
//
//            IFmReceiverEventHandler handler = mEventHandler;
//            if (handler == null) {
//                return;
//            }
//
//            abortBroadcast();
//
//            String action = intent.getAction();
//            if (actionsEqual(ACTION_ON_STATUS, action, ACTION_PREFIX_LENGTH)) {
//                handler.onStatusEvent(intent.getIntExtra(EXTRA_FREQ, 0), intent.getIntExtra(EXTRA_RSSI, 0), intent
//                        .getBooleanExtra(EXTRA_RADIO_ON, false), intent.getIntExtra(EXTRA_RDS_PRGM_TYPE, -1), intent
//                        .getStringExtra(EXTRA_RDS_PRGM_SVC), intent.getStringExtra(EXTRA_RDS_TXT), intent
//                        .getStringExtra(EXTRA_RDS_PRGM_TYPE_NAME), intent.getBooleanExtra(EXTRA_MUTED, false));
//            } else if (actionsEqual(ACTION_ON_AUDIO_MODE, action, ACTION_PREFIX_LENGTH)) {
//                handler.onAudioModeEvent(intent.getIntExtra(EXTRA_AUDIO_MODE, -1));
//            } else if (actionsEqual(ACTION_ON_AUDIO_PATH, action, ACTION_PREFIX_LENGTH)) {
//                handler.onAudioPathEvent(intent.getIntExtra(EXTRA_AUDIO_PATH, -1));
//            } else if (actionsEqual(ACTION_ON_AUDIO_QUAL, action, ACTION_PREFIX_LENGTH)) {
//                handler.onLiveAudioQualityEvent(intent.getIntExtra(EXTRA_RSSI, -1));
//            } else if (actionsEqual(ACTION_ON_EST_NFL, action, ACTION_PREFIX_LENGTH)) {
//                handler.onEstimateNoiseFloorLevelEvent(intent.getIntExtra(EXTRA_NFL, -1));
//            } else if (actionsEqual(ACTION_ON_RDS_DATA, action, ACTION_PREFIX_LENGTH)) {
//                handler.onRdsDataEvent(intent.getIntExtra(EXTRA_RDS_DATA_TYPE, -1), intent.getIntExtra(EXTRA_RDS_IDX,
//                        -1), intent.getStringExtra(EXTRA_RDS_TXT));
//            } else if (actionsEqual(ACTION_ON_RDS_MODE, action, ACTION_PREFIX_LENGTH)) {
//                handler.onRdsModeEvent(intent.getIntExtra(EXTRA_RDS_MODE, -1), intent.getIntExtra(EXTRA_ALT_FREQ_MODE,
//                        -1));
//            } else if (actionsEqual(ACTION_ON_SEEK_CMPL, action, ACTION_PREFIX_LENGTH)) {
//                handler.onSeekCompleteEvent(intent.getIntExtra(EXTRA_FREQ, -1), intent.getIntExtra(EXTRA_RSSI, -1),
//                        intent.getBooleanExtra(EXTRA_SUCCESS, false));
//            } else if (actionsEqual(ACTION_ON_VOL, action, ACTION_PREFIX_LENGTH)) {
//                handler.onVolumeEvent(intent.getIntExtra(EXTRA_STATUS, -1), intent.getIntExtra(EXTRA_VOL, -1));
//            } else if (actionsEqual(ACTION_ON_WRLD_RGN, action, ACTION_PREFIX_LENGTH)) {
//                handler.onWorldRegionEvent(intent.getIntExtra(EXTRA_WRLD_RGN, -1));
//            }
//
//        }
//    }

//    private class LocalFmReceiverCallback implements IFmReceiverCallback {
//        private static final int EVT_ON_AUDIO_MODE=0;
//        private static final int EVT_ON_AUDIO_PATH=1;
//        private static final int EVT_ON_EST_NFL=2;
//        private static final int EVT_ON_LIVE_AUDIO_QUAL=3;
//        private static final int EVT_ON_RDS_DATA=4;
//        private static final int EVT_ON_RDS_MODE=5;
//        private static final int EVT_ON_SEEK_COMPL=6;
//        private static final int EVT_ON_STATUS=7;
//        private static final int EVT_ON_VOL=8;
//        private static final int EVT_ON_WORLD_REG=9;
//
//        Handler mHandler;
//        private class HandlerCreatorImpl implements IHandlerCreator {
//            public Handler create() {
//                return new Handler() {
//                    public void handleMessage(Message msg) {
//                        IFmReceiverEventHandler eventHandler = mEventHandler;
//                        if (eventHandler == null) {return;}
//                        switch (msg.what) {
//                            case EVT_ON_AUDIO_MODE:
//                                eventHandler.onAudioModeEvent(msg.arg1);
//                                break;
//                            case EVT_ON_AUDIO_PATH:
//                                eventHandler.onAudioPathEvent(msg.arg1);
//                                break;
//                            case EVT_ON_EST_NFL:
//                                eventHandler.onEstimateNoiseFloorLevelEvent(msg.arg1);
//                                break;
//                            case EVT_ON_LIVE_AUDIO_QUAL:
//                                eventHandler.onLiveAudioQualityEvent(msg.arg1);
//                                break;
//                            case EVT_ON_RDS_DATA:
//                                eventHandler.onRdsDataEvent(msg.arg1,msg.arg2, (String) msg.obj);
//                                break;
//                            case EVT_ON_RDS_MODE:
//                                eventHandler.onRdsModeEvent(msg.arg1,msg.arg2);
//                                break;
//                            case EVT_ON_SEEK_COMPL:
//                                eventHandler.onSeekCompleteEvent(msg.arg1, msg.arg2, (Boolean)msg.obj);
//                                break;
//                            case EVT_ON_STATUS:
//                                Bundle b= msg.getData();
//                                boolean radioIsOn = b.getBoolean(EXTRA_RADIO_ON, false);
//                                int rdsProgramType = b.getInt(EXTRA_RDS_PRGM_TYPE, -1);
//                                String rdsProgramService = b.getString(EXTRA_RDS_PRGM_SVC);
//                                String rdsRadioText = b.getString(EXTRA_RDS_TXT);
//                                String rdsProgramTypeName = b.getString(EXTRA_RDS_PRGM_TYPE_NAME);
//                                boolean isMute = b.getBoolean(EXTRA_MUTED, false);
//                                eventHandler.onStatusEvent(msg.arg1, msg.arg2, radioIsOn, rdsProgramType, rdsProgramService, rdsRadioText, rdsProgramTypeName, isMute);
//                                break;
//                            case EVT_ON_VOL:
//                                eventHandler.onVolumeEvent(msg.arg1, msg.arg2);
//                                break;
//                                case EVT_ON_WORLD_REG:
//                                    eventHandler.onWorldRegionEvent(msg.arg1);
//                                    break;
//                                }   
//                    }   
//                };
//            }
//        }
//        
//        public LocalFmReceiverCallback() {
//            mHandler = initEventCallbackHandler(new HandlerCreatorImpl());
//        }
//        public IBinder asBinder() {
//            return null;
//        }
//        
//            
//        public void onAudioModeEvent(int audioMode) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_AUDIO_MODE);
//            m.arg1=audioMode;
//            mHandler.sendMessage(m);
//        }
//
//        public void onAudioPathEvent(int audioPath) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_AUDIO_PATH);
//            m.arg1=audioPath;
//            mHandler.sendMessage(m);
//        }
//
//        public void onEstimateNflEvent(int nfl) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_EST_NFL);
//            m.arg1=nfl;
//            mHandler.sendMessage(m);
//        }
//
//        public void onLiveAudioQualityEvent(int rssi) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//                Message m = mHandler.obtainMessage(EVT_ON_LIVE_AUDIO_QUAL);
//            m.arg1=rssi;
//            mHandler.sendMessage(m);
//        }
//
//        public void onRdsDataEvent(int rdsDataType, int rdsIndex, String rdsText) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_RDS_DATA);
//            m.arg1=rdsDataType;
//            m.arg2=rdsIndex;
//            m.obj=rdsText;
//            mHandler.sendMessage(m);
//            
//        }
//
//        public void onRdsModeEvent(int rdsMode, int alternateFreqHopEnabled) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_RDS_MODE);
//            m.arg1=rdsMode;
//            m.arg2=alternateFreqHopEnabled;
//            mHandler.sendMessage(m);
//            
//        }
//
//        public void onSeekCompleteEvent(int freq, int rssi, boolean seeksuccess) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_SEEK_COMPL);
//            m.arg1=freq;
//            m.arg2=rssi;
//            m.obj=seeksuccess;
//            mHandler.sendMessage(m);
//            
//        }
//
//        public void onStatusEvent(int freq, int rssi, 
//                boolean radioIsOn, int rdsProgramType, 
//                String rdsProgramService,
//                String rdsRadioText, String rdsProgramTypeName, 
//                boolean isMute) throws RemoteException {
//            if (mEventHandler ==null) {return;}                    
//            Message m = mHandler.obtainMessage(EVT_ON_STATUS);
//            m.arg1=freq;
//            m.arg2=rssi;
//            Bundle b= m.getData();
//            b.putBoolean(EXTRA_RADIO_ON, radioIsOn);
//            b.putInt(EXTRA_RDS_PRGM_TYPE, rdsProgramType);
//            b.putString(EXTRA_RDS_PRGM_SVC, rdsProgramService);
//            b.putString(EXTRA_RDS_TXT, rdsRadioText);
//            b.putString(EXTRA_RDS_PRGM_TYPE_NAME, rdsProgramTypeName);
//            b.putBoolean(EXTRA_MUTED, isMute);
//            mHandler.sendMessage(m);
//        }
//
//        public void onVolumeEvent(int status, int volume) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_VOL);
//            m.arg1=status;
//            m.arg2=volume;
//            mHandler.sendMessage(m);
//            
//        }
//
//        public void onWorldRegionEvent(int worldRegion) throws RemoteException {
//            if (mEventHandler ==null) {return;}
//            Message m = mHandler.obtainMessage(EVT_ON_WORLD_REG);
//            m.arg1=worldRegion;
//            mHandler.sendMessage(m);
//        }
//
//    }

}
