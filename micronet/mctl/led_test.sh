#!/bin/bash 
 TOTAL_ITERATIONS=10000
 COUNTER=0
 SECONDS=0
 SLEEP_TIME=0.01
 while [  $COUNTER -lt $TOTAL_ITERATIONS ]; do
	 echo The counter is $COUNTER
	 #set left LED to red, max brightness
	 ./mctl api 020600ffff0000
	 sleep $SLEEP_TIME
	 #set left LED to off
	 ./mctl api 02060000ff0000
	 sleep $SLEEP_TIME
	 #set center LED to blue, max brightness
	 ./mctl api 020601ff0000ff
	 sleep $SLEEP_TIME
	 #set center LED off
	 ./mctl api 020601000000ff
	 sleep $SLEEP_TIME
	 let COUNTER=COUNTER+1 
 done
 TOTAL_TIME_TAKEN=$SECONDS
 TIME_PER_ITERATION= $(((SECONDS/COUNTER))- SLEEP_TIME)
 echo total time: $SECONDS sec
 echo iterations: $COUNTER
 echo time per iteration: $TIME_PER_ITERATION
