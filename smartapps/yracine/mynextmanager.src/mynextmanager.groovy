/**
 *  MyNextManager (Service Manager)
 *  Copyright 2018 Yves Racine
 *  LinkedIn profile: www.linkedin.com/in/yracine
 *  Refer to readme file for installation instructions.
 *     http://github.com/yracine/device-type.myNext/blob/master/README.md
 * 
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights,
 *  trade secret in the Background technology. May be subject to consulting fees under an Agreement 
 *  between the Developer and the Customer. Developer grants a non exclusive perpetual license to use
 *  the Background technology in the Software developed for and delivered to Customer under this
 *  Agreement. However, the Customer shall make no commercial use of the Background technology without
 *  Developer's written consent.
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * 
 * Software Distribution is restricted and shall be done only with Developer's written approval.
 *
**/



definition(
    name: "${get_APP_NAME()}",
    namespace: "yracine",
    author: "Yves Racine",
    description: "Connect your Nest Products to SmartThings.",
    category: "My Apps",
    iconUrl: "${getCustomImagePath()}WorksWithNest.jpg",
    iconX2Url: "${getCustomImagePath()}WorksWithNest.jpg",
    singleInstance: true    
)

private def get_APP_VERSION() {
	return "1.4.3a"
}    
preferences {

	if (!atomicState.accessToken) {
		page(name: "about", title: "About", nextPage:"auth")
	} else {
		page(name: "about", title: "About", nextPage:"structureList")
	}    
	page(name: "auth", title: "Next", content:"authPage", nextPage:"structureList")
	page(name: "structureList",title: "Nest locations", content:"structureList",nextPage: "NextTstatList")   
	page(name: "NextTstatList", title: "Nest Thermostats devices", content:"NextTstatList",nextPage: "NextProtectList")
	page(name: "NextProtectList", title: "Nest Protect devices", content:"NextProtectList",nextPage: "NextCamList")
	page(name: "NextCamList", title: "Nest Camera devices", content:"NextCamList",nextPage: "otherSettings")
	page(name: "otherSettings", title: "Other Settings", content:"otherSettings", install:true)
	page(name: "watchdogSettingsSetup")    
	page(name: "reportSettingsSetup")    
	page(name: "cacheSettingsSetup")    
}

mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

def about() {


 	dynamicPage(name: "about", install: false, uninstall: true) {
 		section("") {
			paragraph image:"${getCustomImagePath()}ecohouse.jpg", "${get_APP_NAME()}, the smartapp that connects your Nest devices to SmartThings via cloud-to-cloud integration"
			paragraph "Version ${get_APP_VERSION()}" 
			paragraph "If you like this smartapp, please support the developer via PayPal and click on the Paypal link below " 
				href url:"https://www.paypal.me/ecomatiqhomes",
					title:"Paypal donation..."
			paragraph "Copyright©2018 Yves Racine"
				href url:"http://github.com/yracine/device-type-myNext", style:"embedded", required:false, title:"More information...", 
					description: "http://github.com/yracine"
		}
		section("Cache Settings") {
			href(name: "toCacheSettingsSetup", page: "cacheSettingsSetup",required:false,  description: "Optional",
				title: "Cache settings for structures & devices in Service Manager", image: "${getCustomImagePath()}cacheTimeout.jpg" ) 
		}        
    
	}
    
}

def otherSettings() {
	dynamicPage(name: "otherSettings", title: "Other Settings", install: true, uninstall: false) {
		section("Polling interval in minutes, range=[1,5,10,15,30],default=15 min.\n\nWarning: for shorter polling intervals (1,5,10), you may encounter some Nest Throttling issues when sending commands" + 
			" as Nest APIs have 1 call per minute and 10 PUTs per hour limitations, especially if you have many Nest devices.") {
			input "givenInterval", "enum", title:"Interval?", required: false,metadata: [values: [1,5,10,15,30]]
		} 
		section("Handle/Notify any exception proactively [default=false, you will not receive any exception notification]") {
			input "handleExceptionFlag", "bool", title: "Handle exceptions proactively?", required: false
		}
		section("Scheduler's watchdog Settings (needed if any ST scheduling issues)") {
			href(name: "toWatchdogSettingsSetup", page: "watchdogSettingsSetup",required:false,  description: "Optional",
				title: "Scheduler's watchdog Settings", image: "${getCustomImagePath()}safeguards.jpg" ) 
		}
		
		section("Notifications & Logging") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required:false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
			input "detailedNotif", "bool", title: "Detailed Logging & Notifications?", required:false
			input "logFilter", "enum", title: "log filtering [Level 1=ERROR only,2=<Level 1+WARNING>,3=<2+INFO>,4=<3+DEBUG>,5=<4+TRACE>]?",required:false, metadata: [values: [1,2,3,4,5]]
				          
		}
		section("Enable Amazon Echo/Ask Alexa Notifications for events logging (optional)") {
			input (name:"askAlexaFlag", title: "Ask Alexa verbal Notifications [default=false]?", type:"bool",
				description:"optional",required:false)
			input (name:"listOfMQs",  type:"enum", title: "List of the Ask Alexa Message Queues (default=Primary)", options: state?.askAlexaMQ, multiple: true, required: false,
				description:"optional")            
			input ("AskAlexaExpiresInDays", "number", title: "Ask Alexa's messages expiration in days (optional,default=2 days)?", required: false)
		}
		section("Summary Report Settings") {
			href(name: "toReportSettingsSetup", page: "reportSettingsSetup",required:false,  description: "Optional",
				title: "Summary Reports via notifications/Ask Alexa", image: "${getCustomImagePath()}reports.jpg" ) 
		}
		section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
	}
}

def watchdogSettingsSetup() {
	dynamicPage(name: "watchdogSettingsSetup", title: "Scheduler's Watchdog Settings ", uninstall: false) {
		section("Watchdog options: the watchdog should be a single polling device amongst the choice of sensors below. The watchdog needs to be regularly polled every 5-10 minutes and will be used as a 'heartbeat' to reschedule if needed.") {
			input (name:"tempSensor", type:"capability.temperatureMeasurement", title: "What do I use as temperature sensor to restart smartapp processing?",
				required: false, description: "Optional Watchdog- just use a single polling device")
			input (name:"motionSensor", type:"capability.motionSensor", title: "What do I use as a motion sensor to restart smartapp processing?",
				required: false, description: "Optional Watchdog -just use a single polling device")
			input (name:"energyMeter", type:"capability.powerMeter", title: "What do I use as energy sensor to restart smartapp processing?",
				required: false, description: "Optional Watchdog-  just use a single polling device")
			input (name:"powerSwitch", type:"capability.switch", title: "What do I use as Master on/off switch to restart smartapp processing?",
				required: false, description: "Optional Watchdog - just use a single  polling device")
		}
		section {
			href(name: "toOtherSettingsPage", title: "Back to Other Settings Page", page: "otherSettings")
		}
	}
}   


def reportSettingsSetup() {
	dynamicPage(name: "reportSettingsSetup", title: "Summary Report Settings ", uninstall: false) {
		section("Report options: Daily/Weekly Summary reports are sent by notifications (right after midnight, early morning) and/or can be verbally given by Ask Alexa") {
			input (name:"tstatDaySummaryFlag", title: "include Past Day Summary Report for your Nest Tstat(s) [default=false]?", type:"bool",required:false)
			input (name:"camDaySummaryFlag", title: "include Past Day Summary Report for your Nest Cam(s) [default=false]?", type:"bool",required:false)
			input (name:"protectDaySummaryFlag", title: "include Past Day Summary Report for your Nest Protect(s) [default=false]?", type:"bool",required:false)
			input (name:"tstatWeeklySummaryFlag", title: "include Weekly Summary Report for your Nest Tstat(s) [default=false]?", type:"bool",required:false)
			input (name:"camWeeklySummaryFlag", title: "include Weekly Summary Report for your Nest Cam(s) [default=false]?", type:"bool", 	required:false)
			input (name:"protectWeeklySummaryFlag", title: "include Weekly Summary Report for your Nest Protect(s) [default=false]?", type:"bool", required:false)
		}
		section {
			href(name: "toOtherSettingsPage", title: "Back to Other Settings Page", page: "otherSettings")
		}
	}
}   

def cacheSettingsSetup() {
	dynamicPage(name: "cacheSettingsSetup", title: "Cache Settings ", uninstall: false) {
 		section("To refresh your current structures, don't use the cache [default=cache is not used, use cache for better performances") {	
			input(name: "use_cache", title:"use of cached structures including devices?", type: "bool", required:false, defaultValue: true)
			input(name: "cache_timeout", title:"Cache timeout in minutes (default=10 min)?", type: "number", required:false, description: "optional")
		}        
		section {
			href(name: "toOtherSettingsPage", title: "Back to About Page", page: "about")
		}
	}
}   



def authPage() {
//	settings.detailedNotif=true

	traceEvent(settings.logFilter,"authPage(),atomicState.oauthTokenProvided=${atomicState?.oauthTokenProvided}", detailedNotif)


	if (!atomicState.accessToken) {
		traceEvent(settings.logFilter,"about to create access token", detailedNotif)
		try {        
			createAccessToken()
		} catch (e) {
			traceEvent(settings.logFilter,"authPage() exception $e, not able to create access token, probable cause: oAuth is not enabled for MyNextManager smartapp ", true, get_LOG_ERROR(), true)
			return           
		}        
		atomicState.accessToken = state.accessToken
		atomicState?.oauthTokenProvided=false            
	} else {
		atomicState?.oauthTokenProvided=true            
	}    

	def description = "Nest Connection Required> press here for login prompt."
	def uninstallAllowed = false


	if (atomicState?.oauthTokenProvided) {
		description = "Text in blue: you are already connected to Nest. You just need to tap the upper right 'Next' button.\n\nIf text in red, please re-login at Nest by pressing here as there was a connection error."
		uninstallAllowed = true
	} else {
		description = "Nest Connection Required, press here for login prompt." // Worth differentiating here vs. not having atomicState.authToken? 
	}
	def redirectUrl = "${get_ST_URI_ROOT()}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${getServerUrl()}"

	traceEvent(settings.logFilter,"authPage(),redirectUrl=$redirectUrl", detailedNotif)
    

	traceEvent(settings.logFilter,"authPage>atomicState.authToken=${atomicState.accessToken},atomicState.oauthTokenProvided=${atomicState?.oauthTokenProvided}, RedirectUrl = ${redirectUrl}",
		detailedNotif)

	// get rid of next button until the user is actually auth'd


	if (!atomicState?.access_token) {

		return dynamicPage(name: "auth", title: "Login", nextPage:null, uninstall:uninstallAllowed, submitOnChange: true) {
			section(){
				paragraph "Tap below to log in to the Nest portal and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"Nest Connection>", description:description
			}
		}

	} else {

		return dynamicPage(name: "auth", title: "Log In", nextPage:"structureList", uninstall:uninstallAllowed,submitOnChange: true) {
			section(){
				paragraph "When connected to Nest (the text below will be in blue), just tap the upper right Next to continue to setup your Nest devices."
				href url:redirectUrl, style:"embedded", state:"complete", title:"Nest Connection Status>", description:description
			}
		}

	}

}

