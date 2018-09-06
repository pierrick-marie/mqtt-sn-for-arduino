
#ifndef __LOGS_H__
#define __LOGS_H__

#define DEBUG true

class Logs {

public:
	Logs();
	~Logs();

	void debug(const char* className, const char* methodeName);

	void debug(const char* className, const char* methodeName, const char* message);

	void debug(const char* className, const char* methodeName, const char* message, const int value);

	void debug(const char* className, const char* methodeName, const char* message, const char* value);
};

#endif
