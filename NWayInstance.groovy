/**
 *  Virtual N-Way Switch Instance
 *  IE 4 way or 3 way or whatever
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
	parent: "jonoporter:Virtual 3-way Switch",
    name: "Virtual 3-way Switch Instance",
    namespace: "jonoporter",
    author: "Jonathan Porter",
    description: "Simulates at 3 and 4 way switch and prevents the on/off loop",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)


def nwayNameInput = [
	name:				"nwayName",
	type:				"string",
	title:				"This is the Name of the Group of switches",
	defaultValue:		null,
	required:			true
]
def idleTimeoutInput = [
	name:				"idleTimeout",
	type:				"decimal",
	title:				"The time (milliseconds) that must pass, after activity between the switches stop, before a different switch is allowed to control the group",
	defaultValue:		8000,
    range: "1000..*",
	required:			true,
]
def enableDebugLoggingInput = [
	type: 				"bool",
	name: 				"enableDebugLogging",
	title: 				"Enable Debug Logging?",
	required: 			true,
	defaultValue: 		false
]
def controlSwitchesInput = [
	type: 				"capability.switch",
	name: 				"controlSwitches",
	title: 				"Control Switches?",
	required: 			true,
    multiple:           true,
]
def slaveSwitchesInput = [
	type: 				"capability.switch",
	name: 				"slaveSwitches",
	title: 				"Subordinate Switches?",
	required: 			true,
    multiple:           true,
]
def pauseInput = [
	type: 				"bool",
	name: 				"pause",
	title: 				"Pause",
    description: 		"Suspend Execution?",
	required: 			true,
	defaultValue: 		false
]

preferences {
 	page(name: "mainPage", title: "", install: true, uninstall: true) {

        section("Control Switches that will turn on/off the group"){
            input controlSwitchesInput//"controlSwitches", "capability.switch", multiple: true
        }
        section("Switches that will turn on/off with the group but not trigger"){
            input slaveSwitchesInput // "slaveSwitches", "capability.switch", multiple: true
        }
        section("Options"){
            input nwayNameInput;
            input pauseInput;
            input enableDebugLoggingInput
            input idleTimeoutInput
        }
    }
}
def initialize(){

    app.updateLabel("${nwayName} 3 and 4-way")
    atomicState.lastRun =  new Date().getTime();
    atomicState.lastState = "";
    atomicState.lastId = "";
    if(!pause){
        subscribe(controlSwitches, "switch.on", contactOpenHandler)
        subscribe(controlSwitches, "switch.off", contactOpenHandler)
    }

}

def installed()
{
    initialize();
}

def updated()
{
	unsubscribe()
    initialize();
}
def getAllSwitches(){
    return controlSwitches.plus(slaveSwitches);
}
def setState(desiredState){
    def allSwitches = getAllSwitches();
    def toSwitch =[];
        allSwitches.each {sw -> 
        if(sw.currentState("switch") != desiredState){
            toSwitch.add(sw);
        }
    }
    if(desiredState == "on"){
        toSwitch.each{ it.on();}
    }else{
        toSwitch.each{ it.off();}
    }
}

def contactOpenHandler(evt) {

    def now =  new Date().getTime();
    def lastTime = Long.valueOf(atomicState.lastRun);
    def lastId = atomicState.lastId;
    def elapsed = now - lastTime;
    atomicState.lastRun = now;
    def currentId = evt.descriptionText.split("was").first();

    if(elapsed < idleTimeout && currentId != lastId){
        log "contactOpenHandler Ignored elapsed: $elapsed, lastId: $lastId, currentId: $currentId"
        return;
    }
    if(atomicState.lastState == evt.value ){
        log "contactOpenHandler Ignored lastState: $atomicState.lastState"
        return;
    }
    atomicState.lastState = evt.value;
    atomicState.lastId = currentId;
    log "contactOpenHandler Handled elapsed: $elapsed, currentId:$currentId,  evt.value: $evt.value"
     
    setState(evt.value);
    
}
def log(message) {
	if (enableDebugLogging) {
		log.debug(message)	
	}
}
