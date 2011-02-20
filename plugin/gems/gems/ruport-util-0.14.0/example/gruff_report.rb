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

GraphReport.generate do |r| 
  r.as(:png, :title => "My Graph",
    :labels => {0 => '2003', 2 => '2004', 4 => '2005'},
    :file => "my_graph.png"
  ) 
end
