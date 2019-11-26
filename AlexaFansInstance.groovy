/**
 *  Alexa Fans Instance
 *
 *  Copyright 2019 Jonathan Porter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.*
	
definition(
	parent: "jonoporter:Alexa Fans",
    name: "Alexa Fans Instance",
    namespace: "jonoporter",
    author: "Jonathan Porter",
    description: "Child app that is instantiated by the Alexa Fans app.  It creates the binding between the physical fan and a virtual dimmer that will act as an interface for the fan with alexa. This is to resolve 2 issues with Alexa and fans in Hubitat. Unresponsiveness and No Medium Speed by name.",
    category: "Convenience",
	iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png")

def fanNameInput = [
	name:				"fanName",
	type:				"string",
	title:				"This is the Name that you will command Alexa to change",
	defaultValue:		null,
	required:			true
]
def wrappedFanInput = [
	name:				"wrappedFan",
	type:				"capability.fanControl",
	title:				"Wrapped Fan",
	description:		"Select the real fan that is to be wrapped.",
	multiple:			false,
	required:			true
]

def fanSpeedCountInput = [
	name:				"fanSpeedCount",
	type:				"enum",
	title:				"How many speeds does your fan have?",
	options:			[ "3", "5" ],
	defaultValue:		"3",
	required:			true,
	submitOnChange: 	true
]

def hasScenesInput = [
	name:				"hasScenes",
	type:				"bool",
	title:				"Create Virtual devices to act as Medium, medium high and such, when interfacing with alexa?",
	defaultValue:		true,
	required:			true
]
def lowfanSpeedInput = [
	name:				"lowfanSpeed",
	type:				"Integer",
	title:				"The percentage to set the level for low ",
	defaultValue:		10,
	required:			true
]
def mediumLowfanSpeedInput = [
	name:				"mediumLowfanSpeed",
	type:				"Integer",
	title:				"The percentage to set the level for medium-low (ignore if you only have 3 speeds)",
	defaultValue:		30,
	required:			true
]
def mediumfanSpeedInput = [
	name:				"mediumfanSpeed",
	type:				"Integer",
	title:				"The percentage to set the level for medium",
	defaultValue:		50,
	required:			true
]
def mediumHighfanSpeedInput = [
	name:				"mediumHighfanSpeed",
	type:				"Integer",
	title:				"The percentage to set the level for medium-high (ignore if you only have 3 speeds)",
	defaultValue:		70,
	required:			true
]
def highfanSpeedInput = [
	name:				"highfanSpeed",
	type:				"Integer",
	title:				"The percentage to set the level for low ",
	defaultValue:		90,
	required:			true
]
def enableDebugLoggingInput = [
	type: 				"bool",
	name: 				"enableDebugLogging",
	title: 				"Enable Debug Logging?",
	required: 			true,
	defaultValue: 		true
]

preferences {
 	page(name: "mainPage", title: "", install: true, uninstall: true) {
		section(getFormattedTitle("Alexa Fan Instance")) {
		}
		section("") {
			input fanNameInput
			input wrappedFanInput
			input hasScenesInput
			input fanSpeedCountInput
			paragraph "below are the levels for the virtual wrapper dimmer switch to translate to the real fan and to be set with the medium Switches"
			input lowfanSpeedInput
			input mediumLowfanSpeedInput
			input mediumfanSpeedInput
			input mediumHighfanSpeedInput
			input highfanSpeedInput
			input enableDebugLoggingInput
		}
	}
}
 


def String mediumName(){ return "${fanName} Medium" }
def String mediumHighName(){ return "${fanName} Medium High" }
def String mediumLowName(){ return  "${fanName} Medium Low" }
def String[] getSpeeds(){return ["off","low","medium-low","medium","medium-high","high"  ]}
def Integer getLowfanSpeed(){	return lowfanSpeed.toInteger() }
def Integer getMediumLowfanSpeed(){	return mediumLowfanSpeed.toInteger() }
def Integer getMediumfanSpeed(){	return mediumfanSpeed.toInteger() }
def Integer getMediumHighfanSpeed(){	return mediumHighfanSpeed.toInteger() }
def Integer getHighfanSpeed(){	return highfanSpeed.toInteger() }
def Integer getFanSpeedCount(){	return fanSpeedCount.toInteger() }

def installed() {
	log.info "Installed with settings: ${settings}"
	setupVirtuals()
	initialize()
}
def uninstalled() {
    childDevices.each {
		log.info "Deleting child device: ${it.displayName}"
		deleteChildDevice(it.deviceNetworkId)
	}
}
def updated() {
	log.info "Updated with settings: ${settings}"

	unsubscribe()
	setupVirtuals()
	initialize()
}
def initialize() {
	// Generate a label for this child app
	app.updateLabel("Alexa Fans: ${fanName}")

	def wrapper = getVirtual(fanName);
	subscribe(wrapper, "level", alexaFanLevelHandler)
	subscribe(wrapper, "switch", alexaFanSwitchHandler)
	subscribe(wrappedFan, "level", wrappedFanoffHandler)
	subscribe(wrappedFan, "switch", wrappedFanoffHandler)
	if(wrappedFan.currentValue("switch") == "on")
    {
        wrapper.on();
    }
    else
    {
        wrapper.off();
    }
	if(hasScenes)
	{
		def medName = mediumName()
		subscribe(getVirtual(medName), "switch.on", alexaFanMediumHandler)
		if(getFanSpeedCount() == 5)
		{
			def medhighName = mediumHighName()
			def medlowName = mediumLowName()
			subscribe(getVirtual(medhighName), "switch.on", alexaFanMediumHighHandler)
			subscribe(getVirtual(medlowName), "switch.on", alexaFanMediumLowHandler)
		}
	}
}



def setupVirtuals()
{
	def medName = mediumName()
	def medhighName = mediumHighName()
	def medlowName = mediumLowName()

	addVirtual(fanName, true)
	if(hasScenes){
		addVirtual(medName, false)
		if(getFanSpeedCount() == 5){
			addVirtual(medhighName, false)
			addVirtual(medlowName, false)
		}
		else
		{
			removeVirtual(medhighName)
			removeVirtual(medlowName)
		}
	}
	else
	{
		removeVirtual(medName)
		removeVirtual(medhighName)
		removeVirtual(medlowName)
	}
}
def getIdFromName(String name)
{
	def id = "AFVS_${name.replaceAll("\\s","")}" 
	return id
}
def addVirtual(String name, Boolean isDimmer)
{
	if(!doesVirtualExist(name))
	{
		def id = getIdFromName(name)
		addChildDevice("hubitat", isDimmer ? "Virtual Dimmer" : "Virtual Switch", id, null, [label: name, completedSetup: true, isComponent: true]) 
		log "${name} created"
	}
	else
	{
		log "${name} already exists skipping creation"

	}
}
def doesVirtualExist(String name)
{
	try
	{
		//todo check getChildDevices instead to avoid exception
		return getVirtual(name) != null
	}
	catch( e1)
	{
		return false
	}
}
def getVirtual(String name)
{
	def id = getIdFromName(name)
	def virtual = getChildDevice(id)
	return virtual
}
def removeVirtual(String name)
{
	if(doesVirtualExist(name))
	{
		def id = getIdFromName(name)
		deleteChildDevice(id)
		log "${name} deleted"
	}
	
}

def wrappedFanoffHandler(evt){

	def speed = wrappedFan.currentValue("speed");
	def isOff = wrappedFan.currentValue("switch") == "off";
	def isMedium = speed == "medium";
	if(isOff || !isMedium)	{
		turnoffSpeed(mediumName())
	}
	if(getFanSpeedCount() == 5)
	{
		def isMediumLow = wrappedFan.currentValue("speed") == "medium-low";
		def isMediumHigh = wrappedFan.currentValue("speed") == "medium-high";
		if(isOff || !isMediumLow)	{
			turnoffSpeed(mediumLowName())
		}
		if(isOff || !isMediumHigh){
			turnoffSpeed(mediumHighName())
		}
	}
	def wrapper = getVirtual(fanName);
	if(!isOff){
		def newValue = 0;
		if(speed == "low"){
			newValue=getLowfanSpeed()
		}
		else if(speed == "medium-low"){
			newValue=getMediumLowfanSpeed()
		}
		else if(speed == "medium"){
			newValue=getMediumfanSpeed()
		}
		else if(speed == "medium-high"){
			newValue=getMediumHighfanSpeed()
		}
		else if(speed == "high"){
			newValue=getHighfanSpeed()
		}
		if(wrapper.currentLevel != newValue){
			wrapper.setLevel(newValue);
		}
	}
	def isWrapperOff = wrapper.currentValue("switch") == "off";
	if(isWrapperOff!=isOff){
		if(isOff){
			wrapper.off();
		}
		else{
			wrapper.on();
		}
	}
}
def turnoffSpeed(String speedName){
	log "${speedName} off"
	def speedSwitch = getVirtual(speedName);
	speedSwitch.off()
}

def alexaFanMediumHighHandler(evt) {
	log "${wrappedFan.displayName} Medium High Activated"
	def wrapper = getVirtual(fanName);
	def mediumHigh = getMediumHighfanSpeed();
	wrapper.setLevel(mediumHigh);
	setFan(4)
}
def alexaFanMediumHandler(evt) {
	log "${wrappedFan.displayName} Medium Activated"
	def wrapper = getVirtual(fanName);
	def medium = getMediumfanSpeed();
	wrapper.setLevel(medium);
	setFan(3)
}
def alexaFanMediumLowHandler(evt) {
	log "${wrappedFan.displayName} Medium Low Activated"
	def wrapper = getVirtual(fanName);
	def mediumLow = getMediumLowfanSpeed();
	wrapper.setLevel(mediumLow);
	setFan(2)
}

def alexaFanSwitchHandler(evt) {
	//log "${wrappedFan.displayName} switch changed"
	//def wrappedFan = getChildDevice(wrappedFan.id )
	if(evt.value == "on"){
      def wrapper = getVirtual(fanName);
      def level =  wrapper.currentLevel.toInteger();
      log "${wrappedFan.displayName} switch changed to $evt.value of $level"
   //   wrappedFan.on()

      handleLevel(level);
	}
	else if (evt.value == "off"){
	      log "${wrappedFan.displayName} switch changed to $evt.value"
	    wrappedFan.off()
	}
}
def alexaFanLevelHandler(evt) {
	log "${wrappedFan.displayName} level changed"
	def level = evt.value.toInteger()
	handleLevel(level)
}
def handleLevel(Integer level)
{
	if(getFanSpeedCount() == 3)
	{
		def medium = getMediumfanSpeed();
		if(level<medium){
			setFan(1)
		}
		else if(level>medium){
			setFan(5)
		}
		else {
			setFan(3)
		}
	}
	else
	{
		def low = getLowfanSpeed();
	//	def mediumLow= getMediumLowfanSpeed();
		def medium= getMediumfanSpeed();
	//	def mediumHigh=getMediumHighfanSpeed();
		def high=getHighfanSpeed();

		if(level<=low){
			setFan(1)
		}
		else if(level>=high){
			setFan(5)
		}
		else if(level<medium){
			setFan(2)
		}
		else if(level>medium){
			setFan(4)
		}
		else {
			setFan(3)
		}
	}
}
def setFan(Integer speedIndex)
{
	def speed = getSpeedFromIndex(speedIndex)
	log "${wrappedFan.displayName} is set too level ${speed}"
	wrappedFan.setSpeed(speed)
}
def String getSpeedFromIndex(Integer speed){
	def speeds = getSpeeds()
	if(speed >= 0 && speed < speeds.size())
	{
		return speeds[speed]
	}
	return speeds[0]
}

 
def  String getFormattedTitle(String myText){
	return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def log(message) {
	if (enableDebugLogging) {
		log.debug(message)	
	}
}




