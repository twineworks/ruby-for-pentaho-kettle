###################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###################################################################################
# Reading from info steps
#
# If you'd like to read rows from specific info steps, you need to first connect
# all info steps to your ruby step. Then assign tags to each info step on the info
# steps tab. The info steps will be known by the tag you assign to them in the
# script. They $info_steps hash (indexed by tag) allows you to read from the
# specific steps. If you tag an info step with "foo", you can read rows from it 
# like by calling read or read_all like this:
# 
# $info_steps["foo"].read() reads a single row from the info step and returns it
# as a hash indexed by field name. It returns nil if there are no more rows in
# the info stream.
# 
# $info_steps["foo"].read(n) reads n rows from the info step and returns them in
# an array. If the info stream has less than n rows, it returns a smaller array.
# If the input stream does not have any more rows it returns nil.
#
# $info_steps["foo"].read_all() returns all rows from the info step in an array.
# It returns an empty array if there are no rows in the info stream.
#
# Please see the demo transformations for samples.
#
# NOTE:
# If you mix normal input steps and info steps, all rows from the info steps will
# be loaded into memory before step execution. This happens transparently in the 
# background and is due to a limitation of the Kettle 4.x API. The "Stream Lookup"
# step is an example for this setup. Should you have a lot of rows provided by
# your info steps, you should make *all* input steps info steps. If *all* incoming
# steps are info steps, the above limitation does not apply. In this case the rows
# will be loaded when needed. The "Merge Join" step is an example for this setup.
###################################################################################