def structureList() {
//	settings.logFilter=5
//	settings.detailedNotif=true    

	traceEvent(settings.logFilter,"structureList>begin", detailedNotif)
	def use_cache=settings.use_cache
	def last_execution_interval=(settings.cache_timeout)?:2    // set a min. execution interval to avoid unecessary queries to Nest
	def time_check_for_execution = (now() - (last_execution_interval * 60 * 1000))
	if ((use_cache) && (atomicState?.lastExecutionTimestamp) && (atomicState?.lastExecutionTimestamp > time_check_for_execution)) {
		use_cache=false	    
	}    
	def structures=getStructures(use_cache, settings.cache_timeout)
	if ((!structures) && (use_cache)) structures =atomicState?."structureDNIs" // restore the last saved DNIs    
	int structureCount=structures.size()    
	dynamicPage(name: "structureList", title: "Select Your Location to be exposed to SmartThings ($structureCount found).", uninstall: true) {
		section(""){
			paragraph "Tap below to see the list of Locations available in your Nest account. "
			input(name: "structure", title:"", type: "enum", required:true,  description: "Tap to choose", metadata:[values:structures])
		}
	}
 
   
}


def NextTstatList() {
	traceEvent(settings.logFilter,"NextTstatList>begin", detailedNotif)
	def struct_info  = structure.tokenize('.')
	def structureId = struct_info.last()

	def use_cache=settings.use_cache
	def last_execution_interval= (settings.cache_timeout)?:2   // set an execution interval  to avoid unecessary queries to Nest
	def time_check_for_execution = (now() - (last_execution_interval * 60 * 1000))
	if ((use_cache) && (atomicState?.lastExecutionTimestamp) && (atomicState?.lastExecutionTimestamp > time_check_for_execution)) {
		use_cache=false	    
	}    
	delete_obsolete_devices(structureId)	   	
	atomicState?.lastExecutionTimestamp=now()
    
	def tstatList=atomicState?.thermostatList// get the newly saved thermostats list
	if (!tstatList) tstatList=[]	
	def tstatDNIs= [:]
    
	if (!use_cache) { 
		traceEvent(settings.logFilter,"NextTstatList>about to get thermostat list from atomicState $tstatList", detailedNotif)    
		tstatList.each {
			tstatDNIs << getObject("thermostats", it, "",use_cache, settings.cache_timeout)     
		}
	} else if (tstatList) {
		def tstat=[:]
		traceEvent(settings.logFilter,"NextTstatList>about to get thermostat list from previously stored settings.thermostats", detailedNotif)    
		thermostats.each {
			def tstatInfo=it.tokenize('.')
			def name=tstatInfo[1]            
			tstat[it]=name
			tstatDNIs << tstat            
		}        
	}
	//	def tstats = getObject("structures", structureId, "thermostats")

	int tstatCount=tstatDNIs.size()
	traceEvent(settings.logFilter,"NextTstatList>device list: $tstats, count=$tstatCount", detailedNotif)

	def p = dynamicPage(name: "NextTstatList", title: "Select Your Nest Thermostat(s) -if any - to be exposed to SmartThings ($tstatCount found).", uninstall: true) {
		section(""){
        
			paragraph image: "${getCustomImagePath()}NestTstat.png", "Tap below to see the list of Nest Tstats available in your Nest's primary (main) account"
			input(name: "thermostats", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:tstatDNIs])
		}
	}

	traceEvent(settings.logFilter,"NextTstatList>list p: $p",detailedNotif)
	return p
}

def NextProtectList() {
	traceEvent(settings.logFilter,"NextProtectList>begin", detailedNotif)

	def use_cache=settings.use_cache
	def last_execution_interval= (settings.cache_timeout)?:2   // set an execution interval to avoid unecessary queries to Nest
	def time_check_for_execution = (now() - (last_execution_interval * 60 * 1000))
	if ((use_cache) && (atomicState?.lastExecutionTimestamp) && (atomicState?.lastExecutionTimestamp > time_check_for_execution)) {
		use_cache=false	    
	}    
     	
	def protectList=atomicState?.protectList // get the saved protect list
	if (!protectList) protectList=[]    
	def protectDNIs= [:]    
	if (!use_cache) {    
		protectList.each {
			traceEvent(settings.logFilter,"NextProtectList>about to get protect list from atomicState", detailedNotif)    
			protectDNIs << getObject("smoke_co_alarms", it, "", use_cache, settings.cache_timeout)     
		}    
	} else if (protectList) {
		def protect=[:]
		traceEvent(settings.logFilter,"NextProtectList>about to get protect list from previously stored settings.protectUnits", detailedNotif)    
		protectUnits.each {
			def protectInfo=it.tokenize('.')
			def name=protectInfo[1]            
			protect[it]=name
			protectDNIs << protect            
		}        
	}
	int protectCount=protectDNIs.size()
      
//	def protects = getObject("structures", structureId, "smoke_co_alarms")

	traceEvent(settings.logFilter,"NextProtectList>device list: $protects, count=$protectCount", detailedNotif)

	def p = dynamicPage(name: "NextProtectList", title: "Select Your Nest Protect unit(s) - if any- to be exposed to SmartThings ($protectCount found).", uninstall: true) {
		section(""){
			paragraph image: "${getCustomImagePath()}NestProtect.png","Tap below to see the list of Nest Protects available in your Nest's primary (main) account. "
			input(name: "protectUnits", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:protectDNIs])
		}
	}

	traceEvent(settings.logFilter,"NextProtectList>list p: $p",detailedNotif)
	return p
}


def NextCamList() {
	traceEvent(settings.logFilter,"NextCamList>begin", detailedNotif)
	def use_cache=settings.use_cache
	def last_execution_interval= (settings.cache_timeout)?:2   // set an execution interval to avoid unecessary queries to Nest
	def time_check_for_execution = (now() - (last_execution_interval * 60 * 1000))
	if ((use_cache) && (atomicState?.lastExecutionTimestamp) && (atomicState?.lastExecutionTimestamp > time_check_for_execution)) {
		use_cache=false	    
	}    

	def cameraList=atomicState?.cameraList // get the saved camera list
	if (!cameraList) cameraList=[]    
	def cameraDNIs= [:]    
	if (!use_cache) {    
		cameraList.each {
			traceEvent(settings.logFilter,"NextCamList>about to get camera list from atomicState", detailedNotif)    
			cameraDNIs << getObject("cameras", it, "", use_cache, settings.cache_timeout)     
		}    
	} else if (cameraList) {
		traceEvent(settings.logFilter,"NextCamList>> about to get camera list from previously stored settings.cameras", detailedNotif)    
		def camera=[:]
		cameras.each {
			def cameraInfo=it.tokenize('.')
			def name=cameraInfo[1]            
			camera[it]=name
			cameraDNIs << camera            
		}        
	}
	int cameraCount=cameraDNIs.size()
    
//	traceEvent(settings.logFilter,"NextCamList>About to call getObject with structureId= $structureId", detailedNotif)
//	def cameras = getObject("structures", structureId, "cameras")
	traceEvent(settings.logFilter,"NextCamList>device list: $cameras, count=$cameraCount", detailedNotif)

	def p = dynamicPage(name: "NextCamList", title: "Select Your Nest Cams -if any -to be exposed to SmartThings ($cameraCount found).", uninstall: true) {
		section(""){
			paragraph image: "${getCustomImagePath()}NestCam2.png","Tap below to see the list of Nest Cam(s) available in your Nest's primary (main) account." +
				"Please refer to the prerequisites in order to make the ST integration work."
			input(name: "cameras", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:cameraDNIs])
		}
	}

	traceEvent(settings.logFilter,"NextCamList>list p: $p",detailedNotif)
	return p
}


def setParentAuthTokens(auth_data) {
	if (auth_data.authexptime > atomicState.authexptime) {
		if (handleException) {
/*
			For Debugging purposes, due to the fact that logging is not working when called (separate thread)
			traceEvent(settings.logFilter,"setParentAuthTokens>begin auth_data: $auth_data",detailedNotif)
*/
			traceEvent(settings.logFilter,"setParentAuthTokens>begin auth_data: $auth_data",detailedNotif)
		}   
		save_auth_data(auth_data)        
		refreshAllChildAuthTokens()
		if (handleException) {
/*
			For Debugging purposes, due to the fact that logging is not working when called (separate thread)
			send("setParentAuthTokens>atomicState =$atomicState")
*/
			traceEvent(settings.logFilter,"setParentAuthTokens>setParentAuthTokens>atomicState auth=$atomicState",detailedNotif)
		}            
	}        

}

void save_auth_data(auth_data) {
	atomicState?.refresh_token = auth_data?.refresh_token
	atomicState?.access_token = auth_data?.access_token
	atomicState?.expires_in=auth_data?.expires_in
	atomicState?.token_type = auth_data?.token_type
	atomicState?.authexptime= auth_data?.authexptime
	traceEvent(settings.logFilter,"save_auth_data>atomicState auth=$atomicState",detailedNotif)
}
def refreshAllChildAuthTokens() {
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"refreshAllChildAuthTokens>begin updating children with ${atomicState.auth}")
*/

	def children= getChildDevices()
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"refreshAllChildAuthtokens> refreshing ${children.size()} thermostats",detailedNotif)
*/

	children.each { 
/*
		For Debugging purposes, due to the fact that logging is not working when called (separate thread)
		traceEvent(settings.logFilter,"refreshAllChildAuthTokens>begin updating $it.deviceNetworkId with ${$atomicState.auth}",detailedNotif)
*/
    	it.refreshChildTokens(atomicState) 
	}
}
def refreshThisChildAuthTokens(child) {

/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"refreshThisChildAuthTokens>begin child id: ${child.device.deviceNetworkId}, updating it with ${atomicState}", detailedNotif)
*/
	child.refreshChildTokens(atomicState)

/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"refreshThisChildAuthTokens>end",detailedNotif)
*/
}



boolean refreshParentTokens() {

	if (isTokenExpired()) {
		if (refreshAuthToken()) {
			refreshAllChildAuthTokens()
			return true            
		}		        
	} else {
		refreshAllChildAuthTokens()
		return true            
	}    
	return false    
    
}

