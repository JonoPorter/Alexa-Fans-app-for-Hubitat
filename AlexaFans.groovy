/**
 *  Alexa Fans
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


definition(
    name: "Alexa Fans",
    namespace: "jonoporter",
    author: "Jonathan Porter",
    description: "An app that will create virtual fans that 'wrap' your physical fans devices, making them more reliable.",
    category: "Convenience",
	iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances11-icn@2x.png")


preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
}


def installed() {
    log.info "Installed with settings: ${settings}"
    initialize()
}


def updated() {
    log.info "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}


def initialize() {
    log.info "There are ${childApps.size()} child apps"
    childApps.each { child ->
    	log.info "Child app: ${child.label}"
    }
}


def installCheck() {         
	state.appInstalled = app.getInstallationState()
	
	if (state.appInstalled != 'COMPLETE') {
		section{paragraph "Please hit 'Done' to install '${app.label}'"}
  	}
  	else {
    	log.info "Parent Installed OK"
  	}
}

def String getFormattedLine(){
	return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
}

def  String getFormattedTitle(String myText){
	return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}


def footer(){
	section() {
		paragraph getFormattedLine()
		paragraph "<div style='color:#1A77C9;text-align:center'>Alexa Fans by Jonathan Porter <br><a href='https://github.com/JonoPorter' target='_blank'>Click here to see what I have written</a></div>"
	}       
}


def mainPage() {
    dynamicPage(name: "mainPage") {
    	installCheck()
		
		if (state.appInstalled == 'COMPLETE') {
			section(getFormattedTitle("${app.label}")) {
				paragraph "An app that will create virtual fans that 'wrap' your physical fans devices, making them more reliable and giving the middle settings a device name."
			}
  			section("<b>Alexa Fans:</b>") {
				app(name: "anyOpenApp", appName: "Alexa Fans Instance", namespace: "jonoporter", title: "<b>Add a new Fan</b>", multiple: true)
			}
			footer()
		}
	}
}


