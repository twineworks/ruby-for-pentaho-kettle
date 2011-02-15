###############################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###############################################################################
# This script remembers the previous row, and adds its "testfield" to the
# running row. 
# 

# initialize previous row variable
prev_row ||= nil

# if there's a previous row put its "testfield" value into "prev_value"
$row["prev_value"] = prev_row["testfield"] unless prev_row == nil

# remember this row as the previous row now
prev_row = $row

# evaluate to the row hash
$row