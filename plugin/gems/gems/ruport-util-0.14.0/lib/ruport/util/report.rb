# report.rb : High Level Interface to Ruport
#
# Author: Gregory Brown
# Copyright 2006, All Rights Reserved
#
# This is Free Software.  See LICENSE and COPYING files for details.

#load the needed standard libraries.
%w[erb yaml date logger fileutils].each { |lib| require lib }
require "forwardable"

module Ruport

  # === Overview
  #
  # The Ruport::Report class provides a high level interface to much of Ruport's
  # functionality.  It is designed to allow you to build and run reports easily.
  # If your needs are complicated, you will probably need to take a look at the
  # individual classes of the library, but if they are fairly simple, you may be
  # able to get away using this class alone. 
  #
  # Ruport::Report is primarily meant to be used with Ruport's code generator, 
  # rope, and is less useful when integrated within another system, such as
  # Rails or Camping.
  #
  # Below is a simple example of loading a report in from a CSV, performing a
  # grouping operation, and then rendering the resulting PDF to file.
  #
  #   require "rubygems"
  #   require "ruport"
  #   class MyReport < Ruport::Report 
  #
  #     renders_as_grouping(:style => :inline)   
  #
  #     def renderable_data(format)
  #       table = Table("foo.csv")
  #       Grouping(table, :by => "username")
  #     end  
  #
  #   end   
  #
  #   report = MyReport.new
  #   report.save_as("bar.pdf")
  #
  class Report   
    extend Forwardable
    include Controller::Hooks
        
    # This is a simplified interface to Ruport::Query.
    #
    # You can use it to read SQL statements from file or string:
    #  
    #   #from string 
    #   result = query "select * from foo"
    #
    # You can use multistatement SQL:
    #
    #   # will return the value of the last statement, "select * from foo"
    #   result = query "insert into foo values(1,2); select * from foo"
    # 
    # You can iterate by row:
    #  
    #   query("select * from foo") { |r|
    #     #do something with the rows here
    #   }
    # 
    # query() can return raw DBI:Row objects or Ruport's data structures:
    # 
    #   # will return an Array of DBI::Row objects
    #   result = query "select * from foo", :raw_data => true
    #
    #
    # See Ruport::Query for details.
    #
    def query(sql, options={})
      options[:source] ||= :default        
      q = Ruport::Query.new(sql, options)
      if block_given?
        q.each { |r| yield(r) }
      else
        q.result
      end
    end
    
    def renderable_data(format)
       raise NotImplementedError, 
          "You must implement renderable_data(format) if you wish to use as()
          or save_as()"
    end

    alias_method :old_as, :as
    
    def as(*args,&block) 
      prepare if respond_to?(:prepare)
      output = old_as(*args,&block)
      cleanup if respond_to?(:cleanup)
      return output
    end
    
    def save_as(filename,options={},&block)
      formats = { "csv" => ["w",:csv], "txt" => ["w",:text], 
                  "html" => ["w", :html], "pdf" => ["wb", :pdf ] }
      
      fo = filename =~ /.*\.(.*)/ && formats[$1]
      flags = options.delete(:flags)
      if fo
        File.open(filename,flags || fo[0]) { |f| f << as(fo[1],options,&block) }
      else
        File.open(filename,flags || "w") { |f| f << as($1.to_sym,options,&block) }
      end
    end

    def method_missing(id,*args,&block) 
      if id.to_s =~ /^to_(.*)/
        as($1.to_sym,*args,&block)
      else
        super
      end
    end

    def add_source(*args) 
      Ruport::Query.add_source(*args)
    end

    def self.generate
      yield(new)
    end
  end
end