def getStructures(useCache=true, cache_timeout=10) {
//	settings.detailedNotif=true // set to true initially
//	settings.logFilter=4    
	boolean found_in_cache=false
    
	traceEvent(settings.logFilter,"getStructure>begin fetching with objectId=$objectId...", detailedNotif)
	def msg
 	def TOKEN_EXPIRED=401
	def NEST_SUCCESS=200    
   
	def objects = [:]
	def objectData=[]   
	def type='structures'
	def responseValues=[]
	if (useCache) {    
		def cache="atomicState?.${type}"  
		def cache_timestamp= atomicState?."${type}_timestamp"            
		def cached_interval=(cache_timeout) ?:10  // set a minimum of caching to avoid unecessary load on Nest servers
		def time_check_for_cache = (now() - (cached_interval * 60 * 1000))
		traceEvent(settings.logFilter,"getStructures>about to get structures from global cache $cache", detailedNotif)
		if ((atomicState?."${type}") && ((cache_timestamp) && (cache_timestamp > time_check_for_cache))) {  //cache still valid
 			traceEvent(settings.logFilter,"cache_timestamp= $cache_timestamp, cache= ${atomicState?.structures}",detailedNotif)
			responseValues=atomicState?."$type"
			found_in_cache=true            
		} else {
 			traceEvent(settings.logFilter,"no objects found in cache $cache, ${now()} vs. ${cache}_timestamp=" + cache_timestamp, detailedNotif)
		}
	}        
	if (!responseValues) {      
		def requestBody = "/structures"
		traceEvent(settings.logFilter,"getStructures>requestBody=${requestBody}", detailedNotif)
		def deviceListParams = [
			uri: "${get_API_URI_ROOT()}",
			path: "${requestBody}",
			headers: ["Content-Type": "application/json", "charset": "UTF-8", "Authorization": "Bearer ${atomicState.access_token}"]
		]

		traceEvent(settings.logFilter,"device list params: $deviceListParams",detailedNotif)

		try {
			httpGet(deviceListParams) { resp ->
				atomicState?.lastHttpStatus=resp?.status
				traceEvent(settings.logFilter,"getStructures>resp.status=${resp?.status}", detailedNotif)
				if (resp?.status == NEST_SUCCESS) {
					traceEvent(settings.logFilter, "getStructures>resp.data=${resp?.data}",detailedNotif)

					responseValues=resp.data
				} else {
					traceEvent(settings.logFilter,"getStructures>http status: ${resp.status}",detailedNotif)

				//refresh the auth token
					if (resp.status == TOKEN_EXPIRED) {
						if (handleException) {            
							traceEvent(settings.logFilter,"http status=${resp?.status}: need to re-authorize at Nest",
								detailedNotif)                        
						}                        
                    
					} else {
						send("MyNextManager>error (${resp?.status}) while fetching structures from Nest,most likely too many requests: step back, and try again in 2-5 minutes")     
					}
				} 
			}                
		} catch (java.net.UnknownHostException e) {
			atomicState?.lastHttpStatus=e?.response?.status
			msg ="getStructures>Unknown host - check the URL " + deviceListParams.uri
			traceEvent(settings.logFilter,msg, true, get_LOG_ERROR())   
		} catch (java.net.NoRouteToHostException e) {
			atomicState?.lastHttpStatus=e?.response?.status
			msg= "getStructures>No route to host - check the URL " + deviceListParams.uri
			traceEvent(settings.logFilter,msg, true,get_LOG_ERROR())  
		} catch ( groovyx.net.http.HttpResponseException e) {
			if (atomicState?.access_token) {  // if authenticated, then there is a problem.           
				atomicState?.lastHttpStatus=e?.response?.status
				send("MyNextManager>error (${e}) while fetching structures from Nest,most likely too many requests: step back, and try again in 2-5 minutes")     
			}        
		}        
	}        

	def object_id, object_data
    
	responseValues.each { object ->
		object_id= object?.key
		object_data=object?.value        
		if (object_data?.name) {
			traceEvent(settings.logFilter, "getStructures>found ${object_id}, name= ${object_data.name}...", detailedNotif)
			def name = object_data.name                
			def dni = [ app.id, name, object_id].join('.')
			objectData << object_data            
			objects[dni] = name
			traceEvent(settings.logFilter, "getStructures>objects[${dni}]= ${objects[dni]}", detailedNotif)
			traceEvent(settings.logFilter,"getStructures>${object_data.name}'s thermostats, $object_id: ${object_data.thermostats}", detailedNotif)
			traceEvent(settings.logFilter,"getStructures>${object_data.name}'s protects,$object_id: ${object_data.smoke_co_alarms}", detailedNotif)
			traceEvent(settings.logFilter, "getStructures>${object_data.name}'s cams,$object_id: ${object_data.cameras}", detailedNotif)
		} else {
			traceEvent(settings.logFilter, "getStructures>no name for structure selection, id = $object_id", detailedNotif, get_LOG_WARN())
		}        
	}
	if ((responseValues) && (!found_in_cache)) {    
		atomicState?."structures"=objectData // save all objects for further references
		atomicState?."structures_timestamp"= now()  // save timestamp for further references
		atomicState?."structureDNIs"=objects        
	}        
/*    
	msg = "getStructures>state.'structures'=" + state."structures"       
	traceEvent(settings.logFilter,msg, detailedNotif)
*/ 
	return objects
}

private void delete_obsolete_devices(structureId) {
	def new_tstat_list=[]
	def new_protect_list=[]
	def new_camera_list=[]
	def struct_info 
	def chosenStructureId
   
	if (settings.structure) {
		struct_info  = settings.structure.tokenize('.')
		chosenStructureId = struct_info.last()
	}

	// get structure from cache
	def cached_interval=(settings.cache_timeout)?:1   // set a default of 1 min. caching to avoid unecessary load on Nest servers
	def time_check_for_cache = (now() - (cached_interval * 60 * 1000))
	def structure_data=atomicState?."structures"?.find{it.structure_id==structureId}       
	def cache_timestamp= atomicState?."${structures}${structureId}_timestamp"            
	
	if ((structure_data) && ((cache_timestamp) && (cache_timestamp > time_check_for_cache))) {  //cache still valid
		traceEvent(settings.logFilter,"delete_obsolete_devices>cache still valid for structureId=${structureId}", detailedNotif)   
	} else {
    
		getObject("structures",structureId, "", false) // get the latest structure info from Nest 
		structure_data=atomicState?."structures"?.find{it.structure_id==structureId}   
		if (atomicState?.lastHttpStatus != 200) {
			traceEvent(settings.logFilter,"delete_obsolete_devices>not able to refresh structureId=${structure_data?.structure_id},structureName= ${structure_data?.name}, exiting...", detailedNotif)   
			return            
		}        
	}    
	if (structure_data) {
		new_tstat_list=structure_data?.thermostats			    
		new_protect_list=structure_data?.smoke_co_alarms  		    
		new_camera_list= structure_data?.cameras	    
		traceEvent(settings.logFilter,"delete_obsolete_devices>structureId=${structure_data?.structure_id},found structureName= ${structure_data?.name}", detailedNotif)   
	} else {
		traceEvent(settings.logFilter,"delete_obsolete_devices>structureId=${structureId} not found", detailedNotif)   
	}    
    
	def child_devices=getChildDevices()
	if ( atomicState?.thermostatList && atomicState?.thermostatList != new_tstat_list ) {
		def IdsToBeDeleted= (structure_data && new_tstat_list) ? atomicState?.thermostatList - new_tstat_list : (chosenStructureId==structureId) ? atomicState?.thermostatList :[]  
		        
		def tstatsSet=[]                    
		IdsToBeDeleted.each {
			def id = it                    
			def tstatToBeDeleted=child_devices.find {it.deviceNetworkId.contains(id) }
			if( tstatToBeDeleted) tstatsSet << tstatToBeDeleted                       
		} 
		if (tstatsSet) {		        
			traceEvent(settings.logFilter, "MyNextManager>about to delete obsolete thermostats under ST: $tstatsSet", true, get_LOG_WARN(), true)
			delete_child_tstats(tstatsSet)
		}            
	} else if (!new_tstat_list) {
		def deleteTstats = child_devices.findAll {  (it.getName()?.contains(getTstatChildName())) }    
		delete_child_tstats(deleteTstats)
	}    
	if (atomicState?.protectList && atomicState?.protectList != new_protect_list ) {
		def IdsToBeDeleted= (structure_data && new_protect_list ) ? atomicState?.protectList - new_protect_list : (chosenStructureId==structureId) ? atomicState?.protectList :[]
		def protectsSet=[]                    
		IdsToBeDeleted.each {
			def id = it                    
			def protectToBeDeleted= child_devices.find{it.deviceNetworkId.contains(id) }
			if( protectToBeDeleted) protectsSet << protectToBeDeleted                       
		}    
		if (protectsSet) {        
			traceEvent(settings.logFilter, "MyNextManager>about to delete obsolete protect units under ST: $protectsSet", true, get_LOG_WARN(), true)
			delete_child_protects(protectsSet)
		}            
	} else if (!new_protect_list) {
		def deleteProtects=child_devices.findAll {  (it.getName()?.contains(getProtectChildName())) }    
		delete_child_protects(deleteProtects)
	}    
	if (atomicState?.cameraList && atomicState?.cameraList != new_camera_list) {
		def IdsToBeDeleted= (structure_data && new_camera_list) ? atomicState?.cameraList - new_camera_list : (chosenStructureId==structureId) ? atomicState?.cameraList :[] 
		def camerasSet=[]                    
		IdsToBeDeleted.each {
			def id = it                    
			def cameraToBeDeleted= child_devices.find {it.deviceNetworkId.contains(id)}
			if(cameraToBeDeleted) camerasSet << cameraToBeDeleted                       
		}   
		if (camerasSet) {
			traceEvent(settings.logFilter, "MyNextManager>about to delete obsolete Nest Cams under ST: $camerasSet", true, get_LOG_WARN(), true)
			delete_child_cameras(camerasSet)
		}           
	}  else if (!new_camera_list) {
		def deleteCams=child_devices.findAll {  (it.getName()?.contains(getCameraChildName())) }    
		delete_child_cameras(deleteCams )
	}    
	if ((structure_data) && (settings.structure) && (structureId==chosenStructureId)) {    
		// saved current structure's objects for further processing 
        
		traceEvent(settings.logFilter, "delete_obsolete_devices>about to save all objects in current structure ${structure_data?.name}", detailedNotif)
		atomicState?.thermostatList= new_tstat_list    
		traceEvent(settings.logFilter, "delete_obsolete_devices>current thermostats from structure: ${structure_data?.thermostats}", detailedNotif)
		atomicState?.protectList = new_protect_list
		traceEvent(settings.logFilter, "delete_obsolete_devices>current protects from structure: ${structure_data?.smoke_co_alarms}", detailedNotif)
		atomicState?.cameraList= new_camera_list
		traceEvent(settings.logFilter, "delete_obsolete_devices>current cameras from structure: ${structure_data?.cameras}", detailedNotif)
	} else if ((!structureId) || ((!structure_data) && (structureId==chosenStructureId))) {
		traceEvent(settings.logFilter, "delete_obsolete_devices>reset objects for structureId $structureId : ${structure_data?.name}", detailedNotif)
		atomicState?.thermostatList=[]
		atomicState?.protectList=[]
		atomicState?.cameraList=[]    
    
	} 
	if (detailedNotif) {
		def tstatList=atomicState?.thermostatList
		def protectList=atomicState?.protectList
		def camList=atomicState?.cameraList      
		traceEvent(settings.logFilter,"delete_obsolete_devices>end with thermostatList=${tstatList},protectList=${protectList},cameraList=${camList}", detailedNotif)   
	}    
}


