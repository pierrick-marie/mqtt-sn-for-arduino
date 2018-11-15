
#ifndef __LOGS_H__
#define __LOGS_H__

class Logs {

public:
	Logs();
	~Logs();

	void debug(const char* methodeName);

	void debug(const char* methodeName, const char* message);

	void debug(const char* methodeName, const char* message, const int value);

	void debug(const char* methodeName, const char* message, const char* value);

	void info(const char* message);

	void error(const char* message);

	void notConnected();
};

#endif
