# (Demo) Speech2TextApp
## an Android app developed to try SpeechToText API.
#
# How to use
#
### Click the start button and it starts listening. With a signal tone, a Toast text appears "Je vous écoute" meaning "I'm listening" in French.

### You can start to speak. Your speech is sent to the server. After few seconds, your speech appears on the screen. 
#### During my test, the speech recognition was good with a real device with a French language module downloaded.
#### On an emulater, it often encounters wrong recognition as shown in the image below. 

<img src="https://cloud.githubusercontent.com/assets/21304543/23910812/3bbba10e-08db-11e7-8562-74f8dc2e3336.png" width="400"/>

## Sometime, it cannot recongnize at all and shows a Toast message "Error_No_Match".

<img src="https://cloud.githubusercontent.com/assets/21304543/23910813/3bcb316e-08db-11e7-9487-05b888dbbfd9.png" width="400"/>

### It continues to listen in loop until the command *"Bye, Bye"* to finish the listening is pronounced.
### Another commande *"Ok, google"* makes the app read loud texts written on the screen.


## SDK API 19 or later required

## Technology included
SpeechToText, TextToSpeech

