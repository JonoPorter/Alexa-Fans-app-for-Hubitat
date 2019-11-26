# Alexa Fans app for Hubitat
Alexa Fans is a Project to make it quick and easy to generate a wrapper for your Fan Controller that Alexa does not play well with.

## Purpose

Alexa times out quickly on habitat fan controllers making it so using Alexa to control fans is not fun.
Also Alexa only understands Low and High Settings for fans. 
This solves that issue by making a virtual dimmer switch that you set instead of your fan that your fan will mirror. And extra virtual switches for the medium speeds on your fans. 


## Installation

Step 1:
Install AlexaFans.groovy and AlexaFansInstance.groovy as user Apps in Hubitat 
Step 2:
Rename all your current Fans Controllers to something else.
Step 3: 
install Alexa Fans under Apps
Step 4: 
user the app to create the wrapper with the names your fans used to have.
Step 5: 
add “[Fan Name Here]” and “[Fan Name Here] Medium” to the Echo app access list
Step 5.a:
if you have 5 speeds for your fans also add “[Fan Name Here] Medium High” and  “[Fan Name Here] Medium Low”
Step 6: 
Remove the real fans from the Echo app.
Step 7: Create Alexa routines for each of the Medium Virtual Switches to call them with the same name. This will make it so if you say “[Fan Name Here] Medium” or “Turn on [Fan Name Here] Medium” it will work. There you go now enjoy fans the way they should have been in the first place. 

Note: “Set [Fan Name Here] Medium” will not work :\

# CHANGES
1.1:
Added more Robust events making the medium settings change based on state instead of every time something happens.
Made it so whent he medium setting are turned on it updates the virtual switch. 
1.2:
Made updates 2 way ensuring that if the physical fan controller is changed it will change the virtual. 
This ensures alexa will not be confused with a physical fan that is off but a virtual that is on. 