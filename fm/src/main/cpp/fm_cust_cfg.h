
#ifndef __FM_CUST_CFG_H__
#define __FM_CUST_CFG_H__

//scan sort algorithm 
enum{
    FM_SCAN_SORT_NON = 0,
    FM_SCAN_SORT_UP,
    FM_SCAN_SORT_DOWN,
    FM_SCAN_SORT_MAX
};

//*****************************************************************************************
//***********************************FM config for customer: start******************************
//*****************************************************************************************
//RX
#define FM_RX_SEEK_SPACE      1           //FM radio seek space,1:100KHZ; 2:200KHZ
#define FM_RX_SCAN_CH_SIZE    40          //FM radio scan max channel size
#define FM_RX_BAND            1           //FM radio band, 1:87.5MHz~108.0MHz; 2:76.0MHz~90.0MHz; 3:76.0MHz~108.0MHz; 4:special
#define FM_RX_BAND_FREQ_L     875         //FM radio special band low freq(Default 87.5MHz)
#define FM_RX_BAND_FREQ_H     1080        //FM radio special band high freq(Default 108.0MHz)
#define FM_RX_DEEMPHASIS       0           //0-50us, China Mainland; 1-75us China Taiwan

//TX
#define FM_TX_SCAN_HOLE_LOW  923         //92.3MHz~95.4MHz should not show to user
#define FM_TX_SCAN_HOLE_HIGH 954         //92.3MHz~95.4MHz should not show to user


//*****************************************************************************************
//***********************************FM config for customer:end *******************************
//*****************************************************************************************
// band
#define FM_BAND_UNKNOWN 0
#define FM_BAND_UE      1 // US/Europe band  87.5MHz ~ 108MHz (DEFAULT)
#define FM_BAND_JAPAN   2 // Japan band      76MHz   ~ 90MHz
#define FM_BAND_JAPANW  3 // Japan wideband  76MHZ   ~ 108MHz
#define FM_BAND_SPECIAL 4 // special   band  between 76MHZ   and  108MHz
#define FM_BAND_DEFAULT FM_BAND_UE
#define FM_FREQ_MIN  FM_RX_BAND_FREQ_L
#define FM_FREQ_MAX  FM_RX_BAND_FREQ_H
#define FM_RAIDO_BAND FM_BAND_UE
// space
#define FM_SPACE_UNKNOWN    0
#define FM_SPACE_100K       1
#define FM_SPACE_200K       2
#define FM_SPACE_DEFAULT    FM_RX_SEEK_SPACE
#define FM_SEEK_SPACE FM_RX_SEEK_SPACE
//max scan chl num
#define FM_MAX_CHL_SIZE FM_RX_SCAN_CH_SIZE
// auto HiLo
#define FM_AUTO_HILO_OFF    0
#define FM_AUTO_HILO_ON     1
// seek direction
#define FM_SEEK_UP          0
#define FM_SEEK_DOWN        1



// seek threshold
#define FM_SEEKTH_LEVEL_DEFAULT 4

#endif // __FM_CUST_CFG_H__
