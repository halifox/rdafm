// Steps to use MiniCmd
// 1. include "mc.h" first time
// 2. #define MC_CUSTOM_CMDS like "#define MC_CUSTOM_CMDS {"playsong", play_song, "to play a song"}
//    "playsong" is the command string, play_song is command callback function, "to play a song" is help string.
// 3. in command callback functions, use MiniCmd utils to process parameters
// 4. include "mc.h" second time

// MiniCmd (mc) linucos@gmail.com @2013
#ifndef MC_H_SECOND_TIME_INCLUDE

#ifndef MC_H_FIRST_TIME_INCLUDE
#define MC_H_FIRST_TIME_INCLUDE
#else
#undef MC_H_FIRST_TIME_INCLUDE
#define MC_H_SECOND_TIME_INCLUDE
#endif

#define MC_DEBUG

// first time include, just utils functions, types
#ifdef MC_H_FIRST_TIME_INCLUDE

#ifndef NULL
#define NULL ((void *)0)
#endif

static void mc_loop(void);
typedef struct tagMCCMDITEM
{
    char* cmd_str; // command string
    void (*cmd_cb)(void); // command call back
    char* help_str; // help string for this command
}MCCMDITEM;
#define MC_MAX_CMD_PARAMS_NR 4
#define MC_MAX_CMD_BUFFER_NR 64
static char* mc_prompt_msg = "\nmc >";
static unsigned int mc_params[MC_MAX_CMD_PARAMS_NR+1] = {0};
static char mc_cmd_buf[MC_MAX_CMD_BUFFER_NR];
static char mc_running_flag = 1;
// utils
// port these !!!
static void mc_gets(char* str) {
	gets(str);
}
static void mc_puts(char* str) {
	fprintf(stdout, "%s", str);
}

static void mc_putl(char* str) {
	fprintf(stdout, "%s\n", str);
}

// fix these !!!
static void mc_memset(void* addr, unsigned char value, int len) {
	while(len--) *(( (unsigned char * )addr ) + len) = value;
}
static int mc_strlen(char* str) {
	int count = 0;
	while(str[count] != '\0') ++count;
	return count;
}
static void mc_puti(int num) {
	int i = 0, len = 1, tmp = num;
	char str[128];
	mc_memset(str, '\0', 128);
	if(num == 0) {
		str[0] = '0';
		len = 0;
	}
	else {
		while(num) {
			num /= 10;
			++len;
		}
	}
	num = tmp;
	for(i = 0; i < len && num > 0 ; ++i) {
		tmp = num%10;
		str[len - i - 1 - 1] = tmp + '0';
		num /= 10;
	}
	mc_puts(str);
}
static void mc_puth(int num) {
	int i = 0, len = 1, tmp = num;
	char str[128];
	mc_memset(str, '\0', 128);
	if(num == 0) {
		str[2] = '0';
		len = 0;
	}
	else {
		while(num) {
			num /= 16;
			++len;
		}
	}
	num = tmp;
	for(i = 0; i < len && num > 0; ++i) {
		tmp = num%16;
		if(tmp > 9)
			str[len - i - 1 - 1 + 2] = tmp + 'a' - 10;
		else
			str[len - i - 1 - 1 + 2] = tmp + '0';
		num /= 16;
	}

	str[0] = '0';
	str[1] = 'x';
	mc_puts(str);
}
static void mc_dump_hex(unsigned char* buf, int len) {
	int i = 0;
	for(i = 0; i < len; ++i) {
		mc_puts(" ");
		mc_puth(buf[i]);
	}
	mc_putl("");
}
static void mc_dump_dec(unsigned char* buf, int len) {
	int i = 0;
	for(i = 0; i < len; ++i) {
		mc_puts(" ");
		mc_puti(buf[i]);
	}
	mc_putl("");
}
// retval : 0 means non-dec str, 1 means dec str
static int mc_is_decstr(char* str) {
	int len = mc_strlen(str), i = 0;
	for(i = 0; i < len; ++i) {
		if( !(str[i] >= '0' && str[i] <= '9') ) return 0;
	}
	return 1;
}
// retval : 0 means non-hex str, 1 means without "0x" or "0X", 2 means with "0x" or "0X"
static int mc_is_hexstr(char* str) {
	int len = mc_strlen(str), i = 0;
	// check prefix
	if(len >= 2) {
		if( (str[0] == '0' && str[1] == 'x') || (str[0] == '0' && str[1] == 'X') ) {
			if(len == 2)
				return 0;
			i = 2;
		}
		else {
			i = 0;
		}
	}
	else {
		i = 0;
	}
	// check every char
	for(; i < len; ++i) {
		if( !( (str[i] <= 'F' && str[i] >= 'A') 
				|| (str[i] >= 'a' && str[i] <= 'f') 
					|| (str[i] >= '0' && str[i] <= '9') ) ) return 0;
	}
	// check prefix
	if( (str[0] == '0' && str[1] == 'x') || (str[0] == '0' && str[1] == 'X') )
		return 2;
	return 1;
}
static int mc_hexstr2int(char* str) {
	int i = 0, tmp = 0, prefix = 0;
	unsigned int val = 0;
	if(str == NULL) return 0;
	tmp = mc_is_hexstr(str);
	if(tmp == 0)
		return 0;
	// with prefix
	if(tmp == 2) prefix = 2;
	tmp = mc_strlen(str);
	for(i = prefix; i < tmp; ++i) {
		if(str[i] <= 'Z' && str[i] >= 'A')
			val = val*16 + str[i] - 'A' + 10;
		else if(str[i] <= 'z' && str[i] >= 'a')
			val = val*16 + str[i] - 'a' + 10;
		else 
			val = val*16 + str[i] - '0';
	}

	return val;
}
static int mc_decstr2int(char* str) {
	int i = 0, tmp = 0, prefix = 0;
	unsigned int val = 0;
	if(str == NULL) return 0;
	tmp = mc_is_decstr(str);
	if(tmp == 0)
		return 0;
	tmp = mc_strlen(str);
	for(i = 0; i < tmp; ++i) {
		val = val*10 + str[i] - '0';
	}

	return val;
}

