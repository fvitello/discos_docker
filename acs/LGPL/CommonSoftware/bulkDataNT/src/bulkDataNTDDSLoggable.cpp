/*******************************************************************************
* ALMA - Atacama Large Millimiter Array
* (c) European Southern Observatory, 2011
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
* "@(#) $Id: bulkDataNTDDSLoggable.cpp,v 1.5 2012/04/25 12:37:14 bjeram Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* bjeram  2011-04-19  created
*/

#include "bulkDataNTDDSLoggable.h"
#include <maciHelper.h>
#include <maciContainerImpl.h>
#include <maciSimpleClient.h>

LoggingProxy *BulkDataNTDDSLoggable::logger_mp=0;
bool BulkDataNTDDSLoggable::isLocalLogger_m=false;

unsigned int BulkDataNTDDSLoggable::loggerInitCount_m=0;

BulkDataNTDDSLoggable::~BulkDataNTDDSLoggable ()
{
	ACS_TRACE(__FUNCTION__);

}//~BulkDataNTDDSLoggable


void BulkDataNTDDSLoggable::initalizeLogging()
{
	// this code is a bit dirty, but wee need to initialize loggerproxy per thread
	//isThreadInit return 0 if it is initialized !!!
	if (logger_mp==0 || !LoggingProxy::isInitThread() )
	{
		//TBD here we have to set centralized loggger as well, but we need some support from logging
		if (logger_mp==0) //if we do not have a logger we create one for all DDS threads
		{ //ICT-6837
			if (maci::ContainerImpl::getContainer() != NULL && maci::ContainerImpl::getLoggerProxy() != NULL)
				logger_mp = maci::ContainerImpl::getLoggerProxy();
			else if (maci::SimpleClient::getInstance() != NULL && maci::SimpleClient::getLoggerProxy() != NULL)
				logger_mp = maci::SimpleClient::getLoggerProxy();
			else { //Only local logger.
				isLocalLogger_m = true;
				logger_mp = new LoggingProxy(0, 0, 31);
				//Try to initialize the remote logger, if we have naming service and AcsLogService running, but we're detached from a maciSimpleClient and Container.
				int argc = 3;
				const char* argv[] = { "", "-ORBDottedDecimalAddresses", "1"};
				CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);
				CosNaming::NamingContext_var naming_context = maci::MACIHelper::resolveNameService(orb.in());
				if (!CORBA::is_nil(naming_context.ptr()))
				{
					std::string channelAndDomainName;
					CosNaming::Name name;
					name.length (1);
					name[0].id = "Log";
					CORBA::Object_var log_obj = naming_context->resolve(name);
					if(log_obj.ptr() != CORBA::Object::_nil())
					{
						Logging::AcsLogService_var logSvc = Logging::AcsLogService::_narrow(log_obj.in());
						if (logSvc.ptr() != Logging::AcsLogService::_nil())
						{
							logger_mp->setCentralizedLogger(logSvc.in());
						}
					}
				}
			}
		}
		LoggingProxy::init(logger_mp);
		loggerInitCount_m++; // we initialized Proxy another time
	}
}//initalizeLogging

Logging::Logger::LoggerSmartPtr BulkDataNTDDSLoggable::getLogger ()
{
	initalizeLogging();
	return Logging::Loggable::getLogger();
}//getLogger
