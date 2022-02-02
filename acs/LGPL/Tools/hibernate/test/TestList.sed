s/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9][ T][0-9][0-9]:[0-9][0-9]:[0-9][0-9].[0-9][0-9][0-9]/----------T--:--:--.---/g
s/Time: [0-9,.]*/Time: xxx/g
s/timestamp: [0-9]*/timestamp: xxxxxxxxxxxx/g
s/registered: [0-9,a-f,\-]*/registered: xxxxxxxxxxxxx/g
s/Instantiating session factory with properties:.*/Instantiating session factory with properties: XXXXXXXX/g
s/Registering SessionFactory:.*/Registering SessionFactory: UUID_here/g
s/@[0-9a-f]*/@xxxxxxx/g
s/Opened Session \[[0-9,a-f,\-]*\]/Opened Session [xxxxxxxxxxxxx]/g
s/Closing session \[[0-9,a-f,\-]*\]/Closing session [xxxxxxxxxxxxx]/g
s/All Services successfully started, 17 Services in [0-9]*ms/All Services successfully started, 17 Services in xxms/g
s/Cycle complete: [0-9]*/Cycle complete: x/g
