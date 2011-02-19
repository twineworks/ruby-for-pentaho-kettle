###########################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###########################################################################################
# Using target steps
#
# If you'd like to send output rows to specific steps, you can do so by using target 
# steps. This works similarly to the switch/case step. First connect your ruby step to  
# all possible output steps. Next open the "Target Steps" tab of the ruby step and assign 
# a "tag" to each target step. The tag is the name by which your script can refer to a 
# particular target step.  
#
# The target steps are made available in the $target_steps hash, indexed by the tag value
# you assigned to each target step.
#
# Assuming there's a target step tagged "foo", you can send rows to it by calling its
# write method or << alias like this:
# 
# $target_steps["foo"].write(out)
# $target_steps["foo"] << out
#
# The out argument may be:
#
# nil      - no row will be written
# a hash   - in this case a single row will be written, the keys (strings) are
#            interpreted as field names 
# an array - in this case every entry is required to be a hash or nil. Hashes
#            are written as rows, nil values are skipped
#
# Sample invocations:
# $target_steps["foo"].write nil                             - writes nothing (NOP)
# $target_steps["foo"].write {}                              - writes a row with all fields set to null
# $target_steps["foo"].write {"num" => 42}                   - writes a row with field "num" set to 42
# $target_steps["foo"].write [{"num" => 42}, {"num" => 92}]  - writes two rows with the field "num" 
#                                                              set to 42 and 92 respectively.
#
# Please note that you can use the << alias as well:
#
# $target_steps["foo"] << nil                             - writes nothing (NOP)
# $target_steps["foo"] << {}                              - writes a row with all fields set to null
# $target_steps["foo"] << {"num" => 42}                   - writes a row with field "num" set to 42
# $target_steps["foo"] << [{"num" => 42}, {"num" => 92}]  - writes two rows with the field "num" 
#                                                           set to 42 and 92 respectively.
#
# Please see the demo transformations for samples.
###########################################################################################