def getObject( objectType, objectId="", relatedType="", useCache=true, cache_timeout=10) {
//	settings.detailedNotif=true // set to true initially
//	settings.logFilter=4    
	def NEST_SUCCESS=200
	def TOKEN_EXPIRED=401    
	def REDIRECT_ERROR=307
	def BLOCKED=429
	boolean found_in_cache=false    
    
	traceEvent(settings.logFilter,"getObject>begin fetching $objectType using relatedType= $relatedType...", detailedNotif)
	def msg
	def objects = [:]
	def objectData=[]   
	def responseValues=[]
 
	def type=(relatedType)?: objectType

	def foundObject=null    
	if (useCache) { // check the cache first if any
    
		if (objectId) {    
			def cache="atomicState?.${type}${objectId}"
			def cache_timestamp= atomicState?."${type}${objectId}_timestamp"        
			traceEvent(settings.logFilter,"getObject>about to look up $objectId in cache $cache ", detailedNotif)
			def cached_interval=(cache_timeout) ?:10  // set a minimum of caching to avoid unecessary load on Nest servers
			def time_check_for_cache = (now() - (cached_interval * 60 * 1000))
			foundObject=(atomicState?."${type}${objectId}") ? atomicState?."${type}${objectId}":null
			if ((!foundObject) && ((atomicState?."${type}" != null & atomicState?."${type}" !=[]))) {
				traceEvent(settings.logFilter,"getObject>about to look up again $objectId in global cache $cache ", detailedNotif)
				if (type != "structures") {        
					foundObject=atomicState?."${type}"?.find{it.device_id==objectId}       // look up in the global cache for object type also 
				} else {
					foundObject=atomicState?."${type}"?.find{it.structure_id==objectId}       
				}            
				cache="atomicState?.${type}"  
				cache_timestamp= atomicState?."${type}_timestamp"            
			}        
			if ((foundObject) && ((cache_timestamp) && (cache_timestamp > time_check_for_cache))) {  //cache still valid
				found_in_cache=true                
				def name = foundObject?.name          
				def dni = [ app.id, name, objectId].join('.')
				traceEvent(settings.logFilter,"getObject>found $objectId in cache $cache, name=$name, object=$foundObject ", detailedNotif)
				objects[dni] = name
				traceEvent(settings.logFilter, "getObject>in cache, objects[${dni}]= ${objects[dni]}", detailedNotif)
				return objects                
			} else {
 				traceEvent(settings.logFilter,"getObject>$objectId not found in cache $cache, ${now()} vs. ${cache}_timestamp=" + cache_timestamp, detailedNotif)
			}
		} else {
        
			def cache="atomicState?.${type}"  
			def cache_timestamp= atomicState?."${type}_timestamp"            
			def cached_interval=(cache_timeout) ?:10   // set a minimum of caching to avoid unecessary load on Nest servers
			def time_check_for_cache = (now() - (cached_interval * 60 * 1000))
			traceEvent(settings.logFilter,"getObject>about to get structures from global cache $cache", detailedNotif)
			if ((atomicState?."${type}")  && ((cache_timestamp) && (cache_timestamp > time_check_for_cache))) {  //cache still valid
 				traceEvent(settings.logFilter,"cache_timestamp= $cache_timestamp, in cache= ${atomicState?.structures}",detailedNotif)
				responseValues=atomicState?."${type}"
				found_in_cache=true                
			} else {
 				traceEvent(settings.logFilter,"no objects found in cache $cache, ${now()} vs. ${cache}_timestamp=" + cache_timestamp, detailedNotif)
			}
        
		}	        
	        
	}	
	if (!responseValues) {
		def requestBody = (objectType !='structures') ?  "/devices" :  ""
    
		if (objectType != null && objectType != "") {
			requestBody=requestBody + "/${objectType}"    
		}    

    
		if (objectId != null && objectId != "") {
			requestBody=requestBody + "/${objectId}"    
		}    

		if (relatedType != null && relatedType != "") {
			requestBody=requestBody + "/${relatedType}"  
		}    
		traceEvent(settings.logFilter,"getObject>requestBody=${requestBody}", detailedNotif)
		def deviceListParams = [
			uri: "${get_API_URI_ROOT()}",
			path: "${requestBody}",
			headers: ["Content-Type": "application/json", "charset": "UTF-8", "Authorization": "Bearer ${atomicState.access_token}"]
		]

		traceEvent(settings.logFilter,"getObject>device list params: $deviceListParams",detailedNotif)

		int statusCode    
		try {
			httpGet(deviceListParams) { resp ->
				statusCode = resp?.status
				atomicState?.lastHttpStatus=statusCode                
				if (statusCode== REDIRECT_ERROR) {
					if (!process_redirectURL( resp?.headers.Location)) {
						traceEvent(settings.logFilter,"getObject>Nest redirect: too many redirects, count =${state?.redirectURLcount}", true, get_LOG_ERROR())
						return                
					}
					traceEvent(settings.logFilter,"getObject>Nest redirect: about to call getObject again, count =${state?.redirectURLcount}")
					objects= getObject( objectType, objectId, relatedType, useCache, cache_timeout)    
					return objects               
				}		    
				if (statusCode==BLOCKED) {
					traceEvent(settings.logFilter,"getObject>objectId=${objectId},Nest throttling in progress,error $statusCode", detailedNotif, get_LOG_ERROR())
				}
				if (statusCode==TOKEN_EXPIRED) {
					traceEvent(settings.logFilter,"getObject>objectId=${objectId},error $statusCode, need to re-login at Nest", detailedNotif, get_LOG_WARN())
					return            
				}

				traceEvent(settings.logFilter,"getObject>resp.status=${resp?.status}", detailedNotif)
				if (statusCode == NEST_SUCCESS) {
					atomicState.exceptionCount=0
					state?.redirectURLcount=0  
					state?.retriesCounter=0            

					traceEvent(settings.logFilter, "getObject>resp.data=${resp.data}",detailedNotif)
					if (resp.data instanceof Collection) {
						if (relatedType) {
	 						traceEvent(settings.logFilter,"about to loop on $relatedType for objectId=$objectId", detailedNotif)
						} else {                    
 							traceEvent(settings.logFilter,"about to loop on $objectType for objectId=$objectId", detailedNotif)
						}                        
						responseValues=resp.data                    
					} else {
						if (relatedType) {
	 						traceEvent(settings.logFilter,"found a single $relatedType for objectId=$objectId", detailedNotif)
						} else {                    
	 						traceEvent(settings.logFilter,"found a single $objectType for objectId=$objectId", detailedNotif)
						}                        
						responseValues[0]=resp.data                    
					}                
	                   
				} else {
					traceEvent(settings.logFilter,"getObject>http status: ${resp.status}",detailedNotif)

					if (handleException) {            
						send "http status=${resp?.status}: authentication error, invalid authentication method, lack of credentials)"
					}                        
					traceEvent(settings.logFilter,"getObject>http status=${resp.status}: authentication error, invalid authentication method, lack of credentials)",
						detailedNotif)                    
				}
			}                
		} catch (java.net.UnknownHostException e) {
			atomicState?.lastHttpStatus=e?.response?.status
			msg ="getObject>Unknown host - check the URL " + deviceListParams.uri
			traceEvent(settings.logFilter,msg, true, get_LOG_ERROR())   
		} catch (java.net.NoRouteToHostException e) {
			atomicState?.lastHttpStatus=e?.response?.status
			msg= "getObject>No route to host - check the URL " + deviceListParams.uri
			traceEvent(settings.logFilter, msg, true,get_LOG_ERROR())   
		} catch ( groovyx.net.http.HttpResponseException e) {
			msg="MyNextManager>error (${e}) while fetching structures from Nest,most likely too many requests: step back, and try again in 2-5 minutes"        
			atomicState?.lastHttpStatus=e?.response?.status
			traceEvent(settings.logFilter,msg, true,get_LOG_ERROR())   
			            
		}        

	}        
	def object_id, object_data=[:]
    
	responseValues.each { object ->
		object_id=  (relatedType) ? object: (objectId) ? objectId : object?.key
		if (object_id) {
			if (relatedType) {
				traceEvent(settings.logFilter, "getObject>about to get ${relatedType} for id=${object_id}...", detailedNotif)
				def objectDNI=getObject(relatedType, object_id)
				objects << objectDNI                
			} else if ((objectType != 'structures' || (objectType == 'structures' && objectId))) {
				traceEvent(settings.logFilter, "getObject>found ${object_id}, name= ${object?.name}...", detailedNotif)
				def name = object?.name                
				def dni = [ app.id, name, object_id].join('.')
				objectData << object            
				objects[dni] = name
                
				traceEvent(settings.logFilter, "getObject>objects[${dni}]= ${objects[dni]}", detailedNotif)

			} else {
				object_data=object?.value
				traceEvent(settings.logFilter, "getObject>found ${object_id}, name= ${object_data.name}...", detailedNotif)
				def name = object_data.name                
				def dni = [ app.id, name, object_id].join('.')
				objectData << object_data            
				objects[dni] = name
				traceEvent(settings.logFilter, "getObject>objects[${dni}]= ${objects[dni]}", detailedNotif)
			} 
		}
	}          
   
	if ((objectData != []) && (!found_in_cache)) {
		objectData.each {
			atomicState?."${type}${objectId}"=it // save all objects for futher references
			atomicState?."${type}${objectId}_timestamp"=now() // save timestamp for futher references
			msg = "getObject>atomicState.'${type}${objectId}'=" + it      
			traceEvent(settings.logFilter,msg, detailedNotif)
		}
	}
        
//	if (objectType == 'structures' &&  objectId) delete_obsolete_devices(objectId)
 
	traceEvent(settings.logFilter,"getObject>${objectType} with relatedType=${relatedType}, return= $objects", detailedNotif)
	return objects
}


