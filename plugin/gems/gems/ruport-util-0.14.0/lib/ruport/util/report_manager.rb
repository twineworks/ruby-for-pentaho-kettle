module Ruport
  class ReportManager  

    def self.add_model(*new_models)
       self.models |= new_models
    end   
    
    def self.add_report(*new_reports)
      self.reports |= new_reports
    end

    def self.[](name)     
      (reports + models).find{|n| n.name == name }
    end
  
    def self.models
      @models ||= []
    end      
    
    def self.reports
      @reports ||= []
    end
  
    class << self
      attr_writer :models 
      attr_writer :reports 
    end
  
  end          
end

if defined? ActiveRecord
  class ActiveRecord::Base
    def self.acts_as_managed_report
      Ruport::ReportManager.add_model(self)
    end     
  end
end          

class Ruport::Report
  def self.acts_as_managed_report
    Ruport::ReportManager.add_report(self)
  end
end
