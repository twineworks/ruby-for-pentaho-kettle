###########################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###########################################################################################
# Writing to the output stream
#
# If you'd like to generate a row manually during script execution, you can do so
# by calling $output.write(out) or the << alias: $output << out
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
# $output.write nil                             - writes nothing (NOP)
# $output.write {}                              - writes a row with all fields set to null
# $output.write {"num" => 42}                   - writes a row with field "num" set to 42
# $output.write [{"num" => 42}, {"num" => 92}]  - writes two rows with the field "num" 
#                                                 set to 42 and 92 respectively.
#
# Please note that you can use the << alias as well:
#
# $output << nil                             - writes nothing (NOP)
# $output << {}                              - writes a row with all fields set to null
# $output << {"num" => 42}                   - writes a row with field "num" set to 42
# $output << [{"num" => 42}, {"num" => 92}]  - writes two rows with the field "num" 
#                                              set to 42 and 92 respectively.
#
# Please see the demo transformations for samples.
###########################################################################################
