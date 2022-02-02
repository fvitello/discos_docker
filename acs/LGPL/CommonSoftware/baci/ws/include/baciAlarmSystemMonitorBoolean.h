#ifndef BACIALARMSYSTEMMONITORBOOLEAN_H_
#define BACIALARMSYSTEMMONITORBOOLEAN_H_

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "baciAlarmSystemMonitor_T.h"
#include "baciROboolean.h"

namespace baci
{

/**
 * Implementation of the AlarmSystemMonitorBoolean for boolean
 */
class baci_EXPORT AlarmSystemMonitorBoolean : public AlarmSystemMonitor<ROboolean>
{
  public:
    
    AlarmSystemMonitorBoolean(ROboolean* property, EventDispatcher * eventDispatcher);

    virtual ~AlarmSystemMonitorBoolean();
    
    virtual void check(BACIValue &val,
	       const ACSErr::Completion & c,
	       const ACS::CBDescOut & desc );
 
  private:

    static const bool ENABLE;
    static const bool DISABLE;
    static const int32_t ALARM_NOT_RAISED;
    static const int32_t ALARM_RAISED;

    /**
     * ALMA C++ coding standards state assignment operators should be disabled.
     */
    void operator=(const AlarmSystemMonitorBoolean&);
    
    /**
     * ALMA C++ coding standards state copy constructors should be disabled.
     */
    AlarmSystemMonitorBoolean(const AlarmSystemMonitorBoolean&);
    
    /**
     * Update the alarm
     */
    void updateAlarm(bool enable);


};//class AlarmSystemMonitorBoolean

}//namespace baci

#endif /*BACIALARMSYSTEMBOOLEAN_H_*/
