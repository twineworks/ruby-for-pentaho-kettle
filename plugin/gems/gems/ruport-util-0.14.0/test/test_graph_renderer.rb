require 'test/helper'

Ruport.quiet { testcase_requires 'scruffy' }

class MockGraphPlugin < Ruport::Formatter
  renders :mock, :for => Ruport::Controller::Graph
  def prepare_graph 
    output << "prepare"
  end
  def build_graph
    output << "build"
  end
  def finalize_graph
    output << "finalize"
  end
end

describe 'Graph Renderer' do
  before :all do
    @graph = Ruport::Controller::Graph
    @data = Graph(%w[a b c],[[1,2,3],[4,5,6]])
  end

  it 'should render' do
    out = @graph.render_mock 
    out.should == 'preparebuildfinalize'
  end

  it 'should render SVG' do
    @graph.render_svg{|r| r.data = @data}.should_not be_nil
  end


end
