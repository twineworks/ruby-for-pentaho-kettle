###################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###################################################################################
# Reading from the input stream
#
# If you'd like to read rows from the input stream, you can call $input.read or
# $input.read_all
# 
# $input.read() reads a single row from the input stream and returns it as a hash
# indexed by field name. It returns nil if there are no more rows in the input
# stream.
# 
# $input.read(n) reads n rows from the input stream and returns them in an array.
# If the input stream has less than n rows, it returns a smaller array. If the
# input stream does not have any more rows it returns nil.
#
# # $input.read_all() returns all rows from the input stream in an array. It
# returns an empty array if there are no rows in the input stream.
#
# Please see the demo transformations for samples.
###################################################################################
