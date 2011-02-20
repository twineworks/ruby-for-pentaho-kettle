# Copyright (C) 2007, Wes Hays
# All Rights Reserved.

require 'test/helper'
testcase_requires 'roo'

describe 'Ruport::Data::TableFromODS' do
  before(:each) do
    @ods_file = 'test/samples/people.ods'
    @csv_file = 'test/samples/data.csv'
    
    @ods_file_column_names = %w(Name Age DOB)
    @rows = [ ['Andy',    27.0, Date.parse('01/20/1980')], 
              ['Bob',     26.0, Date.parse('02/11/1981')],
              ['Charlie', 20.0, Date.parse('03/14/1987')],
              ['David',   73.0, Date.parse('04/26/1997')] ]
              
    @ods_file_column_names2 = %w(Name Age Pet_Type)
    @rows2 = [ ['Tigger', 3.0,  'Cat'], 
               ['Chai',   4.0,  'Dog'],
               ['Rusky',  6.0,  'Dog'],
               ['Sam',    13.0, 'Dog'] ]            
  end

  # ==== File check ====
  # Raise error if file is not found
  it "should raise if ods file is not found" do
    lambda do
      Ruport::Data::Table.load_ods('people.ods')
    end.should raise_error
  end
  
  # Raise error if file is not found
  it "shouldn't raise if ods file exists" do
    lambda do
      Ruport::Data::Table.load_ods(@ods_file)
    end.should_not raise_error
  end  
  
  
  # ==== Constructor check ====
  it "shouldn't be nil if a ods file is passed" do
    table = Table(@ods_file)
    table.should_not be_nil
  end  
  
  it "shouldn't be nil if a Openoffice object is passed" do
    oo = Openoffice.new(@ods_file)
    oo.default_sheet = oo.sheets.first
    table = Table(oo) # This will be passed to the base Ruport::Data::Table class.
    table.should_not be_nil
  end
  
  it "shouldn't be nil if a Ruport::Data::Table parameter is passed" do
    table = Table(@csv_file) # Pass cs file
    table.should_not be_nil
  end  
  
  
  # ==== Constructor check with options params ====
  it "shouldn't be nil if a ods file is passed with options params" do
    table = Table(@ods_file, {:has_column_names => false})
    table.should_not be_nil
  end  
  
  it "shouldn't be nil if a Openoffice object is passed with options params using parse_ods method" do
    oo = Openoffice.new(@ods_file)
    oo.default_sheet = oo.sheets.first
    table = Ruport::Data::Table.parse_ods(oo, {:has_column_names => false})
    table.should_not be_nil
  end
  
  it "shouldn't be nil if a Ruport::Data::Table parameter is passed with options params" do
    table = Table(@csv_file, {:has_column_names => false}) # Pass cs file
    table.should_not be_nil
  end  
  
  it "should raise if start_row is less than zero" do
    lambda do
      Table(@ods_file, {:start_row => -2})
    end.should raise_error
  end
  
  it "should raise if start_row is greater than the number of rows (starting at 0) in the spreadsheet" do
    lambda do
      Table(@ods_file, {:start_row => 20})
    end.should raise_error
  end  
    
  
  # ==== Table load check ====
  
  # Output:
  # +-----------------------------+
  # | Name    | Age  | DOB        |
  # | Andy    | 27.0 | 1980-01-20 |
  # | Bob     | 26.0 | 1981-02-11 |
  # | Charlie | 20.0 | 1987-03-14 |
  # | David   | 73.0 | 1997-04-26 |
  # +-----------------------------+
  it "table should be valid without column names loaded from ods file starting at the row 1 (index 0) - column names will be data" do
    # Load data from ods file but do not load column headers.
    table = Table(@ods_file, {:has_column_names => false, :start_row => 0})
    table.should_not be_nil
    table.column_names.should == []
    
    # Add headers to the first position
    @rows.insert(0, @ods_file_column_names)    
    
    table.each { |r| r.to_a.should == @rows.shift
                     r.attributes.should == [0, 1, 2] }  
  end
  
  # Output:
  # +-----------------------------+
  # | Bob     | 26.0 | 1981-02-11 |
  # | Charlie | 20.0 | 1987-03-14 |
  # | David   | 73.0 | 1997-04-26 |
  # +-----------------------------+
  it "table should be valid without column names loaded from ods file starting at row 3 (index 2)" do
    # Load data from ods file but do not load column headers.
    # Will start at Row 3 (index 2): ['Bob', 26.0, Date.parse('02/11/1981')]
    table = Table(@ods_file, {:has_column_names => false, :start_row => 2})
    table.should_not be_nil
    table.column_names.should == []
    
    # The header row has not been included yet so don't worry about that one
    # just delete the first row in @rows.
    @rows.delete_at(0) # delete ['Andy', 27.0, Date.parse('01/20/1980')]
    
    table.each { |r| r.to_a.should == @rows.shift
                     r.attributes.should == [0, 1, 2] }  
  end  
  
  # Output:
  # +-----------------------------+
  # | Name    | Age  | DOB        |
  # | Andy    | 27.0 | 1980-01-20 |
  # | Bob     | 26.0 | 1981-02-11 |
  # | Charlie | 20.0 | 1987-03-14 |
  # | David   | 73.0 | 1997-04-26 |
  # +-----------------------------+  
  it "table should be valid without column names loaded from ods file" do
    # Load data from ods file but do not load column headers.
    table = Table(@ods_file, {:has_column_names => false})
    table.should_not be_nil
    table.column_names.should == [] 
    
    # Add headers to the first position
    @rows.insert(0, @ods_file_column_names)
    
    table.each { |r| r.to_a.should == @rows.shift
                     r.attributes.should == [0, 1, 2] }  
  end
  
  # Output:
  # +-----------------------------+
  # |  Name   | Age  |    DOB     |
  # +-----------------------------+
  # | Andy    | 27.0 | 1980-01-20 |
  # | Bob     | 26.0 | 1981-02-11 |
  # | Charlie | 20.0 | 1987-03-14 |
  # | David   | 73.0 | 1997-04-26 |
  # +-----------------------------+
  it "table should be valid with column names loaded from ods file" do
    # Load data from ods file but do not load column headers.
    table = Table(@ods_file)
    table.should_not be_nil
    table.column_names.should == @ods_file_column_names
    
    table.each { |r| r.to_a.should == @rows.shift
                     r.attributes.should == @ods_file_column_names }  
  end  
  
  # Output:
  # +--------------------------+
  # |  Name  | Age  | Pet_Type |
  # +--------------------------+
  # | Tigger |  3.0 | Cat      |
  # | Chai   |  4.0 | Dog      |
  # | Rusky  |  6.0 | Dog      |
  # | Sam    | 13.0 | Dog      |
  # +--------------------------+ 
  it "table should be valid with column names loaded from ods file using Sheet2" do
    # Load data from ods file but do not load column headers.
    table = Table(@ods_file, {:select_sheet => 'Sheet2'})
    table.should_not be_nil
    table.column_names.should == @ods_file_column_names2
    
    table.each { |r| r.to_a.should == @rows2.shift
                     r.attributes.should == @ods_file_column_names2 }  
  end  
  
  # Output:
  # +--------------------------+
  # |  Name  | Age  | Pet_Type |
  # +--------------------------+
  # | Tigger |  3.0 | Cat      |
  # | Chai   |  4.0 | Dog      |
  # | Rusky  |  6.0 | Dog      |
  # | Sam    | 13.0 | Dog      |
  # +--------------------------+
  it "should be valid if an Openoffice object is passed using parse_ods method" do
    oo = Openoffice.new(@ods_file)
    oo.default_sheet = oo.sheets.first
    table = Ruport::Data::Table.parse_ods(oo)
    table.should_not be_nil
    
    table.column_names.should == @ods_file_column_names
    
    table.each { |r| r.to_a.should == @rows.shift
                     r.attributes.should == @ods_file_column_names }    
  end   
  
end

    
    
  