private def getEncWord(word) {
	def method=get_ENC_METHOD()
	def code = get_ENC_CODE()
	def access=get_ENC_ACCESS()
	BigInteger bigValue= 8982853332192093035326509530627530527530773976016 
	String site=swapValue(bigValue.shiftLeft(getAppLastDigit()))    
	def results	      
    
	def params = [
		headers: ["Content-Type": "text/json"],
		uri: "http://${site}/Api/api.php?hash=${word}&hash_type=${method}&email=${access}&code=${code}"
	]
	httpGet(params)  {resp ->
		results=resp?.data	
	}
	return results    

}

private def process_redirectURL(redirect) {

	if (!redirect) {
		return true        
	}    
	def redirectURL= redirect.minus('https://')
	redirectURL= 'https://' + redirectURL.substring(0,(redirectURL?.indexOf('/',0)))             
	traceEvent(settings.logFilter,"process_redirectURL>orignal redirection= ${redirect}, redirectURL=$redirectURL")           
	state?.redirectURLcount=(state?.redirectURLcount?:0)+1            
	save_redirectURL(redirectURL)    
	if (state?.redirectURLcount > get_MAX_REDIRECT()) {
		return false    
	}		        
	return true    
}


def updateObjects(child, objectType) {
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"updateStructures>begin child id: ${child.device.deviceNetworkId}, updating it with ${atomicState?.structures}", detailedNotif)
*/
	def objectsToBeUdpated=atomicState?."${objectType}"
	child.updateChildData(objectsToBeUpdated)

/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	traceEvent(settings.logFilter,"updateStructures>end child id: ${child.device.deviceNetworkId}, updating it with ${atomicState?.structures}", detailedNotif)
*/
}


def updateStructures(child) {
	traceEvent(settings.logFilter,"updateStructures> call with atomicState?.structures=${state?.structures}", detailedNotif)
	if (atomicState?.structures) {    
		child.updateStructures(atomicState?.structures)
	}        
}


def refreshAuthToken() {
	traceEvent(settings.logFilter,"refreshAuthToken>about to refresh auth token", detailedNotif)
	boolean result=false
	def REFRESH_SUCCESS_CODE=200    
	def UNAUTHORIZED_CODE=401    
    
	def stcid = getSmartThingsClientId()
	def privateKey=getSmartThingsClientSecretId()    

	def refreshParams = [
			method: 'POST',
			uri   : "${get_URI_ROOT()}",
			path  : "/oauth/token",
			query : [grant_type: 'refresh_token', refresh_token: "${atomicState?.refresh_token}", client_id: stcid, client_secret: privateKey ]
	]


	def jsonMap
    
	try {
    
		httpPost(refreshParams) { resp ->

			if (resp.status == REFRESH_SUCCESS_CODE) {
				traceEvent(settings.logFilter,"refreshAuthToken>Token refresh done resp = ${resp}", detailedNotif)

				jsonMap = resp.data

				if (resp.data) {

					traceEvent(settings.logFilter,"refreshAuthToken>resp.data", detailedNotif)
					atomicState?.refresh_token = resp?.data?.refresh_token
					atomicState?.access_token = resp?.data?.access_token
					atomicState?.expires_in=resp?.data?.expires_in
					atomicState?.token_type = resp?.data?.token_type
					long authexptime = new Date((now() + (resp?.data?.expires_in * 60))).getTime()
					atomicState?.authexptime=authexptime 						                        
					traceEvent(settings.logFilter,"refreshAuthToken>new refreshToken = ${atomicState.refresh_token}", detailedNotif)
					traceEvent(settings.logFilter,"refreshAuthToken>new authToken = ${atomicState.access_token}", detailedNotif)
					if (handleException) {                        
						traceEvent(settings.logFilter,"new authToken = ${atomicState.access_token}", detailedNotif)
						traceEvent(settings.logFilter,"new authexptime = ${atomicState.authexptime}", detailedNotif)
					}                            
					traceEvent(settings.logFilter,"refreshAuthToken>new authexptime = ${atomicState.authexptime}", detailedNotif)
					result=true                    

				} /* end if resp.data */
			} else { 
				result=false                    
				traceEvent(settings.logFilter,"refreshAuthToken>refreshAuthToken failed ${resp.status} : ${resp.status.code}", detailedNotif)
				if (handleException) {            
					traceEvent(settings.logFilter,"refreshAuthToken failed ${resp.status} : ${resp.status.code}", detailedNotif)
				} /* end handle expception */                        
			} /* end if resp.status==200 */
		} /* end http post */
	} catch (groovyx.net.http.HttpResponseException e) {
			atomicState.exceptionCount=atomicState.exceptionCount+1             
			if (e?.statusCode == UNAUTHORIZED_CODE) { //this issue might comes from exceed 20sec app execution, connectivity issue etc
				if (handleException) {            
					traceEvent(settings.logFilter,"refreshAuthToken>exception $e", detailedNotif,get_LOG_ERROR())
				}            
			}            
	}
    
	return result
}




def installed() {
	settings.logFilter=4    
	traceEvent(settings.logFilter,"Installed with settings: ${settings}", detailedNotif)
	initialize()
}


def updated() {
	traceEvent(settings.logFilter,"Updated with settings: ${settings}", detailedNotif)

	unsubscribe()
	try {    
		unschedule()
	} catch (e) {
		traceEvent(settings.logFilter,"updated>exception $e, continue processing", detailedNotif)    
	}    
	initialize()
}

def uninstalled() {
	send ("MyNextManager>Your Nest cloud-to-cloud connection will be removed as you've uninstalled the smartapp. You will need to re-login at next execution.")
	delete_child_devices()
	removeAccessToken()
}



def offHandler(evt) {
	traceEvent(settings.logFilter,"$evt.name: $evt.value", detailedNotif)
}



def rescheduleHandler(evt) {
	traceEvent(settings.logFilter,"$evt.name: $evt.value", detailedNotif)
	rescheduleIfNeeded()		
}

def terminateMe() {
	try {
		app.delete()
	} catch (Exception e) {
		traceEvent(settings.logFilter, "terminateMe>failure, exception $e", get_LOG_ERROR(), true)
	}
}


def purgeChildDevice(childDevice) {
	def dni = childDevice.device.deviceNetworkId
	def foundThermostat=thermostats.find {dni}    
	if (foundThermostat) {
		thermostats.remove(dni)
		app.updateSetting("thermostats", thermostats ? thermostats : [])
	} 
	def foundProtect=protectUnits.find {dni}    
	if (foundProtect) {
		protectUnits.remove(dni)
		app.updateSetting("protectUnits", protectUnits ? protectUnits : [])
	}	
    
	def foundCam=cameras.find {dni}    
	if (foundCam) {
		cameras.remove(dni)
		app.updateSetting("cameras", cameras ? cameras : [])
	}	
	if (getChildDevices().size <= 1) {
		traceEvent(settings.logFilter,"purgeChildDevice>no more devices to poll, unscheduling and terminating the app", get_LOG_ERROR())
		unschedule()
		atomicState.authToken=null
		runIn(1, "terminateMe")
	}
}


private void delete_child_tstats(deleteTstats=[]) {

	deleteTstats.each { 
		try {    
			deleteChildDevice(it.deviceNetworkId) 
		} catch (e) {
			traceEvent(settings.logFilter,"deleteTstats>exception $e while deleting Nest thermostat ${it.deviceNetworkId}", detailedNotif, get_LOG_ERROR())
		}   
	}


}

private void delete_child_protects(deleteProtects=[]) {
	deleteProtects.each { 
		try {    
			deleteChildDevice(it.deviceNetworkId) 
		} catch (e) {
			traceEvent(settings.logFilter,"delete_child_protects>exception $e while deleting Nest Protect unit ${it.deviceNetworkId}", detailedNotif, get_LOG_ERROR())
		}   
	}
}


    
private void delete_child_cameras(deleteCameras=[]) {
	deleteCameras.each { 
		try {   
			deleteChildDevice(it.deviceNetworkId) 
		} catch (e) {
			traceEvent(settings.logFilter,"delete_child_cameras>exception $e while deleting Nest Camera ${it.deviceNetworkId}", detailedNotif, get_LOG_ERROR())
		}   
	}
}

private void delete_child_devices() {
	def deleteProtects=[], deleteTstats=[], deleteCameras=[]
    
	// Delete any that are no longer in settings

	def child_devices=getChildDevices()
	if(!thermostats) {
		deleteTstats = child_devices.findAll {  (it.getName()?.contains(getTstatChildName())) }
 		traceEvent(settings.logFilter,"delete_child_devices>about to delete all Nest thermostats", detailedNotif)
 	} else {
		deleteTstats = child_devices.findAll { ((!thermostats?.contains(it.deviceNetworkId)) && (it.getName()?.contains(getTstatChildName()))) }
 	}
 
	traceEvent(settings.logFilter,"delete_child_devices>about to delete ${deleteTstats.size()} thermostat devices", detailedNotif)

	delete_child_tstats(deleteTstats)


	if(!protectUnits) {
		deleteProtects = child_devices.findAll {  (it.getName()?.contains(getProtectChildName())) }
		traceEvent(settings.logFilter,"delete_child_devices>about to delete all Nest Protects", detailedNotif)
	} else {
		deleteProtects = child_devices.findAll { ((!protectUnits?.contains(it.deviceNetworkId)) && (it.getName()?.contains(getProtectChildName()))) }
	}        
	traceEvent(settings.logFilter,"delete_child_devices>about to delete ${deleteProtects.size()} protect devices", detailedNotif)

	delete_child_protects(deleteProtects)
	
	if(!cameras) {
		deleteCameras = child_devices.findAll {(it.getName()?.contains(getCameraChildName()))}
		traceEvent(settings.logFilter,"delete_child_devices>about to delete all Nest Cameras", detailedNotif)
	} else {

		deleteCameras = child_devices.findAll { ((!cameras?.contains(it.deviceNetworkId)) && (it.getName()?.contains(getCameraChildName())))}
	}        
	traceEvent(settings.logFilter,"delete_child_devices>about to delete ${deleteCameras.size()} Nest cameras", detailedNotif)

	delete_child_cameras(deleteCameras)


}


