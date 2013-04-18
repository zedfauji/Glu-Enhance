def NodeNameList = []

def gen_nodeName()
{
	for (i in 1..params.noofserver )
	{
		NodeNameList.add[i] = '$param.datacenter-sigiri'i;
		
	}
}



def boolean isServiceup(NodeName)
{
	
	shell.waitFor(timeout: '30s', heartbeat: '60s'){ duration ->
			def output= shell.exec("ssh ${NodeName} sh /cacheDir/glu_scripts/load_service_monitor.sh ${params.service}")
			log.info "${output}"
			
				if(output == 'null')
					{
						log.warn ("No ${params.service} is running on this server.")
						return null
					}
			
	
}

def boolean isServiceDown( NodeName)
{
	isServiceup( NodeName) == null
}
	
def isServerup()
{
	//check if server is in stale list for this service.
	def DownServList = []
	NodeName='';
	for (i in 1..params.noofserver  ) // checking wheather serve/agent is down 
	{
		NodeName='$param.datacenter-sigiri'i;
		def resp_ssh = shell.exec("ssh ${NodeName} ps -elf|grep -v grep|grep glu")
		if (resp_ssh > 0)
		{
			log.info ("${NodeName} server is up and running")
		}
		else
		{
		DownServList.add(${NodeName}) // add server to a down server list
		log.info ("Server ${NodeName} has been flagged as Down Server ")
		log.info ("Shifting running services to another node.")
		mvServiceNextNode(NodeName)
		
		
		
		}
				
			
					
	}
}


def boolean isServerAvail(NodeName)
{
	isServerup(NodeName) == 'null'
}

def mvServiceNextNode( NodeName )
{
	//Check which services were running on the downed server.
	
	//1. Calculate Total no. of Running services.
		
}

def checkPortAvail(NodeName, ServiceName)
{
	portRange=${params.portRange}
	def portAvail=0
		for (int i =1 ; i < 4; i++ )
		{
		servState= shell.exec("/home/nextag/glu/console-cli/bin/./console-cli.py -f Services -u admin -x admin  -b  -l -s "agent='${NodeName}.pv.sv.nextag.com';mountPoint='/${ServiceName}/p${portRange}${i}'" status|grep "servState"|awk -F '"' '{print $4}'")
		entrState= shell.exec("/home/nextag/glu/console-cli/bin/./console-cli.py -f Services -u admin -x admin  -b  -l -s "agent='${NodeName}.pv.sv.nextag.com';mountPoint='/${ServiceName}/p${portRange}${i}'" status|grep "entryState"|awk -F '"' '{print $4}'")
			if(entryState=='running' && servState=='Started')
			{
				//Do nothing
			}
			else
			portAvail=${portRange}${i}
		}
		
		if(portAvail > 0)
		{
			return portAvail
		}
		else
		return null
}


def boolean isPortAvail(NodeName, ServiceName)
{
	checkPortAvail(NodeName, ServiceName) == 'null'
}

def doRetry( NodeName , ServiceName , port)
{
		
	//Check for Down service.
	
	if ( currentRunningServ() < ${params.reqServiceNo} )
		{
			shiftServ(ServiceName, NodeName)
		}	
	else
	//Do nothing
	log.info("Current running services are with in plan so doing nothing")
}

def shiftServ(ServiceName , NodeName)
{
	// This function will shift service
	//1. Check if stale Node is availaible
	
		staleNode= ${params.stalenode}
		
		if(isServerAvail(staleNode))
		{
			log.warn ("${staleNode} server is down which is in stale node list now moving to next Node")
			shiftNextNode(NodeName)
			
		}
		
		else
		{
			log.info ("Shifting Service to stale node")
			shiftstaleNode(staleNode)
		}
	
}

shiftstaleNode(staleNode)
{
	// This funtion will shift the service to stale node.
	
	//1. we ll check wheather the port is availaible or not.
	
	if(isPortAvail)
	{
		//Port is availaible. We ll shift to this.
		startService(staleNode)
		log.info("started service on stale node")
	}
	
	else
	{
		log.info("port on stale isn't free")
	}
}


