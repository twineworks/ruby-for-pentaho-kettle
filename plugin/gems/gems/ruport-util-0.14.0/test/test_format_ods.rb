require 'test/helper'

testcase_requires 'hpricot'

describe 'OSD Formatter' do
  before :all do
    @csv = "first col,second col,third col\n" +
           "first row,cell 1,cell 2\n"        +
           "second row,cell 3,cell 4\n"       +
           "third row,special >,special <\n"  +
           "fourth row,,after empty\n"        +
           "\n"                               +
           "seventh row,nothing,more\n"
    @table = Table(:string => @csv)
    zipfile = @table.to_ods(:tempfile => true)
    zip = Zip::ZipFile.open(zipfile.path)
    @content = zip.read('content.xml')
    zip.close
    @doc = Hpricot(@content)
    @rows = (@doc/'table:table-row')
  end

  it 'should have content' do
    @content.should_not be_nil
  end

  it 'should have all rows' do
    @rows.should have(7).rows
  end

  it 'should have the header' do
    headers = (@rows.first/'text:p').map{|t| t.inner_html}
    headers.should == @table.column_names
  end

  it 'should have the two rows with data' do
    @rows[1..2].zip(@table.data).each do |h_row, r_row|
      (h_row/'text:p').map{ |t| t.inner_html }.should == r_row.to_a
    end
  end

  it 'should escape HTML entities' do
    (@rows[3]/'text:p').map{|e| e.inner_html}.
      should == %w[third\ row special\ &gt; special\ &lt;]
  end
end