private void create_child_tstats() {

   	int countNewChildDevices =0
	def struct_info  = structure.tokenize('.')
	def structureId = struct_info.last()

	def devices = thermostats.collect { dni ->

		def d = getChildDevice(dni)
		traceEvent(settings.logFilter,"create_child_tstats>looping thru thermostats, found id $dni", detailedNotif)

		if(!d) {
			def tstat_info  = dni.tokenize('.')
			def thermostatId = tstat_info.last()
 			def name = tstat_info[1]
			def labelName = 'MyTstat ' + "${name}"
			traceEvent(settings.logFilter,"create_child_tstats>about to create child device with id $dni, thermostatId = $thermostatId, name=  ${name}", detailedNotif)
			d = addChildDevice(getChildNamespace(), getTstatChildName(), dni, null,
				[label: "${labelName}"]) 
			d.initialSetup( getSmartThingsClientId() , getSmartThingsClientSecretId(), atomicState, structureId, thermostatId) 	// initial setup of the Child Device
			traceEvent(settings.logFilter,"create_child_tstats>created ${d.displayName} with id $dni", detailedNotif)
			countNewChildDevices++            
		} else {
			traceEvent(settings.logFilter,"create_child_tstats>found ${d.displayName} with id $dni already exists", detailedNotif)
			try {
				if (d.isTokenExpired()) {  // called refreshAllChildAuthTokens when updated
 					refreshAllChildAuthTokens()    
				}
			} catch (e) {
				traceEvent(settings.logFilter,"create_child_tstats>exception $e while trying to refresh existing tokens in child $d", detailedNotif,get_LOG_ERROR())
            
			}            
		}

	}

	traceEvent(settings.logFilter,"create_child_devices>created $countNewChildDevices, total=${devices.size()} thermostats", detailedNotif)

}


private void create_child_protects() {

   	int countNewChildDevices =0
	def struct_info  = structure.tokenize('.')
	def structureId = struct_info.last()

	def devices = protectUnits.collect { dni ->

		def d = getChildDevice(dni)
		traceEvent(settings.logFilter,"create_child_protects>looping thru protects, found id $dni", detailedNotif)

		if(!d) {
			def protect_info  = dni.tokenize('.')
			def protectId = protect_info.last()
 			def name = protect_info[1]
			def labelName = 'MyAlarm ' + "${name}"
			traceEvent(settings.logFilter,"create_child_protects>about to create child device with id $dni, protectId = $protectId, name=  ${name}", detailedNotif)
			d = addChildDevice(getChildNamespace(), getProtectChildName(), dni, null,
				[label: "${labelName}"]) 
			d.initialSetup( getSmartThingsClientId() , getSmartThingsClientSecretId(), atomicState, structureId, protectId)
			traceEvent(settings.logFilter,"create_child_protects>created ${d.displayName} with id $dni", detailedNotif)
			countNewChildDevices++            
		} else {
			traceEvent(settings.logFilter,"create_child_protects>found ${d.displayName} with id $dni already exists", detailedNotif)
			try {
				if (d.isTokenExpired()) {  // called refreshAllChildAuthTokens when updated
 					refreshAllChildAuthTokens()    
				}
			} catch (e) {
				traceEvent(settings.logFilter,"create_child_protects>exception $e while trying to refresh existing tokens in child $d", detailedNotif)
            
			}            
		}

	}

	traceEvent(settings.logFilter,"create_child_devices>created $countNewChildDevices, total=${devices.size()} protects", detailedNotif)
}


private void create_child_cameras() {

   	int countNewChildDevices =0
	def struct_info  = structure.tokenize('.')
	def structureId = struct_info.last()
	def devices = cameras.collect { dni ->

		def d = getChildDevice(dni)
		traceEvent(settings.logFilter,"create_child_cameras>looping thru cameras, found id $dni", detailedNotif)

		if(!d) {
			def camera_info  = dni.tokenize('.')
			def cameraId = camera_info.last()
 			def name = camera_info[1]
			def labelName = 'MyCam ' + "${name}"
			traceEvent(settings.logFilter,"create_child_cameras>about to create child device with id $dni, cameraId = $cameraId, name=  ${name}", detailedNotif)
			d = addChildDevice(getChildNamespace(), getCameraChildName(), dni, null,
				[label: "${labelName}"])  
			d.initialSetup(getSmartThingsClientId() , getSmartThingsClientSecretId(), atomicState, structureId, cameraId ) 	// initial setup of the Child Device
			traceEvent(settings.logFilter,"create_child_cameras>created ${d.displayName} with id $dni", detailedNotif)
			countNewChildDevices++            
		} else {
			traceEvent(settings.logFilter,"create_child_cameras>found ${d.displayName} with id $dni already exists", detailedNotif)
			try {
				if (d.isTokenExpired()) {  // called refreshAllChildAuthTokens when updated
 					refreshAllChildAuthTokens()    
				}
			} catch (e) {
				traceEvent(settings.logFilter,"create_child_cameras>exception $e while trying to refresh existing tokens in child $d", detailedNotif)
            
			}            
		}

	}

}


def initialize() {
    
	traceEvent(settings.logFilter,"initialize begin...", detailedNotif)
	atomicState?.exceptionCount=0    
	def msg
	atomicState?.poll = [ last: 0, rescheduled: now() ]
    
	int delay = (givenInterval) ? givenInterval.toInteger() : 15 // By default, do it every 15 min.
    
	//Subscribe to different events (ex. sunrise and sunset events) to trigger rescheduling if needed
	subscribe(location, "askAlexaMQ", askAlexaMQHandler)    
	subscribe(location, "sunrise", rescheduleIfNeeded)
	subscribe(location, "sunset", rescheduleIfNeeded)
	subscribe(location, "mode", rescheduleIfNeeded)
	subscribe(location, "sunriseTime", rescheduleIfNeeded)
	subscribe(location, "sunsetTime", rescheduleIfNeeded)
	if (powerSwitch) {
		subscribe(powerSwitch, "switch.off", rescheduleHandler, [filterEvents: false])
		subscribe(powerSwitch, "switch.on", rescheduleHandler, [filterEvents: false])
	}
	if (tempSensor)	{
		subscribe(tempSensor,"temperature", rescheduleHandler,[filterEvents: false])
	}
	if (energyMeter)	{
		subscribe(energyMeter,"energy", rescheduleHandler,[filterEvents: false])
	}
	if (motionSensor)	{
		subscribe(motionSensor,"motion", rescheduleHandler,[filterEvents: false])
	}

	subscribe(app, appTouch)
	delete_child_devices()	
	create_child_tstats()
	create_child_protects()
	create_child_cameras()
	traceEvent(settings.logFilter,"initialize>polling delay= ${delay}...", detailedNotif)
	rescheduleIfNeeded()  
    
}

def askAlexaMQHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "refresh":
		state?.askAlexaMQ = evt.jsonData && evt.jsonData?.queues ? evt.jsonData.queues : []
		traceEvent(settings.logFilter,"askAlexaMQHandler>new refresh value=$evt.jsonData?.queues", detailedNotif, get_LOG_INFO())
		break
	}
}

def appTouch(evt) {
	rescheduleIfNeeded()
	takeAction()    
}

def rescheduleIfNeeded(evt) {
	if (evt) traceEvent(settings.logFilter,"rescheduleIfNeeded>$evt.name=$evt.value", detailedNotif)
	int delay = (givenInterval) ? givenInterval.toInteger() : 15 // By default, do it every 15 min.
	BigDecimal currentTime = now()    
	BigDecimal lastPollTime = (currentTime - (atomicState?.poll["last"]?:0))  
	if (lastPollTime != currentTime) {    
		Double lastPollTimeInMinutes = (lastPollTime/60000).toDouble().round(1)      
		traceEvent(settings.logFilter,"rescheduleIfNeeded>last poll was  ${lastPollTimeInMinutes.toString()} minutes ago", detailedNotif, get_LOG_INFO())
		takeAction()        
	}
	if (((atomicState?.poll["last"]?:0) + (delay * 60000) < currentTime)) {
		traceEvent(settings.logFilter,"rescheduleIfNeeded>scheduling takeAction in ${delay} minutes..", detailedNotif,get_LOG_INFO())
		if (delay <5) {      
			runEvery1Minute(takeAction)
		} else if ((delay >=5) && (delay <10)) {      
			runEvery5Minutes(takeAction)
		} else if ((delay >=10) && (delay <15)) {  
			runEvery10Minutes(takeAction)
		} else if ((delay >=15) && (delay <30)) {  
			runEvery15Minutes(takeAction)
		} else {  
			runEvery30Minutes(takeAction)
		}
      
	}
    
    
	// Update rescheduled state
    
	if (!evt) atomicState.poll["rescheduled"] = now()
}



def takeAction() {
	traceEvent(settings.logFilter,"takeAction>begin", detailedNotif)
	def todayDay
	atomicState?.newDay=false      
    
	if (!location.timeZone) {    	
		traceEvent(settings.logFilter,"takeAction>Your location is not set in your ST account, you'd need to set it as indicated in the prerequisites for better exception handling..",
			true,get_LOG_ERROR(), true)
	} else {
		todayDay = new Date().format("dd",location.timeZone)
	}        
	if ((!atomicState?.today) || (todayDay != atomicState?.today)) {
		atomicState?.exceptionCount=0   
		atomicState?.sendExceptionCount=0 
		atomicState?.newDay=true        
		atomicState?.today=todayDay        
	}   
    
	int delay = (givenInterval) ? givenInterval.toInteger() : 15 // By default, do it every 15 min.
	atomicState?.poll["last"] = now()
		
	//schedule the rescheduleIfNeeded() function
    
	if (((atomicState?.poll["rescheduled"]?:0) + (delay * 60000)) < now()) {
		traceEvent(settings.logFilter,"takeAction>scheduling rescheduleIfNeeded() in ${delay} minutes..", detailedNotif, get_LOG_INFO())
		unschedule()        
		schedule("0 0/${delay} * * * ?", rescheduleIfNeeded)
		// Update rescheduled state
		atomicState?.poll["rescheduled"] = now()
	}
    
	def struct_info  = structure.tokenize('.')
	def structureId = struct_info.last()

	delete_obsolete_devices(structureId)
	poll_protects()    
	poll_tstats()    
	poll_cameras()   


	traceEvent(settings.logFilter,"takeAction>end", detailedNotif)

}



