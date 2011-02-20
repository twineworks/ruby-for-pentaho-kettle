require 'test/helper'
require 'net/smtp'

class SampleReport < Ruport::Report
  renders_with Ruport::Controller::Table

  def renderable_data(format)
    Table(%w[not abc]) << %w[o r] << %w[one two] << %w[thr ee]
  end
end

class MyReport < Ruport::Report

  attr_accessor :data
  
  def renderable_data(format)
    data
  end

end

describe 'Report essentials' do
  before :each do
    @report = Ruport::Report.new
  end

  it 'should render with shortcuts' do
    a = SampleReport.new
    csv = "not,abc\no,r\none,two\nthr,ee\n"

    a.to_csv.should == csv

    SampleReport.generate { |r| r.to_csv.should == csv }
  end

end

describe 'Writing Report to files' do
  def file_mock(mode, filename, content)
    file = mock("File #{filename}")

    file.should_receive(:<<).
      with(content).and_return(file)

    File.should_receive(:open).
      with(filename, mode).once.
      and_yield(file)
  end

  before :each do
    @report = SampleReport.new
  end

  it 'should write correctly to files' do
    file_mock(mode = 'w', filename = 'foo.csv', 
                          content = "not,abc\no,r\none,two\nthr,ee\n")
    @report.save_as(filename).should_not be_nil
  end   
  
  it 'should yield the controller object' do
    file_mock(mode = 'w', filename = 'foo.csv', 
                          content = "not,abc\no,r\none,two\nthr,ee\n")
    @report.save_as(filename) do |r|
      r.should be_an_instance_of(Ruport::Controller::Table)
    end
  end
end

describe 'MyReport rendering' do
  before :all do
    @table = [[1,2,3],[4,5,6]].to_table(%w[a b c])
  end

  before :each do
    @my_report = MyReport.new
  end

  def generate(table)
    @my_report.data = table
  end

  it 'should renders_with Controller::Table' do
    MyReport.renders_with Ruport::Controller::Table
    generate @table
    @my_report.to_csv.should == "a,b,c\n1,2,3\n4,5,6\n"
  end

  it 'should renders_with Controller::Table and optional headers' do
    MyReport.renders_with Ruport::Controller::Table, :show_table_headers => false
    generate @table

    @my_report.to_csv.should == "1,2,3\n4,5,6\n"
    @my_report.to_csv(:show_table_headers => true).should == "a,b,c\n1,2,3\n4,5,6\n"
  end

  it 'should render as table' do
    MyReport.renders_as_table
    generate @table

    @my_report.to_csv.should == "a,b,c\n1,2,3\n4,5,6\n"
  end

  it 'should render as row' do
    MyReport.renders_as_row
    generate @table[0]

    @my_report.to_csv.should == "1,2,3\n"
  end

  it 'should render as group' do
    MyReport.renders_as_group
    generate @table.to_group('foo')

    @my_report.to_csv.should == "foo\n\na,b,c\n1,2,3\n4,5,6\n"
  end

  it 'should render as grouping' do
    MyReport.renders_as_grouping
    generate Grouping(@table, :by => 'a')

    @my_report.to_csv.should == "1\n\nb,c\n2,3\n4\n\nb,c\n5,6\n\n"
  end
end
