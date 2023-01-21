/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#ifndef __LOGS_H__
#define __LOGS_H__

#include <stdint.h>
#include <string.h>

class Logs {

public:
	Logs();
	~Logs();

	void debug(const char* methodeName, const char* message);

	void debugln(const char* methodeName, const char* message);

	void debug(const char* methodeName, const char* message, const int value);

	void debug(const char* methodeName, const char* message, const char* value);

	void info(const char* message);

	void error(const char* message);

	void connectionLost();
};

#endif
