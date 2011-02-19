###################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###################################################################################
# To add more script tabs, right click the script tabs bar and 
# choose "Add Script" from the context menu.
#
# Each script tab has a type associated with it. New tabs are always created as 
# "Lib Scripts". Types can be assigned by selecting the tab in question, right 
# clicking it and choosing another type from the list. 
# 
# The following script types are available: 
#
# Row Script   - Row scripts are executed once for each input row. If the 
#                ruby step has no input steps, the script is executed just once. 
#                Each ruby step must have exactly one script with this type. If you
#                assign this type to a script, any existing row script will 
#                turn into a lib script automatically.
#
# Start Script - Start scripts are executed once before any other script tab
#                runs. They are guaranteed to execute, even if there are no input
#                rows. Start scripts are used to set up complex objects or 
#                to allocate resources like database connections or file handles
#                that are used by the row script for row processing. There may 
#                be only one start script defined in any ruby step. Consider
#                including lib scripts if you'd like splitting startup code 
#                across multiple tabs.
#
# End Script   - End scripts are executed after the last row has been processed
#                by the row script. They are guaranteed to execute even if there
#                are no input rows. End scripts are usually used to release 
#                resources acquired by a start script, or to generate summary rows.
#                There may be only one end script defined in any ruby step.
#                Consider including lib scripts if you'd like splitting the code
#                across multiple tabs.
#
# Lib Script   - Lib scripts are not executed unless specifically included by some
#                other script. 
#                 
#                All script tabs are made available as the $tabs hash. It is 
#                indexed by tab title. Script tabs can be included in two different
#                ways.
#
#                --- Including a script tab multiple times ---
#                A call to $tabs["My Lib Script"].load() causes the script
#                tab named "My Lib Script" to be evaluated immediately. Each call
#                to load() will cause another evaluation of the script tab.
# 
#                --- Including a script once ---
#                A call to $tabs["My Lib Script"].require() causes the script
#                to be evaluated once. Subsequent calls to require() have no effect.
#
# Please note that script tabs do not share local variable scope. If you need to
# share data between tabs you should use global variables.
#
# See "Using Start and End Scripts" and "Using Lib Scripts" for samples
###################################################################################