#define mc_param1_hexstr2int() (mc_hexstr2int((char *)mc_params[0]))
#define mc_param2_hexstr2int() (mc_hexstr2int((char *)mc_params[1]))
#define mc_param3_hexstr2int() (mc_hexstr2int((char *)mc_params[2]))
#define mc_param4_hexstr2int() (mc_hexstr2int((char *)mc_params[3]))

#define mc_param1_decstr2int() (mc_decstr2int((char *)mc_params[0]))
#define mc_param2_decstr2int() (mc_decstr2int((char *)mc_params[1]))
#define mc_param3_decstr2int() (mc_decstr2int((char *)mc_params[2]))
#define mc_param4_decstr2int() (mc_decstr2int((char *)mc_params[3]))

#define mc_param1_str() ((char *)(mc_params[0]))
#define mc_param2_str() ((char *)(mc_params[1]))
#define mc_param3_str() ((char *)(mc_params[2]))
#define mc_param4_str() ((char *)(mc_params[3]))

#define mc_check_param(want_cnt) \
	do { \
		if(mc_param_count() < want_cnt) { \
			mc_puts("This cmd need "); \
			mc_puti(want_cnt); \
			mc_puts("param(s), but pass "); \
			mc_puti(mc_param_count()); \
			mc_putl("param(s), return !!!"); \
			return; \
		} \
	}while(0)

static int mc_param_count(void) {
	int count = 0, i = 0;
	for(i = 0; i < MC_MAX_CMD_PARAMS_NR; ++i)
		if(mc_params[i] != 0) ++count;
	return count;
}

#endif // MC_H_FIRST_TIME_INCLUDE

// second time include
#ifdef MC_H_SECOND_TIME_INCLUDE
// core
static void do_mc_help(void);
static void do_mc_quit(void);

#ifndef MC_CUSTOM_CMDS
#warning "You should define MC_CUSTOM_CMDS like "#define MC_CUSTOM_CMDS {"playsong", playsong_function, "play song help"}""
#define MC_CUSTOM_CMDS
#endif

