#ifndef LOGGING_EXECUTE_WITH_LOGGER_H
#define LOGGING_EXECUTE_WITH_LOGGER_H

#include "loggingACEMACROS.h"
template <class F, class... T>
auto executeWithLogger(std::string tname, F f, T... args) -> typename std::result_of<F(T...)>::type;
#include "loggingExecuteWithLogger.i"

#endif //LOGGING_EXECUTE_WITH_LOGGER_H
