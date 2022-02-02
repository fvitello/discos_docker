#include "loggingExecuteWithLogger.h"
#include <maciSimpleClient.h>
#include <boost/asio.hpp>
#include <boost/thread/thread.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>

void print(const boost::system::error_code&) {
	std::cout << "Logging print function" << std::endl;
	ACS_SHORT_LOG((LM_INFO, "print function"));
}

class Test {
  public:
	void print(const boost::system::error_code&) {
		std::cout << "Logging print method" << std::endl;
		ACS_SHORT_LOG((LM_INFO, "print method"));
	}
};

int main(int argc, char** argv) {
	Test ts;
	ACS_SHORT_LOG((LM_INFO, "main method"));
	boost::asio::io_service io;
	boost::thread_group tpool;
	size_t (boost::asio::io_service::*run)() = &boost::asio::io_service::run;
	boost::asio::deadline_timer t(io, boost::posix_time::seconds(5));
	t.async_wait(boost::bind(&executeWithLogger<void (*)(const boost::system::error_code&), const boost::system::error_code&>, std::string("ThreadFunc"), &print, boost::asio::placeholders::error));
	t.async_wait(boost::bind(&executeWithLogger<void (Test::*)(const boost::system::error_code&), Test&, const boost::system::error_code&>, std::string("ThreadMethod"), &Test::print, ts, boost::asio::placeholders::error));
	tpool.add_thread(new boost::thread(run, &io));
	boost::system::error_code e;
	executeWithLogger(std::string("MainFunc"), &print, e);
	executeWithLogger(std::string("MainMethod"), &Test::print, ts, e);
	tpool.join_all();
	return 0;
}
