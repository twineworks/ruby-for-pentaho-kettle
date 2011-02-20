require 'cgi'
require 'tmpdir'

require 'zip/zip'

module Ruport

  class Formatter::ODS < Formatter
    BLANK_ODS = File.join(Ruport::Util::BASEDIR, 'example', 'data', 'blank.ods')

    renders :ods, :for => [ Controller::Row, Controller::Table]

    def prepare_table
      output << %{<?xml version="1.0" encoding="UTF-8"?>
<office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0" xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0" xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0" xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0" xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" xmlns:math="http://www.w3.org/1998/Math/MathML" xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0" xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0" xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer" xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" office:version="1.0">
  <office:scripts/>
  <office:font-face-decls/>
  <office:automatic-styles/>
  <office:body>
    <office:spreadsheet>
      <table:table table:name="Ruport" table:print="false">
        <office:forms form:automatic-focus="false" form:apply-design-mode="false"/>
}
    end

    def build_table_header
      if @options.show_table_headers
        table_row{ build_cells(data.column_names) }
      end
    end

    def build_table_body
       data.each { |r| build_row(r) }
    end

    def build_row(row=data)
      table_row{ build_cells(row.to_a) }
    end

    def table_row
      output << %{        <table:table-row>\n}
      yield
      output << %{        </table:table-row>\n}
    end

    def build_cells(values)
      values.each do |value|
        value = CGI.escapeHTML(value.to_s)
        output << %{          <table:table-cell>\n}
        output << %{            <text:p>#{value}</text:p>\n}
        output << %{          </table:table-cell>\n}
      end
    end

    def finalize_table
      output << %{      </table:table>
    </office:spreadsheet>
  </office:body>
</office:document-content>}

      @tempfile = Tempfile.new('output.ods')

      File.open(BLANK_ODS){|bo| @tempfile.print(bo.read(1024)) until bo.eof? }
      @tempfile.close

      zip = Zip::ZipFile.open(@tempfile.path)
      zip.get_output_stream('content.xml') do |cxml|
        cxml.write(output)
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
end
