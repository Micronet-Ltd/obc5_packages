#!/bin/bash 
 COUNTER=0
 SLEEP_TIME=0.5
 while [  $COUNTER -lt 1000 ]; do
	 echo The counter is $COUNTER
	 #get rtc time
	 ./mctl api 020b
	 sleep $SLEEP_TIME
	 #set rtc time
	 #./mctl api 020c
	 let COUNTER=COUNTER+1 
 done