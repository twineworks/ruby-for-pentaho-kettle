###############################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###############################################################################
# Each processed row is passed to the ruby script as a ruby hash indexed by
# field name. It is made available as the global variable $row. 
#
# A ruby script is expected to evaluate to one of the following:
#    
#    nil    - in this case no output row is produced
#
#    hash   - the hash represents the output row and should contain all
#             specified output fields indexed by field name
#
#    array  - the array may contain nil values or hashes. Each hash value
#             produces an output row
###############################################################################
#     
# The following script implements a simple counter. Make sure to specify the 
# output field "counter" of type "Integer" to see the new field in the output. 

# initialize counter variable
my_counter ||= 0

# increment counter variable
my_counter += 1

# set field counter to the value of counter variable
$row["counter"] = my_counter

# make sure the script evaluates to a row hash
$row
