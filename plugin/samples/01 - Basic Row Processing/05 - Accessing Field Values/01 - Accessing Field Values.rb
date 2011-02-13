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
# This script converts the incoming string field "text" to uppercase and saves
# it in the new field "text_upper". 

# calculate upper case version of text
$row["text_upper"] = $row["text"].upcase()

# make sure the script evaluates to a row hash
$row
