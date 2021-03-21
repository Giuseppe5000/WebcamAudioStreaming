#!/bin/sh

if [ "$1" == "client" ]; then
  java -jar -Djava.library.path=opencv-linux-4.5.1/lib out/artifacts/Client_jar/Client.jar
elif [ "$1" == "server" ]; then
  java -jar -Djava.library.path=opencv-linux-4.5.1/lib out/artifacts/Server_jar/Server.jar
else
  echo "./run.sh: <args> missing, args=client/server " >&2
fi