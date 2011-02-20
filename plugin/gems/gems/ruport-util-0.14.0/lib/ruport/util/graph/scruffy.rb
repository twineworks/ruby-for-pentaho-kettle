class Ruport::Formatter
  module Graph
    class Scruffy < Ruport::Formatter

      renders :svg, :for => Ruport::Controller::Graph

      # a hash of Scruffy themes.
      #
      # You can use these by setting options.theme like this:
      #
      #   Graph.render_svg(:theme => :mephisto)
      #  
      # Available themes: ( :mephisto, :keynote, :ruby_blog )
      #
      def themes
        { :mephisto => Scruffy::Themes::Mephisto.new,
          :keynote  => Scruffy::Themes::Keynote.new,
          :ruby_blog => Scruffy::Themes::RubyBlog.new }
      end

      # generates a scruffy graph object
      def initialize
        Ruport.quiet { require 'scruffy' }   
        @graph = ::Scruffy::Graph.new
      end

      # the Scruffy::Graph object
      attr_reader :graph

      # sets the graph title, theme, and column_names
      #
      # column_names are defined by the Data::Table,
      # theme may be specified by options.theme (see SVG#themes)
      # title may be specified by options.title 
      #
      def prepare_graph 
        @graph.title ||= options.title
        @graph.theme = themes[options.theme] if options.theme
        @graph.point_markers ||= data.x_labels
      end

      # Generates an SVG using Scruffy.
      def build_graph    

        data.each_with_index do |r,i|
          add_line(r.to_a,r.gid||"series #{i+1}")
        end

        output << @graph.render( 
          :size => [options.width||500, options.height||300],
          :min_value => options.min, :max_value => options.max
        )
      end          
      
      def apply_template
         options.min    = template.y_min
         options.max    = template.y_max
         options.width  = template.width
         options.height = template.height
      end
  
      # Uses Scruffy::Graph#add to add a new line to the graph.
      #
      # Line style is determined by options.style
      #
      def add_line(row,label)
        @graph.add( options.style || :line, label, row )
      end  
    end
  end
end