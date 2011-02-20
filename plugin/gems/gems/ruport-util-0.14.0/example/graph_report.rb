require "ruport"

require "ruport/util"

class GraphReport < Ruport::Report 
  
  renders_as_graph
  
  def renderable_data(format)
    graph = Graph(%w[a b c d e])
    graph.series [1,2,3,4,5], "foo" 
    graph.series [11,22,70,2,19], "bar"
    return graph
  end

end                                

Ruport::Formatter::Template.create(:graph) do |t|
  t.y_max = 100
end
    
# you need scruffy installed for svg, gruff for jpg / png
GraphReport.generate do |r| 
  r.save_as("foo.svg", :template => :graph)
end
