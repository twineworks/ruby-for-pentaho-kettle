# o_f_c.rb
# Generalized graphing support for Ruby Reports with Open Flash Chart
#
# This class used :
# - Open Flash Chart (http://teethgrinder.co.uk/open-flash-chart/) is Licensed by GPL (http://www.gnu.org/copyleft/gpl.html)
# - Open Flash Chart for Rails (http://pullmonkey.com/projects/open_flash_chart/) is licensed by MIT (http://www.opensource.org/licenses/mit-license.php)
# This is free software.  See LICENSE and COPYING for details.
#
class Ruport::Formatter
  module Graph
  # This class implements the open flash chart engine for Ruport.
  #
  # Depend of the OpenFlashChart plugin
  # == Options
  #
  # * title #=> Title of the Graph
  # * title_style  #=> Style of the title
  # * bg_color #=> '#DFFFDF'
  # * y_label_steps #=> 
  # * x_tick_size #=> 
  # * tool_tips #=> 
  # * x_axis_3d #=> 
  # * y_right_min #=> 
  # * y_right_max #=> 
  # * attach_to_y_right_axis #=> 
  # * x_labels #=> 
  # * y_max #=> 
  # * y_min #=> 
  # * x_axis_steps #=> 3
  # X Label Style :
  # * label_style #=> 
  # * x_label_color #=> ''
  # * x_orientation #=> 0
  # * x_label_step #=> -1
  # * x_label_grid_color #=> ''
  # X Legend Text :
  # * x_legend_text #=> 
  # * x_legend_size #=> -1
  # * x_legend_color #=> ''
  # X Legend Text :
  # * y_legend_text #=> 
  # * y_legend_size #=> -1
  # * y_legend_color #=> ''
  # Y Label Style :
  # * y_label_size #=> 
  # * y_label_color #=> ''
  # X Label Style :
  # * x_label_size #=> 
  # * x_label_color #=> ''
  # * x_label_orientation #=> 0
  # * x_label_step #=> -1
  # * x_label_grid_color #=> ''
  # Y Right Label Style :
  # * y_right_label_size #=> 
  # * y_right_label_color #=> ''
  # Y Right Legend Text :
  # * y_right_legend_text #=> 
  # * y_right_legend_size #=> -1
  # * y_right_legend_color #=> ''
  # Background Image :
  # * bg_image #=> 
  # * bg_image_x #=> 'center'
  # * bg_image_y #=> 'center'
  # Inner background Color
  # * inner_bg_color #=> 
  # * inner_bg_color2 #=> ''
  # * inner_bg_angle #=> -1
  #
  # Chart Type :
  # * chart_type : Table of table, with the first cell, the type of the chart, and the other cells parameters
  #  * line => (width, color='', text='', size=-1, circles=-1)
  #  * line_dot => (width, dot_size, color, text='', font_size=-1)
  #  * line hollow => (width, dot_size, color, text='', font_size=-1)
  #  * bar => (alpha, color, text='', font_size=-1)
  #  * bar_3d => (alpha, color, text='', font_size=-1)
  #  * bar_fade => (alpha, color, text='', font_size=-1)
  #  * bar_glass => (alpha, color, color_outline, text='', font_size=-1)
  #  * bar_filled => (alpha, color, color_outline, text='', font_size=-1)
  #  * area_hollow => (width, dot_size, color, alpha, text='', font_size=-1)
  #  * pie => (alpha, line_color, label_color)
  # ==  Plugin hooks called (in order)
  # 
  # * prepare_graph
  # * build_graph
  # * finalize_graph
    class OFC < Ruport::Formatter
        renders :ofc, :for => Ruport::Controller::Graph
        
      # Attribute of the OpenFlashChart object
        attr_reader :graph
          
        # Initialize the OpenFlashChart
        def initialize
          Ruport.quiet { require 'open_flash_chart' }
          @graph = OpenFlashChart.new
          @graph_pie = false
        end

        # Prepare the Graph
        def prepare_graph
          @graph.title(options.title || "#{options.style} Report",
                       options.title_style || 'font-weight:bold; font-size: 25px;')
          @graph.set_bg_color(options.bg_color || '#DFFFDF')
          @graph.set_y_label_steps(options.y_label_steps) if options.y_label_steps
          @graph.set_x_tick_size(options.x_tick_size) if options.x_tick_size
          @graph.set_tool_tips(options.tool_tips) if options.tool_tips
          @graph.set_x_axis_3d(options.x_axis_3d) if options.x_axis_3d
          @graph.set_y_right_min(options.y_right_min) if options.y_right_min
          @graph.set_y_right_max(options.y_right_max) if options.y_right_max
          @graph.attach_to_y_right_axis(options.attach_to_y_right_axis) if options.attach_to_y_right_axis
          if options.x_label_size
            @graph.set_x_label_style(options.x_label_size, 
                                     options.x_label_color || '',
                                     options.x_orientation || 0,
                                     options.x_label_step || -1,
                                     options.x_label_grid_color || '')
          end
          if options.x_legend_text
            @graph.set_x_legend(options.x_legend_text, 
                                     options.x_legend_size || -1,
                                     options.x_legend_color || '')
          end
          if options.y_legend_text
            @graph.set_y_legend(options.y_legend_text, 
                                     options.y_legend_size || -1,
                                     options.y_legend_color || '')
          end
          if options.y_label_size
            @graph.set_y_label_style( options.y_label_size,
                                     options.y_label_color || '')
          end
          if options.x_label_size
            @graph.set_x_label_style( options.x_label_size,
                                      options.x_label_color || '',
                                      options.x_label_orientation || 0,
                                      options.x_label_step || -1,
                                      options.x_label_grid_color || '')
          end
          if options.y_right_label_size
            @graph.set_y_right_label_style( options.y_right_label_size,
                                     options.y_right_label_color || '')
          end
          if options.y_right_legend_text
            @graph.set_y_right_legend(options.y_right_legend_text, 
                                     options.y_right_legend_size || -1,
                                     options.y_right_legend_color || '')
          end
          if options.bg_image
            @graph.set_bg_image(options.bg_image,
                                options.bg_image_x || 'center',
                                options.bg_image_y || 'center')
          end
          if options.inner_bg_color
            @graph.set_inner_background(options.inner_bg_color,
                                options.inner_bg_color2 || '',
                                options.inner_bg_angle || -1)
          end
          (options.chart_types || []).each{ |ct| 
               if [:line, :line_dot, :line_hollow, :bar, :bar_3d, :bar_fade, :bar_glass, :bar_filled, :area_hollow].include?(ct[0])
                  @graph.send(*ct)
               end
               if ct[0] == :pie
                 @graph_pie = true
                 @graph.pie_slice_colors(options.pie_slice_colors) if options.pie_slice_colors
                 @graph.pie(ct[1] || 50, ct[2] || '#DFFFDF', ct[3] || '#DFFFDF')
               end
            }

           @graph.set_x_labels((options.x_labels || data.column_names).map{|l| l.nil? ? '' : l})
           all_val = data.inject([]) {|ac, t|  ac += t.to_a }.flatten
           @graph.set_y_max(options.y_max || all_val.max)
           @graph.set_y_min(options.y_min || all_val.min)
           @graph.set_x_axis_steps(options.x_axis_step || 3)
         end

         # Build the Graph
         def build_graph
           if (@graph_pie)
             @graph.pie_values(data.data.first.to_a, (options.x_labels || data.column_names).map{|l| l.nil? ? '' : l})
           else
              data.each_with_index do |r,i|
                @graph.set_data(r.to_a)
           end 
         end
          
         output << @graph.render
        end

      end
    end
  end