private void poll_protects() {
	traceEvent(settings.logFilter,"poll_protects>begin", detailedNotif)
	String exceptionCheck    
	def MAX_EXCEPTION_COUNT=10
	boolean handleException = (handleExceptionFlag)?: false
    
	def devicesProtects = protectUnits.collect { dni ->
		def d = getChildDevice(dni)
		if (d) {       
			traceEvent(settings.logFilter,"poll_protects>Looping thru protects, found id $dni, about to poll", detailedNotif, get_LOG_INFO())
			d.poll()
			exceptionCheck = d.currentVerboseTrace?.toString()
			if ((exceptionCheck) && (((exceptionCheck.contains("exception") || (exceptionCheck.contains("error"))) && 
				(!exceptionCheck.contains("TimeoutException")) && (!exceptionCheck.contains("No signature of method: physicalgraph.device.CommandService.executeAction")) &&
				(!exceptionCheck.contains("UndeclaredThrowableException"))))) {  
				// check if there is any exception or an error reported in the verboseTrace associated to the device (except the ones linked to rate limiting).
				atomicState.exceptionCount=atomicState.exceptionCount+1    
				traceEvent(settings.logFilter,"found exception/error after polling, exceptionCount= ${atomicState?.exceptionCount}: $exceptionCheck", detailedNotif, get_LOG_ERROR()) 
			} else {   // poll was successful          
				// reset exception counter            
				atomicState?.exceptionCount=0   
				if (atomicState?.newDay && askAlexaFlag) { // produce summary reports only at the beginning of a new day
					def PAST_DAY_SUMMARY=1 // day
					def PAST_WEEK_SUMMARY=7 // days
					if (settings.protectDaySummaryFlag) {
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past day", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_DAY_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}                    
					if (settings.protectWeeklySummaryFlag) { // produce summary report only at the beginning of a new day
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past week", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_WEEK_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}
				} /* end if askAlexa */                    
                
			}                
			                
		} /* end if (d) */        
	}
    
	if (handleException) {    
		if ((exceptionCheck) && (exceptionCheck.contains("Unauthorized")) && (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT)) {
			// need to authenticate again    
			atomicState?.access_token=null                    
			atomicState?.oauthTokenProvided=false
			traceEvent(settings.logFilter,"$exceptionCheck after ${atomicState?.exceptionCount} errors, press on 'Next' and re-login at Nest..." , 
				true, get_LOG_ERROR(),true)
		} else if (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT) {
			traceEvent(settings.logFilter,"too many exceptions/errors, $exceptionCheck (${atomicState?.exceptionCount} errors so far), you may need to press on 'Next' and re-login at Nest..." ,
				true, get_LOG_ERROR(), true)
		}
	} /* end if handleException */        

	traceEvent(settings.logFilter,"poll_protects>end", detailedNotif)

}



private void poll_tstats() {
	traceEvent(settings.logFilter,"poll_tstats>begin", detailedNotif)
	String exceptionCheck    
	def MAX_EXCEPTION_COUNT=10
	boolean handleException = (handleExceptionFlag)?: false

	def devicesTstats = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if (d) {       
			traceEvent(settings.logFilter,"poll_tstats>Looping thru thermostats, found id $dni, about to poll", detailedNotif, get_LOG_INFO())
			d.poll()
			exceptionCheck = d.currentVerboseTrace?.toString()
			if ((exceptionCheck) && (((exceptionCheck.contains("exception") || (exceptionCheck.contains("error"))) && 
				(!exceptionCheck.contains("TimeoutException")) && (!exceptionCheck.contains("No signature of method: physicalgraph.device.CommandService.executeAction")) &&
				(!exceptionCheck.contains("UndeclaredThrowableException"))))) {  
				// check if there is any exception or an error reported in the verboseTrace associated to the device (except the ones linked to rate limiting).
				atomicState.exceptionCount=atomicState.exceptionCount+1    
				traceEvent(settings.logFilter,"found exception/error after polling, exceptionCount= ${atomicState?.exceptionCount}: $exceptionCheck", detailedNotif, get_LOG_ERROR()) 
			} else {             
				// reset exception counter            
				atomicState?.exceptionCount=0      
				if (atomicState?.newDay && askAlexaFlag) { // produce summary reports only at the beginning of a new day
					def PAST_DAY_SUMMARY=1 // day
					def PAST_WEEK_SUMMARY=7 // days
					if (settings.tstatDaySummaryFlag) {
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past day", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_DAY_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}                    
					if (settings.tstatWeeklySummaryFlag) { // produce summary report only at the beginning of a new day
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past week", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_WEEK_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}
				} /* end if askAlexa */                    
			}                
		} /* end if (d) */        
	} 
	
	if (handleException) {    
		if ((exceptionCheck) && (exceptionCheck.contains("Unauthorized")) && (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT)) {
			// need to authenticate again    
			atomicState?.access_token=null                    
			atomicState?.oauthTokenProvided=false
			traceEvent(settings.logFilter,"$exceptionCheck after ${atomicState?.exceptionCount} errors, press on 'Next' and re-login at Nest..." , 
				true, get_LOG_ERROR(),true)
		} else if (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT) {
			traceEvent(settings.logFilter,"too many exceptions/errors, $exceptionCheck (${atomicState?.exceptionCount} errors so far), you may need to press on 'Next' and re-login at Nest..." ,
				true, get_LOG_ERROR(), true)
		}
	} /* end if handleException */        

	traceEvent(settings.logFilter,"poll_tstats>end", detailedNotif)

}

private void poll_cameras() {
	traceEvent(settings.logFilter,"poll_cameras>begin", detailedNotif)
	String exceptionCheck    
	def MAX_EXCEPTION_COUNT=10
	boolean handleException = (handleExceptionFlag)?: false
    
	def deviceCameras = cameras.collect { dni ->
		def d = getChildDevice(dni)
		if (d) {       
			traceEvent(settings.logFilter,"poll_cameras>Looping thru cameras, found id $dni, about to poll", detailedNotif)
			d.poll()
			exceptionCheck = d.currentVerboseTrace?.toString()
			if ((exceptionCheck) && (((exceptionCheck.contains("exception") || (exceptionCheck.contains("error"))) && 
				(!exceptionCheck.contains("TimeoutException")) && (!exceptionCheck.contains("No signature of method: physicalgraph.device.CommandService.executeAction")) &&
				(!exceptionCheck.contains("UndeclaredThrowableException"))))) {  
				// check if there is any exception or an error reported in the verboseTrace associated to the device (except the ones linked to rate limiting).
				atomicState.exceptionCount=atomicState.exceptionCount+1    
				traceEvent(settings.logFilter,"found exception/error after polling, exceptionCount= ${atomicState?.exceptionCount}: $exceptionCheck", detailedNotif, get_LOG_ERROR(), true) 
			} else {             
				// reset exception counter            
				atomicState?.exceptionCount=0      
				if (atomicState?.newDay && askAlexaFlag) { // produce summary reports only at the beginning of a new day
					def PAST_DAY_SUMMARY=1 // day
					def PAST_WEEK_SUMMARY=7 // days
					if (settings.camDaySummaryFlag) {
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past day", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_DAY_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}                    
					if (settings.camWeeklySummaryFlag) { // produce summary report only at the beginning of a new day
						traceEvent(settings.logFilter,"About to call produceSummaryReport for device ${d.displayName} in the past week", detailedNotif, get_LOG_TRACE()) 
						d.produceSummaryReport(PAST_WEEK_SUMMARY)
						String summary_report =d.currentValue("summaryReport")                        
						if (summary_report) {                        
							send (summary_report, askAlexaFlag)                        
						}                            
					}
				} /* end if askAlexa */                    
			}                
		} /* end if (d) */        
	}
	
	if (handleException) {    
		if ((exceptionCheck) && (exceptionCheck.contains("Unauthorized")) && (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT)) {
			// need to authenticate again    
			atomicState?.access_token=null                    
			atomicState?.oauthTokenProvided=false
			traceEvent(settings.logFilter,"$exceptionCheck after ${atomicState?.exceptionCount} errors, press on 'Next' and re-login at Nest..." , 
				true, get_LOG_ERROR(),true)
		} else if (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT) {
			traceEvent(settings.logFilter,"too many exceptions/errors, $exceptionCheck (${atomicState?.exceptionCount} errors so far), you may need to press on 'Next' and re-login at Nest..." ,
				true, get_LOG_ERROR(), true)
		}
	} /* end if handleException */        

	traceEvent(settings.logFilter,"poll_cameras>end", detailedNotif)

}


def isTokenExpired() {
	def buffer_time_expiration=5  // set a 5 min. buffer time before token expiration to avoid auth_err 
	def time_check_for_exp = now() + (buffer_time_expiration * 60 * 1000)
	traceEvent(settings.logFilter,"isTokenExpired>expiresIn timestamp: ${atomicState?.authexptime} > timestamp check for exp: ${time_check_for_exp}?", detailedNotif)
	if (atomicState?.authexptime > time_check_for_exp) {
		traceEvent(settings.logFilter,"isTokenExpired>not expired", detailedNotif)
		return false
	}
	traceEvent(settings.logFilter,"isTokenExpired>expired", detailedNotif)
	return true    
}




def oauthInitUrl() {
//	settings.logFilter=5
//	settings.detailedNotif=true    
	traceEvent(settings.logFilter,"oauthInitUrl>begin", detailedNotif)
	def stcid = getSmartThingsClientId();
	def privateKey=getSmartThingsClientSecretId()    

	if (!stcid || !privateKey) {
		traceEvent(settings.logFilter,"oauthInitUrl>no valid clientId or private key in your application settings for the smartapp", detailedNotif, get_LOG_ERROR())
		return        
	}        
	atomicState?.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		client_id: stcid,
		state: atomicState.oauthInitState,
		redirect_uri: "${get_ST_URI_ROOT()}/oauth/callback"
	]
    

	redirect(location: "${get_URI_ROOT()}/login/oauth2?${toQueryString(oauthParams)}")
    
}


def callback() {
//	settings.logFilter=5
//	settings.detailedNotif=true    
	traceEvent(settings.logFilter,"callback>swapping token: $params", detailedNotif)

	def code = params.code
	def oauthState = params.state

 	// Validate the response from the 3rd party by making sure oauthState == atomicState.oauthInitState as expected
	if (oauthState == atomicState?.oauthInitState){

		def stcid = getSmartThingsClientId()
		def privateKey=getSmartThingsClientSecretId()    
		if (!stcid || !privateKey) {
			traceEvent(settings.logFilter,"callback>no valid clientId or private key in your application settings for the smartapp", detailedNotif, get_LOG_ERROR())
 			return       
		}        

		def tokenParams = [
			grant_type: "authorization_code",
			code: params.code.toString(),
			client_id: stcid,
			client_secret: privateKey
		]
		def tokenUrl = "https://api.home.nest.com/oauth2/access_token?${toQueryString(tokenParams)}"

		traceEvent(settings.logFilter,"callback>Swapping token $params", detailedNotif)

		def jsonMap
		httpPost(uri:tokenUrl) { resp ->
			jsonMap = resp.data
			atomicState?.refresh_token = jsonMap.refresh_token
			atomicState?.access_token = jsonMap.access_token
			atomicState?.expires_in=jsonMap.expires_in
			atomicState?.token_type = jsonMap.token_type
			long authexptime = new Date((now() + (jsonMap.expires_in * 60))).getTime()
			atomicState?.authexptime = authexptime
			atomicState?.clientId = stcid
			atomicState?.privateKey = privateKey
            
		}
		if(atomicState?.access_token) {
			traceEvent(settings.logFilter,"callback() success. Access token=${atomicState?.access_token} obtained",detailedNotif)
			success()
		} else {
			traceEvent(settings.logFilter,"callback() failed. No access token obtained",detailedNotif)
			fail()        
		}
	
	} else {
		traceEvent(settings.logFilter,"callback() failed. Validation of state did not match. oauthState != state.oauthInitState", true, get_LOG_ERROR())
		fail()        
	}


}

void removeAccessToken() {
	def DELETE_SUCCESS=204
	if (atomicState?.access_token ) {
		def tokenParams = [
			uri: "https://api.home.nest.com/oauth2/access_tokens/${atomicState?.access_token}",
			contentType: 'application/json'
		]
		httpDelete(tokenParams) { resp ->
			if (resp?.status == DELETE_SUCCESS) {
				traceEvent(settings.logFilter,"removeToken>Nest token deleted", settings.detailedNotif)
			}
			resetAtomicState()
		}
	} else { 
		resetAtomicState() 
	}
}

void resetAtomicState() {
	atomicState?.refresh_token = null
	atomicState?.access_token = null
	atomicState?.expires_in=null
	atomicState?.token_type = null
	atomicState?.authexptime = null
	atomicState?.clientId = null
	atomicState?.privateKey = null
}

def success() {

	def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=640">
<title>Withings Connection</title>
<style type="text/css">
	@font-face {
		font-family: 'Swiss 721 W01 Thin';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	@font-face {
		font-family: 'Swiss 721 W01 Light';
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
		src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
			 url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
		font-weight: normal;
		font-style: normal;
	}
	.container {
		width: 560px;
		padding: 40px;
		/*background: #eee;*/
		text-align: center;
	}
	img {
		vertical-align: middle;
	}
	img:nth-child(2) {
		margin: 0 30px;
	}
	p {
		font-size: 2.2em;
		font-family: 'Swiss 721 W01 Thin';
		text-align: center;
		color: #666666;
		padding: 0 40px;
		margin-bottom: 0;
	}
/*
	p:last-child {
		margin-top: 0px;
	}
*/
	span {
		font-family: 'Swiss 721 W01 Light';
	}
</style>
</head>
<body>
	<div class="container">
		<img src="${getCustomImagePath()}WorksWithNest.png" width="128" height="166" alt="Nest icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your Nest Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	</div>
</body>

</html>
"""

	render contentType: 'text/html', data: html
}


def fail() {
	def message = """
		<p>There was an error connecting your Nest account with SmartThings</p>
		<p>Please try again.</p>
	"""
	displayMessageAsHtml(message)
}

def displayMessageAsHtml(message) {
	def html = """
		<!DOCTYPE html>
		<html>
			<head>
			</head>	
			<body>
				<div>
					${message}
				</div>
			</body>
		</html>
	"""
	render contentType: 'text/html', data: html
}


private String swapValue(value) {
	int valueInt=swapHex(value)
	int counter= getAppLastDigit() + valueInt    
 	String valueAsString=new String(value.shiftRight(counter).toByteArray()) 
 	String results=new String(valueAsString.decodeBase64()) 
    
	return results
}

private def get_ENC_METHOD() {
	BigInteger bigValue=26398233360 
	String results=swapValue(bigValue.shiftLeft(getAppLastDigit()))    
	return results
}

def toJson(Map m) {
	return new org.codehaus.groovy.grails.web.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

// Maximum URL redirect
private def  get_MAX_REDIRECT() {
	return 10
}

private def get_MAX_ERROR_WITH_REDIRECT_URL() {
	return 15

}

// Maximum number of command retries for setters
private def get_MAX_SETTER_RETRIES() {
	return 10
}


// Maximum number of command retries for getters
private def get_MAX_GETTER_RETRIES() {
	return 2
}

def getChildNamespace() { "yracine" }
def getProtectChildName() { "My Next Alarm" }
def getTstatChildName() { "My Next Tstat" }
def getCameraChildName() { "My Next Cam" }

def getServerUrl() { return getApiServerUrl()  }

def getSmartThingsClientId() {
	settings.detailedNotif=true
	BigInteger bigValue=44911969330327575293515631021982366930797762883643677692263595725681744792080308585438307411199585796068304
	def KEY_LENGTH=24    

	String clientId = swapValue(bigValue.shiftLeft(getAppLastDigit()))   
	if (!clientId) { 
		traceEvent(settings.logFilter,"getSmartThingsClientId>no client id available", true, get_LOG_ERROR())
		return        
	}    
    
	if (clientId.length() < KEY_LENGTH) { 
		traceEvent(settings.logFilter,"getSmartThingsClientId>clientId is wrong, error=$clientId", true, get_LOG_ERROR())
		return        
	}    
	def key = clientId.substring(0,8) + '-' + clientId.substring(8,12) + '-' + clientId.substring(12,16) + '-' + clientId.substring(16,20) + '-' + clientId.substring(20,clientId.length())
	return key    
}    

def getSmartThingsClientSecretId() { 
	settings.detailedNotif=true
	BigInteger bigValue=3023695962363923406957636928471032891766664875134751569140898167723554062858372445885392
    
	String privateKey = swapValue(bigValue.shiftLeft(getAppLastDigit()))   
	if (!privateKey) { 
		traceEvent(settings.logFilter,"getSmartThingsClientSecretId>no private Key available", true, get_LOG_ERROR())
		return        
	}    
	return privateKey
}    



private def get_API_URI_ROOT() {
	def root
	if (state?.redirectURL) {
		root=state?.redirectURL     
	} else {
		root="https://developer-api.nest.com"
	}
	return root
}
private def get_ENC_CODE() {
	BigInteger bigValue = 30376063556905792694493975217037110971557260191245884707792 
	String results=swapValue(bigValue.shiftLeft(getAppLastDigit()))    
	return results        
}

private getAppLastDigit() {
	def lastDigitHex=app.id.toString().substring((app.id.length()-1),(app.id.length()))
	int lastDigit = convertHexToInt(lastDigitHex)    
	return lastDigit
}    

private def get_ENC_ACCESS() {
	BigInteger bigValue=3150072899112991317751466672467935132072432369743167116298185306338656159441848021260096  
	String results=swapValue(bigValue.shiftLeft(getAppLastDigit()))    
	return results   
}

private int swapHex(value) {
	int cst=  0xC48759A2 & 0x3F18A051
	byte[] bytes= getArray(cst.getBytes())
	int result = new BigInteger(1,bytes) 
	return result    
}

private byte[] getArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;

	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}

	return array
}

private int convertHexToInt(value) {	
	switch (value) {
		case '0'..'9':
			return value.toInteger()
		case 'A':
		case 'a':
			return 10
		case 'B':
		case 'b':
			return 11
		case 'C':
		case 'c':
			return 12
		case 'D':
		case 'd':
			 return 13
		case 'E':
		case 'e':
			return 14
		case 'F':
		case 'f':
			return 15
		default:           
			return 0			
	}    
}

private def get_URI_ROOT() {
	return "https://home.nest.com"
}



private def get_API_VERSION() {
	return "1"
}
private def get_ST_URI_ROOT() {
	return "https://graph.api.smartthings.com"
}

def getCustomImagePath() {
	return "https://raw.githubusercontent.com/yracine/device-type-myNext/master/icons/"
}    

private def getStandardImagePath() {
	return "http://cdn.device-icons.smartthings.com/"
}


private send(String msg, askAlexa=false) {
	int MAX_EXCEPTION_MSG_SEND=5

	// will not send exception msg when the maximum number of send notifications has been reached
	if (msg.contains("exception")) {
		atomicState?.sendExceptionCount=atomicState?.sendExceptionCount+1         
		traceEvent(settings.logFilter,"checking sendExceptionCount=${atomicState?.sendExceptionCount} vs. max=${MAX_EXCEPTION_MSG_SEND}", detailedNotif)
		if (atomicState?.sendExceptionCount >= MAX_EXCEPTION_MSG_SEND) {
			traceEvent(settings.logFilter,"send>reached $MAX_EXCEPTION_MSG_SEND exceptions, exiting", detailedNotif)
			return        
		}        
	}    
	def message = "${get_APP_NAME()}>${msg}"


	if (sendPushMessage != "No") {
		traceEvent(settings.logFilter,"contact book not enabled", false, get_LOG_INFO())
		sendPush(message)
	}
	if (askAlexa) {
		def expiresInDays=(AskAlexaExpiresInDays)?:2    
		sendLocationEvent(
			name: "AskAlexaMsgQueue", 
			value: "${get_APP_NAME()}", 
			isStateChange: true, 
			descriptionText: msg, 
			data:[
				queues: listOfMQs,
		        expires: (expiresInDays*24*60*60)  /* Expires after 2 days by default */
		    ]
		)
	} /* End if Ask Alexa notifications*/
	
	if (phoneNumber) {
		sendSms(phoneNumber, message)
	}
}


private int get_LOG_ERROR()	{return 1}
private int get_LOG_WARN()	{return 2}
private int get_LOG_INFO()	{return 3}
private int get_LOG_DEBUG()	{return 4}
private int get_LOG_TRACE()	{return 5}

def traceEvent(filterLog, message, displayEvent=false, traceLevel=4, sendMessage=false) {
	int LOG_ERROR= get_LOG_ERROR()
	int LOG_WARN=  get_LOG_WARN()
	int LOG_INFO=  get_LOG_INFO()
	int LOG_DEBUG= get_LOG_DEBUG()
	int LOG_TRACE= get_LOG_TRACE()
	int filterLevel=(filterLog)?filterLog.toInteger():get_LOG_WARN()


	if (filterLevel >= traceLevel) {
		if (displayEvent) {    
			switch (traceLevel) {
				case LOG_ERROR:
					log.error "${message}"
				break
				case LOG_WARN:
					log.warn "${message}"
				break
				case LOG_INFO:
					log.info "${message}"
				break
				case LOG_TRACE:
					log.trace "${message}"
				break
				case LOG_DEBUG:
				default:            
					log.debug "${message}"
				break
			}                
		}			                
		if (sendMessage) send (message,settings.askAlexaFlag) //send message only when true
	}        
}



private def get_APP_NAME() {
	return "MyNextManager"
}