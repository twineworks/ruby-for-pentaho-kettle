require 'ruport'
require 'ruport/util'

class Example < Ruport::Renderer
  stage :report
  
  def setup
    graph = Graph(%w[a b c d e])
    graph.series [1,2,3,4,5], "foo" 
    graph.series [11,22,70,2,19], "bar"
    self.data = graph
  end
  
  class PDF < Ruport::Formatter::PDF
    renders :pdf, :for => Example
    
    def build_report
      add_text "Report With Graph", :font_size => 20
      draw_graph(data, :x => 40, :y => 400, :width => 300, :height => 300)
      
      render_pdf
    end
  end
  
end

Example.render_pdf(:file => "example.pdf")