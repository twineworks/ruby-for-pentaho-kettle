require 'test/helper'

Ruport.quiet { testcase_requires 'open_flash_chart' }

require "ruport/util/graph/o_f_c"

describe 'Graph OpenFlashCharts' do
  before :all do
     @controller = Ruport::Controller::Graph
  end

  it 'Render Bar chart' do
    @controller = Ruport::Controller::Graph
    @controller.should_not be_nil
    @table = Table(%w(name))
    @table << [[3,2,6,7,1,3,2,7,9,1,15,14]]
    @table.should_not be_nil
    @report = @controller.render(:ofc, :data => @table, 
		:chart_types => [[:bar_glass, 50, '#9933CC', '#8010A0', 'PAGE VIEWS', 10]],
		:x_labels => %w(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec),
                :title => 'Page views By Visitor',
                :title_style => "{font-size: 25px; font-color:#736AFF",
                :bg_color => '#FFFFFF',
                :x_label_size => 10,
                :x_label_color => 'Ox9933CC',
                :x_label_orientation => 0,
                :x_max => 12,
                :y_max => 15,
                :y_label_steps => 3,
                :y_label_size => 12,
                :y_label_color => '#000000',
                :y_legend_text => 'Open flash Chart for Ruport',
                :y_legend_size => 12,
                :y_legend_color => '#736AFF'
              )
     @report.should == "&title=Page views By Visitor,{font-size: 25px; font-color:#736AFF& \n&x_axis_steps=3,& \n&bg_colour=#FFFFFF& \n&x_label_style=10,Ox9933CC,0&y_legend=Open flash Chart for Ruport,12,#736AFF&y_ticks=5,10,3& \n&bar_glass=50,#9933CC,#8010A0,PAGE VIEWS,10& \n&values=3,2,6,7,1,3,2,7,9,1,15,14& \n&x_labels=Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec& \n&y_min=1& \n&y_max=15& \n"
  end
  
  it 'Render Multiline' do
    @controller = Ruport::Controller::Graph
    @table = Table(%w(name))
    @table << [(1..12).to_a.map{|x| x * 1.3}]
    @table << [(1..12).to_a.map{|x| Math.sin(x) + 3}]
    @table << [(1..6).to_a + (1..6).to_a.reverse]
    @table << [(1..2).to_a + (1..2).to_a.reverse + (3..8).to_a]
    @controller.should_not be_nil
    @table.should_not be_nil
    @report = @controller.render(:ofc, :data => @table, 
		:chart_types => [[:line, 2, '#9933CC', 'Page Views', 12],
				[:area_hollow, 2, 3, 25, '#CC3399', 'Visitors', 12],
                                [:line_dot, 3,5,'0xCC3399', 'Downloads', 12],
                                [:line_hollow, 2,4,'0x80a033', 'Bounces', 12]],
		:x_labels => %w(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec),
                :title => 'Pages Views',
                :title_style => "{font-size: 25px;}",
                :bg_color => '#FFFFFF',
                :x_label_size => 12,
                :x_label_color => 'Ox000000',
                :x_label_orientation => 0,
                :x_label_step => 2,
                :y_max => 12,
                :y_label_steps => 12,
                :y_label_size => 12,
                :y_label_color => '#000000',
                :y_legend_text => 'Open flash Chart for Ruport',
                :y_legend_size => 12,
                :y_legend_color => '#736AFF'
              )
     @report.should == "&title=Pages Views,{font-size: 25px;}& \n&x_axis_steps=3,& \n&bg_colour=#FFFFFF& \n&x_label_style=12,Ox000000,0,2&y_legend=Open flash Chart for Ruport,12,#736AFF&y_ticks=5,10,12& \n&line=2,#9933CC,Page Views,12& \n&area_hollow_2=2,3,25,#CC3399,Visitors,12& \n&line_dot_3=3,0xCC3399,Downloads,12,5& \n&line_hollow_4=2,0x80a033,Bounces,12,4& \n&values=1.3,2.6,3.9,5.2,6.5,7.8,9.1,10.4,11.7,13.0,14.3,15.6& \n&values_2=3.8414709848079,3.90929742682568,3.14112000805987,2.24319750469207,2.04107572533686,2.72058450180107,3.65698659871879,3.98935824662338,3.41211848524176,2.45597888911063,2.0000097934493,2.46342708199957& \n&values_3=1,2,3,4,5,6,6,5,4,3,2,1& \n&values_4=1,2,2,1,3,4,5,6,7,8& \n&x_labels=Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec& \n&y_min=1& \n&y_max=12& \n"
  end
  
  it 'Render Pie' do
    @controller = Ruport::Controller::Graph
    @table = Table(%w(name))
    @table << [(1..12).to_a.map{|x| x * 1.3}]
    @controller.should_not be_nil
    @table.should_not be_nil
    @report = @controller.render(:ofc, :data => @table, 
		:chart_types => [[:pie, 80, '#9933CC', '#8010A0']],
		:x_labels => %w(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec),
                :pie_slice_colors => ['#d01f3c','#356aa0','#C79810'])
    @report.should == "&title= Report,font-weight:bold; font-size: 25px;& \n&x_axis_steps=3,& \n&bg_colour=#DFFFDF& \n&y_ticks=5,10,5& \n&line=3,#87421F& \n&x_labels=Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec& \n&y_min=1.3& \n&y_max=15.6& \n&pie=80,#9933CC,#8010A0& \n&values=1.3,2.6,3.9,5.2,6.5,7.8,9.1,10.4,11.7,13.0,14.3,15.6& \n&pie_labels=Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec& \n&colours=#d01f3c,#356aa0,#C79810& \n"
  end
end
