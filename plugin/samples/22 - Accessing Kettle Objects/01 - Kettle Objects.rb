###########################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###########################################################################################
# Using Kettle Objects
#
# To use some of Kettle's features like logging or Kettle variables the ruby step makes
# the $step and $trans objects available. 
#
# The $step object is the java instance of the ruby step. It inherits from the BaseStep
# class, and you'd usually use it to do logging and variable resolution.
#
# The $trans object is the java instance of the Trans class, which represents the running
# transformation. You'd usually use this to find information about other steps, 
# defined database connections, etc.
#
# Please refer to the kettle javadoc and kettle sources for details.
# http://javadoc.pentaho.com/kettle/
# 
# See the demo transformation for samples of common uses.
###########################################################################################