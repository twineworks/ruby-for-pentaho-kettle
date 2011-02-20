###########################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###########################################################################################
# Using error handling
#
# To use Kettle's error handling feature you need to define an error output step the
# usual way: connect the ruby step to the step that should receive rejected rows, then 
# right click the ruby step and select "define error handling". Specify the error fields
# as you like them.
#
# In ruby code use $error.write(err) or the alias $error << err to send an error row hash to
# the error step. Be sure to include the error fields you specified in the error handling 
# dialog. You may also pass entire arrays of rows if you like.
# 
# please see the demo transformation for samples
###########################################################################################

