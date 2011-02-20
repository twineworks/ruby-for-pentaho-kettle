require "ruport"
require "ruport/util"

class MyReport < Ruport::Report
  
  acts_as_managed_report 
   
  def content
     "Hello Mike"
  end
  
end      

class YourReport < Ruport::Report
  
  acts_as_managed_report
  
  def content
    "Hello Joe"
  end 
  
end  

class HisReport < Ruport::Report
  
  acts_as_managed_report
  
  def content
    "Hello Robert"
  end 
  
end

%w[MyReport YourReport HisReport].each do |report|
  puts Ruport::ReportManager[report].generate { |r| r.content }
end
