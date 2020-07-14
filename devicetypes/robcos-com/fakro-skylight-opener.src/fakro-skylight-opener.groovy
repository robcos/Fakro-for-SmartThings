/**
 *	Fakro Skylight Opener
 *
 *	Copyright 2020 robcos.com
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 
 *  http://manuals-backend.z-wave.info/make.php?lang=en&sku=ZWS12%20RU&cert=ZC10-18036065
 */
metadata {
	definition (name: "Fakro Skylight Opener", namespace: "robcos.com", author: "Roberto Cosenza", ocfDeviceType: "oic.d.garagedoor") {
		capability "Switch"
        capability "Actuator"

		capability "Door Control"
		capability "Contact Sensor"
		capability "Sensor"
	}
}

import physicalgraph.zwave.commands.barrieroperatorv1.*

/**
 * Mapping of command classes and associated versions used for this DTH
 */
private getCommandClassVersions() {
	[
        0x26: 1,  // Switch Multilevel
	]
}

def parse(String description) {
	log.trace("parse ${description}")
	def result = null
    def cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        result = zwaveEvent(cmd)            
    }
	log.debug "\"$description\" parsed to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	log.trace("zwaveEvent SwitchMultilevelReport: ${cmd}")
    updateTile()
}

def updateTile() {
	logger.trace("updateTile")
    def result = []
    log.debug("Setting contact and door to ${state.switchState}")
  	result << createEvent(name: "contact", value: state.switchState, displayed: false)
    result << createEvent(name: "door", value: state.switchState, displayed: false)
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace("zwaveEvent Command: ${cmd}")
    updateTile()
}

// Google Assistant needs the on/off pair as it treats it like a switch.
def on() {
	log.trace("on")
    open()
}

def off() {
	log.trace("off")
    close()
}

// The SmartThings app calls on/off when tapping on the door icon.
def open() {
	log.trace("Open")
	state.switchState = "open"
	setLevel(255)    
}

def close() {
	log.trace("Close")
	state.switchState = "closed"
	setLevel(0)  
}

def setLevel(level) {
	log.trace "setLevel value:${level}"
	delayBetween ([
	    zwave.basicV1.basicSet(value: level).format(), 
    	zwave.switchMultilevelV3.switchMultilevelGet().format(),    
    ], 2000) 
}