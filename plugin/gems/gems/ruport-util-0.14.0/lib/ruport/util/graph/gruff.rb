class Ruport::Formatter  
  module Graph    
    
    class Gruff < Ruport::Formatter

      renders [:png,:jpg], :for => Ruport::Controller::Graph

      def initialize
        Ruport.quiet { require 'gruff' }
      end  
      
      def apply_template
         options.min    = template.y_min
         options.max    = template.y_max
         options.width  = template.width
         options.height = template.height
      end

      def build_graph   
        graph = ::Gruff::Line.new("#{options.width || 800}x#{options.height || 600}")
        graph.title = options.title if options.title
        graph.labels = options.labels if options.labels
        data.each do |r|
          graph.data(r.gid,r.to_a)
        end   
        
        graph.maximum_value = options.max if options.max
        graph.minimum_value = options.min if options.min

        output << graph.to_blob(format.to_s)
      end

      # Save the output to a file.
      def save_output(filename)
        File.open(filename,"wb") {|f| f << output }
      end
    end         
  end
  
  class PDF
    def draw_graph(graph, opts={})
      x = opts.delete(:x)
      y = opts.delete(:y)
      width = opts.delete(:width)
      height = opts.delete(:height)
      g = graph.as(:jpg, opts)
      info = ::PDF::Writer::Graphics::ImageInfo.new(g)
      
      # reduce the size of the image until it fits into the requested box
      img_width, img_height =
        fit_image_in_box(info.width,width,info.height,height)
      
      # if the image is smaller than the box, calculate the white space buffer
      x, y = add_white_space(x,y,img_width,width,img_height,height)
      
      pdf_writer.add_image(g, x, y, img_width, img_height) 
    end
  end
end