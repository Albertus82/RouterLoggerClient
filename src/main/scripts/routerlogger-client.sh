#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx32m -classpath "$PRGDIR/routerlogger-client.jar:$PRGDIR/lib/*" it.albertus.router.client.RouterLoggerClient
  else java -Xms4m -Xmx32m -classpath "$PRGDIR/routerlogger-client.jar:$PRGDIR/lib/*" it.albertus.router.client.RouterLoggerClient
fi
