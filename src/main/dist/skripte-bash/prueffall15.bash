#!/bin/bash
source ../../../skripte-bash/einstellungen.sh

echo =================================================
echo =
echo =       Pruefungen SE4 - DUA, SWE 4.8 
echo =
echo =================================================
echo 

index=0
declare -a tests
declare -a testTexts

#########################
# Name der Applikation #
#########################
appname=daufd

########################
#     Testroutinen     #
########################

tests[$index]="tp.TaupunktTest"
testTexts[$index]="Testet die Taupunkt Klasse"
index=$(($index+1))

tests[$index]="stufewfd.WasserFilmDickeStufeTest"
testTexts[$index]="Testet die WasserFilmDickeStufe Klasse"
index=$(($index+1))

tests[$index]="stufesw.SichtWeiteStufeTest"
testTexts[$index]="Testet die SichtWeiteStufe Klasse"
index=$(($index+1))

tests[$index]="stufeni.NiederschlagIntensitaetStufeTest"
testTexts[$index]="Testet die NiederschlagIntensitaetStufe Klasse"
index=$(($index+1))

tests[$index]="stufenaesse.NaesseStufeTest"
testTexts[$index]="Testet die NaesseStufe Klasse"
index=$(($index+1))

tests[$index]="hysterese.HysterezeTester2"
testTexts[$index]="Testet die Hysterese Klasse"
index=$(($index+1))


########################
#      ClassPath       #
########################
cp="../../de.bsvrz.sys.funclib.bitctrl/de.bsvrz.sys.funclib.bitctrl-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-runtime.jar"
cp=$cp":../de.bsvrz.dua."$appname"-test.jar"
cp=$cp":../../junit-4.1.jar"

########################
#     Ausfuehrung      #
########################

for ((i=0; i < ${#tests[@]}; i++));
do
	echo "================================================="
	echo "="
	echo "= Test Nr. "$(($i+1))":"
	echo "="
	echo "= "${testTexts[$i]}
	echo "="
	echo "================================================="
	echo 
	java -cp $cp $jvmArgs org.junit.runner.JUnitCore "de.bsvrz.dua."$appname"."${tests[$i]}
	pause 2
done

exit
