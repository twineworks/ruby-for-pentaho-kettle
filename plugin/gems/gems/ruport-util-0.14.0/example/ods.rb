require 'ruport'
require "ruport/util"

csv = %{
first col,second col,third col
first row,cell 1,cell 2
second row,cell 3,cell 4
}.strip


f = Table(:string => csv).to_ods(:tempfile => true)
FileUtils.cp(f.path,"out.ods")
