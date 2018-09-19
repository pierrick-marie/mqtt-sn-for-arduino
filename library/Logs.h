
#ifndef __LOGS_H__
#define __LOGS_H__

#define DEBUG true

enum log_level {
	NONE,
	ACTIVE,
	VERBOSE
};

class Logs {

public:
	Logs();
	~Logs();
	const log_level LEVEL = VERBOSE;

	void debug(const log_level level, const char* methodeName);

	void debug(const log_level level, const char* methodeName, const char* message);

	void debug(const log_level level, const char* methodeName, const char* message, const int value);

	void debug(const log_level level, const char* methodeName, const char* message, const char* value);
};

#endif
