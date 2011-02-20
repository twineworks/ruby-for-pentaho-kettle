require 'cgi'
require 'tmpdir'
require 'zip/zip'

module Ruport
  # This class provides Excel output for Ruport's Table controllers.
  # It can export to format :
  #  * Excel 2003 (use spreadsheet/excel gems)
  #  * Excel 2003 XML
  #  * Excel 2007 OpenXML
  # 
  # === Rendering Options
  #     * worksheet_name : Name of the Worksheet
  #     * Renders
  #       * xls => Excel 2003 (If spreadsheet/excel no exist use xml format instead)
  #       * xlsx => Excel 2007 OpenXML
  #       * xlsxml => Excel 2003 XML
  #
  class Formatter::XLS < Formatter
    renders :xls, :for => [Controller::Row, Controller::Table]

    def initialize
      Ruport.quiet {
        require 'spreadsheet/excel'
      }
    end


    def prepare_table
      @xls_row = 0
      @tempfile = Tempfile.new('output.xls')
      @workbook = Spreadsheet::Excel.new(@tempfile.path)
      @worksheet = @workbook.add_worksheet(options.worksheet_name || 'Ruport')
      @header_style = options.header_style || @workbook.add_format(:bold => 1, :size => 12)
    end

    def build_table_header
      if options.show_table_headers
        table_row { build_cells(data.column_names, @header_style) }
      end
    end

    def build_table_body
      data.each do |r|
          table_row { build_cells(r) }
      end
    end

    def build_row
      table_row{ build_cells(data.to_a) }
    end
    
    def table_row
      yield
      @xls_row += 1
    end

    def build_cells(values, style = nil)
      col = 0
      values.each do |value|
        if style
          @worksheet.write(@xls_row, col, value, style)
        else
          @worksheet.write(@xls_row, col, value)
        end
        col += 1
      end
    end

    def finalize_table
      @workbook.close
      options.io =
        if options.tempfile
          @tempfile
        else
          File.read(@tempfile.path)
        end
    end
  end  
  
  # Excel 2007 OpenXML
  class Formatter::XLSX < Formatter
    BLANK_XLSX = File.join(Ruport::Util::BASEDIR, 'example', 'data', 'blank.xlsx')
    renders :xlsx, :for => [ Controller::Row, Controller::Table]

    def initialize
    end
    
 
   
    def prepare_table
      @xls_row = 0
      output << %{<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xml:space="preserve" xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
 <sheetPr codeName="#{options.worksheet_name || 'Ruport'}"/>

 <sheetViews>
  <sheetView tabSelected="1" workbookViewId="0">
   <selection/>
  </sheetView>
 </sheetViews>
 <sheetFormatPr defaultRowHeight="12.75"/>
<cols>
      }
      data.column_names.size.times {
        output << %{ <col min="1" max="1" width="10" customWidth="true"/>}
      }
      output << %{</cols><sheetData>}
      @strings = []
    end

    def build_table_header
      if options.show_table_headers
        table_row { build_cells(data.column_names, 'Heading') }
      end
    end

    def build_table_body
      data.each do |r|
          table_row { build_cells(r) }
      end
    end

    def build_row
      table_row{ build_cells(data.to_a) }
    end

    def table_row
      output << %{        <row r="#{@xls_row + 1}">\n}
      yield
      output << %{        </row>\n}
      @xls_row += 1
    end

    def get_cell_name(row, col)
      name = ((col % 26) + 65).chr + row.to_s
      name = ((col / 26) + 65).chr + name if (col / 26 != 0)
      name
    end

    def build_cells(values, style = '')
      col = 0
      values.each do |value|
        value = CGI.escapeHTML(value.to_s)
        id = @strings.length
        @strings.push(value)
        output << %{<c r="#{get_cell_name(@xls_row + 1, col)}" t="s">
    <v>#{id}</v>
   </c>}
        col += 1
      end     
    end

    def build_strings_file
      out = ''
      out << %{<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" uniqueCount="#{@strings.length}">\n}
      @strings.each {|val|
        out << %{  <si><t>#{val}</t></si>\n} 
      }
      out << %{</sst>\n}
      out
    end
    
    def finalize_table
      output << %{</sheetData>
 <sheetProtection sheet="false" objects="false" scenarios="false" formatCells="false" formatColumns="false" formatRows="false" insertColumns="false" insertRows="false" insertHyperlinks="false" deleteColumns="false" deleteRows="false" selectLockedCells="false" sort="false" autoFilter="false" pivotTables="false" selectUnlockedCells="false"/>
 <printOptions gridLines="false" gridLinesSet="true"/>
 <pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/>

 <pageSetup paperSize="1" orientation="default"/>
 <headerFooter differentOddEven="false" differentFirst="false" scaleWithDoc="true" alignWithMargins="true">
  <oddHeader></oddHeader>
  <oddFooter></oddFooter>
  <evenHeader></evenHeader>
  <evenFooter></evenFooter>
  <firstHeader></firstHeader>
  <firstFooter></firstFooter>
 </headerFooter>

</worksheet>}

      @tempfile = Tempfile.new('output.xlsx')

      File.open(BLANK_XLSX) { |bo| 
        @tempfile.print(bo.read(1024)) until bo.eof? 
      }
      @tempfile.close
      zip = Zip::ZipFile.open(@tempfile.path)
      zip.get_output_stream('xl/worksheets/sheet1.xml') do |cxml|
        cxml.write(output)
      end
      zip.get_output_stream('xl/sharedStrings.xml') do |cxml|
        cxml.write(build_strings_file)
      end
      workbook = %{<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xml:space="preserve"  xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<fileVersion appName="xl" lastEdited="4" lowestEdited="4" rupBuild="4505"/>
<workbookPr codeName="ThisWorkbook"/>
	<bookViews>
<workbookView activeTab="0" autoFilterDateGrouping="1" firstSheet="0" minimized="0" showHorizontalScroll="1" showSheetTabs="1" showVerticalScroll="1" tabRatio="600" visibility="visible"/>
</bookViews>
	<sheets>
<sheet name="#{options.worksheet_name || 'Ruport'}" sheetId="1" r:id="rId4"/>
</sheets>
<definedNames/>
<calcPr calcId="124519" calcMode="auto" fullCalcOnLoad="1"/>
</workbook>}
      zip.get_output_stream('xl/workbook.xml') do |cxml|
        cxml.write(workbook)
      end
      zip.close
      options.io =
        if options.tempfile
          @tempfile
        else
          File.read(@tempfile.path)
        end
    end
  end
  
  # Excel 2003 XML
  class Formatter::XLSXML < Formatter
    renders :xlsxml, :for => [ Controller::Row, Controller::Table]
   
    def prepare_table
     output << %{<?xml version="1.0" encoding="UTF-8"?><?mso-application progid="Excel.Sheet"?>
    <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
          xmlns:o="urn:schemas-microsoft-com:office:office"
          xmlns:x="urn:schemas-microsoft-com:office:excel"
          xmlns:html="http://www.w3.org/TR/REC-html40"
          xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
      <Styles>
        <Style ss:ID="Default" ss:Name="Default"/>

        <Style ss:ID="Heading" ss:Name="Heading">#{options.header_style || '
          <Alignment ss:Horizontal="Center"/>
          <Font ss:Bold="1" ss:Italic="1" ss:Size="12"/>'}
        </Style>
        <Style ss:ID="co1"/>
        <Style ss:ID="ta1"/>
      </Styles>
      <ss:Worksheet ss:Name="#{options.worksheet_name || 'Ruport'}">
	<Table ss:StyleID="ta1">
      }
      data.column_names.size.times {
        output << %{<Column ss:AutoFitWidth="1"/>}
      }
    end

    def build_table_header
      if options.show_table_headers
        table_row { build_cells(data.column_names, 'Heading') }
      end
    end

    def build_table_body
      data.each do |r|
          table_row { build_cells(r) }
      end
    end

    def build_row
      table_row{ build_cells(data.to_a) }
    end
    
    def table_row
      output << %{        <Row>\n}
      yield
      output << %{        </Row>\n}
    end

    def build_cells(values, style = '')
      values.each do |value|
        value = CGI.escapeHTML(value.to_s)
        if style.length > 0
          output << %{          <Cell>\n}
        else
          output << %{          <Cell ss:StyleID="#{style}">\n}
        end
        output << %{              <Data ss:Type="String">#{value}</Data>\n}
        output << %{            </Cell>\n}
      end
    end

    def finalize_table
      output << %{      </Table>
    </ss:Worksheet>
</Workbook>}

      @tempfile = Tempfile.new('output.xls')
      @tempfile.print(output)
      @tempfile.close();
      options.io =
        if options.tempfile
          @tempfile
        else
          File.read(@tempfile.path)
        end
    end

  end
end
