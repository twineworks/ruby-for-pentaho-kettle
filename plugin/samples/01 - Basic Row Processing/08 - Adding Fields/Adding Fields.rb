###############################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
###############################################################################
# When returning row hashes, you may omit all fields that are not mentioned in
# the output fields definition.
#
# The following script replaces the field value of the string field "text" and
# adds some more fields. 
# Only the modified/added fields appear in the returned hash.
# 

{
	"text" => $row["text"].upcase,
	"number_field" => 42.5,
	"integer_field" => 666,
	"date_field" => Time.now
}