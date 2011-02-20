require 'ruport'

# === Overview
#
# This class extends the core class Ruport::Data::Table and adds support for loading Openoffice
# spreadsheet files using roo. The idea is to get data from speadsheets that may contain
# already calculated values entered by non-programmers.
#
# Once your data is in a Table object, it can be manipulated
# to suit your needs, then used to build a report.
#
# Copyright (C) 2007, Wes Hays
# All Rights Reserved.
#
class Ruport::Data::Table

  # === Overview
  #
  # This module provides facilities for creating tables from Openoffice spreadsheet file (ods).
  #
  module FromODS
    # Loads a ods file directly into a Table using the roo library.
    #
    # Example:
    #   
    #   # Load data from Openoffice ods file with defaults
    #   table = Table.load_ods('myspreadsheet.ods')
    #
    #   # do not assume the data has column names - default is false.
    #   table = Table.load_ods('myspreadsheet.ods',{:has_column_names => false})
    #
    #   # Select sheet - default is the first sheet.
    #   table = Table.load_ods('myspreadsheet.ods', {:select_sheet => 1})
    #
    #   # Start row - default is the first row. Use this to override where
    #                 the first row should start.
    #   table = Table.load_ods('myspreadsheet.ods', {:start_row => 1})
    #
    def load_ods(ods_file, options={})
      get_table_from_ods_file(ods_file, options)
    end

    # Creates a Table from an Openoffice object (from roo library). 
    #
    # Example:
    #   
    #   # parse openoffice object with defaults. 
    #   table = Table.parse_ods(openoffice_object)
    #
    #   # do not assume the data has column names.
    #   table = Table.parse_ods(openoffice_object,{:has_column_names => false})
    #
    #   # Select sheet - default is the first sheet.
    #   table = Table.parse_ods(openoffice_object, {:select_sheet => 1})
    #
    #   # Start row - default is the first row. Use this to override where
    #                 the first row should start.
    #   table = Table.parse_ods('myspreadsheet.ods', {:start_row => 1})    
    #
    def parse_ods(ods_object, options={})
      get_table_from_ods(ods_object, options)
    end      

    private

    def get_table_from_ods_file(ods_file, options) #:nodoc:
      require 'roo'
      oo = Openoffice.new(ods_file)
      get_table_from_ods(oo, options)
    end

    def get_table_from_ods(oo, options) #:nodoc:
      options = {:has_column_names => true, 
                 :select_sheet => oo.sheets.first,
                 :start_row => 0}.merge(options)        
      oo.default_sheet = options[:select_sheet]
      
      options[:start_row] = options[:start_row].to_i + 1 unless options[:start_row].nil?      
      start_row = options[:start_row]

      raise 'start_row must be greater than or equal to zero' if options[:start_row].to_i < 0
      
      last_row_index_zero = oo.last_row - 1
      raise "start_row must be less than or equal to #{last_row_index_zero}" if !oo.last_row.nil? and 
                                                                                (options[:start_row].to_i > oo.last_row)
                                                                                
      table = self.new(options) do |feeder|            
        
        if options[:has_column_names] == true
          feeder.data.column_names = oo.row(start_row) 
          start_row = start_row + 1
        end
        
        unless oo.last_row.nil?
          start_row.upto(oo.last_row) do |row|
            tempArr = []
            1.upto(oo.last_column) do |col|
              tempArr << oo.cell(row,col)
            end
            feeder << tempArr
          end 
        end
        
      end

      return table     
    end

  end

  extend FromODS

end
    

module Kernel
  
  alias :RuportTableMethod :Table
  
  # Updates the Ruport interface for creating Data::Tables with
  # the ability to pass in a ODS file or Roo Openoffice object.
  #
  #   t = Table("myspreadsheet.ods")
  #   t = Table("myspreadsheet.ods", :has_column_names => true)
  def Table(*args,&block)
    table=
    case(args[0])
    when /\.ods/
      Ruport::Data::Table.load_ods(*args)
    else
      RuportTableMethod(*args,&block)
    end             
    
    return table
  end
end  
