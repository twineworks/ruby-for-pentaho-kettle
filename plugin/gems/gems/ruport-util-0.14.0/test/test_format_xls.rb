require 'test/helper'
require 'rexml/document'
testcase_requires 'hpricot'  
testcase_requires 'spreadsheet/excel'

describe 'XLS Formatter' do
  before :all do
    @csv = "first col,second col,third col\n" +
           "first row,cell 1,cell 2\n"        +
           "second row,cell 3,cell 4\n"       +
           "third row,special >,special <\n"  +
           "fourth row,,after empty\n"        +
           "\n"                               +
           "seventh row,nothing,more\n"
    @table = Table(:string => @csv)
    @xlsxml = Hpricot(@table.to_xlsxml())
    @xls = @table.to_xls()
    @xlsx = @table.to_xlsx(:tempfile => true)
    zip = Zip::ZipFile.open(@xlsx.path)
      @xlsx_content = Hpricot(zip.read('xl/worksheets/sheet1.xml'))
    zip.close
    @xlsx_rows = @xlsx_content.search('//row')
  end

  it 'should have content' do
    @xlsxml.should_not be_nil
    @xls.should_not be_nil
    @xlsx.should_not be_nil
  end

  it 'should have all rows' do
    #@xlsxml.search('//row').should have(7).rows
    @xlsx_rows.should have(7).rows
  end
end
