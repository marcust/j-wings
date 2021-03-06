# source this to get your environment set up
# adjust JAVA_HOME and JSDK_HOME to fit your needs

JAVA_HOME=/usr/local/jdk1.3
J2EE_HOME=/usr/local/j2sdkee1.2.1
WINGS_HOME=/home/hengels/wings

DEVEL_HOME=`pwd`

CLASSPATH=$JAVA_HOME/jre/lib/rt.jar:$J2EE_HOME/lib/j2ee.jar
CLASSPATH=$CLASSPATH:$DEVEL_HOME/src:$DEVEL_HOME/demo:$WINGS_HOME/src

PATH=$PATH:$JAVA_HOME/bin
JAVA=$JAVA_HOME/bin/java

export JAVA JAVA_HOME PATH DEVEL_HOME CLASSPATH
