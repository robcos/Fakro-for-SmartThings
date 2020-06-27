/**
 *  Device Type Definition File
 *
 *  Device Type:		Fakro Skylight
 *  File Name:			fakro-skylight.groovy
 *	Initial Release:	2020-06-27
 *	Author:				Roberto Cosenza
 *
 *  Copyright 2015 SmartThings
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

metadata {
	definition (name: "Fakro Skylight", namespace: "robcos.com", author: "Roberto Cosenza") {
		capability "Switch"
		capability "Actuator"
                       
        fingerprint deviceId: "0x1101", inClusters: "0x72,0x86,0x70,0x85,0x8E,0x26,0x7A,0x27,0x73,0xEF,0x26,0x2B", deviceJoinName: "Fakro Skylight"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}

		main(["switch"])
		details(["switch", "refresh", "levelSliderControl"])
	}
}

/**
 * Mapping of command classes and associated versions used for this DTH
 */
private getCommandClassVersions() {
	[
		0x26: 1,  // Switch Multilevel
		0x70: 2,  // Configuration
		0x72: 2   // Manufacturer Specific
	]
}

def parse(String description) {
	log.debug "Parse description ${description}"

	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, commandClassVersions)
    log.debug "cmd: ${cmd}"
    
    if (cmd) {
        result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
    
    if(result?.descriptionText)
		log.debug "Parse returned ${result?.descriptionText}"
        
	result

}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	log.debug "createEvent 2: ${cmd.toString()} ${item1.toString()}"

	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	log.debug "createEvent 3: ${cmd.toString()}"

	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	log.debug "createEvent 4: ${cmd.toString()}"

	[]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	log.debug "createEvent 5: ${cmd.toString()}"

	[response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	log.debug "createEvent 6: ${cmd.toString()}"

	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	log.debug "createEvent 1: ${cmd.toString()}"
    def result = []
    result << createEvent(name:"switch", value: state.switchState)
	log.debug "createEvent result: ${result.toString()} ${cmd.value}"
    result
}

def createEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled: ${cmd.toString()}"
	[:]
}

def createEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, Map item1) {

	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"

}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.info "on"
    state.switchState = "on"
    setLevel(255)
}

def off() {
	log.info "off"
    state.switchState = "off"
    setLevel(0)
}

def setLevel(value) {
	log.info "setLevel1 value:${value} duration:${duration}"
    def level = Math.min(value as Integer, 99)
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.info "setLevel2 value:${value} duration:${duration}"
    def level = Math.min(value as Integer, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def poll() {
	log.info("poll")
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.info("refresh")
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

 /**
 * Configures the device to settings needed by SmarthThings at device discovery time. Assumes
 * device is already at default parameter settings.
 *
 * @param none
 *
 * @return none
 */
def configure() {
	log.debug "Configuring Device..."
	def cmds = []

	// send associate to group 3 to get sensor data reported only to hub
	cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()

	delayBetween(cmds, 500)
}