MCCMDITEM mc_all_cmds [] = {
    // mc internal cmds
    {"h", do_mc_help, "help"},
    {"q", do_mc_quit, "quit mc"},
	MC_CUSTOM_CMDS
};
static void do_mc_help(void) {
    mc_putl("mc help:");
    unsigned int i = 0;
    for(i = 0; i < sizeof(mc_all_cmds)/sizeof(MCCMDITEM); ++i) {
        if(mc_all_cmds[i].help_str != NULL && mc_all_cmds[i].cmd_str != NULL) {
            mc_puts(mc_all_cmds[i].cmd_str);
            mc_puts(" ---- ");
            mc_puts(mc_all_cmds[i].help_str);
            mc_putl("");
        }
    }
    mc_putl("");
}
static void do_mc_quit(void) {
    mc_running_flag = 0;
    mc_putl("mc quit!");
}
static void exec_command_buffer(char* cmd_buf, int len) {
    int i = 0, cmd_index = -1, p1_index = -1, p2_index = -1, p3_index  = -1, p4_index = -1;
    if(cmd_buf == NULL || len <= 0)  {
        mc_putl("command not found");
        return;
    }
    // 1. find cmd and param
    // - replace ' ' to '\0'
    i = 0;
    while(i < len) {
        if(cmd_buf[i] == ' ' || cmd_buf[i] == '\n' || cmd_buf[i] == '\t')
            cmd_buf[i] = '\0';
        ++i;
    }
    // - find cmd index
    i = 0;
    while(i < len && cmd_buf[i] == '\0') ++i;
    if(i < len) {
        cmd_index = i;
        while(i < len && cmd_buf[i] != '\0') ++i;
    }
    // - find param index
    while(i < len) {
        // -- filter multi '\0'
        while(i < len && cmd_buf[i] == '\0') ++i;

        if(i >= len) break;
        if(p1_index == -1) {
            p1_index = i;
            goto cont;
        }
        if(p2_index == -1) {
            p2_index = i;
            goto cont;
        }
        if(p3_index == -1) {
            p3_index = i;
            goto cont;
        }
        if(p4_index == -1) {
            p4_index = i;
            goto cont;
        }
cont:
        while(i < len && cmd_buf[i] != '\0') ++i;
    }
    // -- find cmd ?
    if(cmd_index == -1) {
        mc_putl("command not found");
        return;
    }
    // 2. search and match cmd
    for(i = 0; (unsigned int)i < sizeof(mc_all_cmds)/sizeof(MCCMDITEM); ++i) {
        if(strcmp(cmd_buf + cmd_index, mc_all_cmds[i].cmd_str) == 0) {
            // prepare params, TODO
            if(p1_index != -1) mc_params[0] = (unsigned int)(mc_cmd_buf + p1_index);
            if(p2_index != -1) mc_params[1] = (unsigned int)(mc_cmd_buf + p2_index);
            if(p3_index != -1) mc_params[2] = (unsigned int)(mc_cmd_buf + p3_index);
            if(p4_index != -1) mc_params[3] = (unsigned int)(mc_cmd_buf + p4_index);
            // call cmd call back
            if(mc_all_cmds[i].cmd_cb != NULL)
                mc_all_cmds[i].cmd_cb();
			return;
        }
    }
	mc_putl("command not found");
}

static void mc_loop(void) {
    while(mc_running_flag) {
        mc_puts(mc_prompt_msg); // 1. prompt
        mc_memset(mc_cmd_buf, '\0', sizeof(mc_cmd_buf));
        mc_memset(mc_params, '\0', sizeof(mc_params));
        mc_gets((char*)mc_cmd_buf); // 2. get string
        mc_putl("\n");
        exec_command_buffer(mc_cmd_buf, mc_strlen(mc_cmd_buf)); // 3. execute command buffer
    }
}
#endif // MC_H_SECOND_TIME_INCLUDE

#endif // MC_H_SECOND_TIME_INCLUDE
// * MiniCmd end
