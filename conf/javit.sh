#!/bin/sh
#
# simple startup shell for javit
#
# its always a good idea to set up JAVA_HOME
# in your env to make java happy
#
# otherwise its best to make the java cmd 
# executed in this script an absolute path
#



java -Xmx2048m -cp ".:lib/*"  net.nexxus.gui.Main

