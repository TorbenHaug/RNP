#!/bin/sh
Executable="Aufgabe3Client.jar"
ServerAdress="127.0.0.1"
ServerPort=23000
LocalFile="/home/torbenhaug/Studium/RNP/Aufgabe3/FCdata.pdf"
RemoteFile="/home/torbenhaug/Studium/RNP/Aufgabe3/FCdata2.pdf"

java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 1 10
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 1 100
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 1 1000
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 10 10
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 10 100
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 10 1000
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 100 10
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 100 100
sleep 5
java -jar $Executable $ServerAdress $ServerPort $LocalFile $RemoteFile 100 1000

