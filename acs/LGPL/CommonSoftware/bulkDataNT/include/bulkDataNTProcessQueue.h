#ifndef BULKDATA_NT_PROCESS_QUEUE
#define BULKDATA_NT_PROCESS_QUEUE

#include <queue>
#include <acserr.h>
#include <acsThread.h>

#include "bulkDataNT.h"
#include "bulkDataNTCallback.h"

namespace AcsBulkdata
{
  class BulkDataNTCallback;

	enum BDState {
		START,
		RECEIVE,
		STOP,
		RESET,
		ERROR,
		DATA_LOST,
		SENDER_CONNECT,
		SENDER_DISCONNECT,
		FINISH
	};
	
	struct Item {
		unsigned char data[ACSBulkData::FRAME_MAX_LEN];
		unsigned int size;
		unsigned short senders;
		unsigned long frameCount;
		unsigned long totalFrames;
		ACSErr::CompletionImpl error;
		BDState state;
	};
	
	class Queue {
	  public:
		Queue();
		void push(Item& item);
		Item pop();
		bool empty();
		void release();
		void clear();
	  protected:
		std::queue<Item> items;
		ACE_Thread_Mutex itemsMutex;
		ACE_Condition<ACE_Thread_Mutex> itemsWait;
		ACE_Thread_Mutex itemsWaitMutex;
	};
	
	class ProcessQueue: public ACS::Thread {
	  public:
		ProcessQueue(const ACE_CString name);
		virtual ~ProcessQueue();
		void setCallback(BulkDataNTCallback* callback);
		void setTopicName(std::string topicName);
		void cbStart(unsigned char* userParam_p=0, unsigned int size=0);
		void cbReceive(unsigned char* frame_p, unsigned int size);
		void cbStop();
		void cbReset();
		void onError(ACSErr::CompletionImpl &error);
		void onDataLost(unsigned long frameCount, unsigned long totalFrames, ACSErr::CompletionImpl &error);
		void onSenderConnect(unsigned short totalSenders);
		void onSenderDisconnect(unsigned short totalSenders);
		void run();
		void cleanUp();
		void forceCleanUp();
	  protected:
		Queue queue;
		BulkDataNTCallback* callback_mp;
		std::string topicName_m;
		bool processing;
	};
}

#endif //BULKDATA_NT_PROCESS_QUEUE
