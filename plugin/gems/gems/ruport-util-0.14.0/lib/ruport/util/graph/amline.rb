class Ruport::Formatter  
  module Graph  
    class Amline < Ruport::Formatter
      renders :amline, :for => Ruport::Controller::Graph

      def initialize
        Ruport.quiet { require "hpricot" }
      end

      def build_graph
        generate_config_file
        data_out << "<chart>"
        data_out << "<xaxis>"
        data.x_labels.each_with_index do |e,i|
          data_out << %Q{<value xid="#{i}" show="true">#{e}</value>}
        end
        data_out << "</xaxis>"
        data_out << "<graphs>"
        data.each do |r|
          data_out << %Q{<graph gid="#{r.gid}">}
          r.each_with_index do |e,i|
            data_out << %Q{<value xid="#{i}">#{e}</value>}
          end
          data_out << "</graph>"
        end       
        data_out << "</graphs>"

        data_out << "</chart>"

        if options.data_file
          File.open(options.data_file,"w") { |f| f << data_out }
        end

        if options.settings_file
          File.open(options.settings_file,"w") { |f| f << settings_out }
        end

      end

      def apply_template       
        options.templated_settings_proc = lambda do |s|
          s.config do |c|
            c.values.y_left.max = template.y_max
            c.values.y_right.max = template.y_max 
            c.values.y_left.min = template.y_min
            c.values.y_right.min = template.y_min
            c.width = template.width
            c.height = template.height
            c.legend.enabled = template.show_legend
            if template.config
              template.config[c]
            end
          end      
          if template.graph_options
            default = template.graph_options[:_default] || lambda { }
            template.graph_options.each do |k,v|    
              next if k == :_default
              s.graph(k,&default)
              s.graph(k,&v)  
            end
          end
        end
      end


      def generate_config_file
        settings = ::Amline::Settings.new
        data.each do |r|
          settings.add_graph(r.gid)
          settings.graph(r.gid) { |g|
            g.title = r.gid
          }
        end

        options.templated_settings_proc[settings] if options.templated_settings_proc
        format_settings[settings]
        settings_out << settings.to_xml
      end

      def format_settings
        options.format_settings || lambda {}
      end

      def output
        { :data => data_out, :settings => settings_out }
      end

      def data_out
        @data_out ||= ""
      end

      def settings_out
        @settings_out ||= ""
      end

    end  
  end
end  

class Amline     
    
  class UnknownOption < StandardError; end

  class BlankSlate
    instance_methods.each { |m| undef_method m unless m =~ /^__/ }
  end

  class HpricotTraverser < BlankSlate


    def initialize(some_root)
      @root = some_root
    end

    attr :root

    def method_missing(id, *args, &block)
      if id.to_s =~ /^(\w+)=/
        @root.at($1).innerHTML =  args[0].to_s
      elsif id.to_s =~ /^(\w+)!/
        @root.at($1)
      else
        new_root = @root.at(id) or raise
        HpricotTraverser.new(new_root)
      end
    rescue
      raise UnknownOption
    end

    def ==(other)
      @root == other
    end

    def inspect
      @root
    end

    def to_s
      @root.to_s
    end

    alias_method :to_xml, :to_s

  end

  class Settings      
    
    SETTINGS_FILE = File.join(Ruport::Util::BASEDIR, 'example', 'data', 
                                                     'amline_settings.xml') 
    GRAPH_FILE = File.join(Ruport::Util::BASEDIR, 'example','data',
                                                     'amline_graph.xml')

    def initialize(settings_file=SETTINGS_FILE,graph_file=GRAPH_FILE)
      @config = HpricotTraverser.new(Hpricot(File.read(settings_file))) 
      @graph_file = graph_file
    end

    def config
      yield @config.settings if block_given?
      @config.settings
    end  
    
    def add_label(label,options)
     xml = %Q{
      <label>
        <x>#{options[:x]}</x>                                                 
        <y>#{options[:y]}20</y>                                   
        <rotate>#{options[:rotate]}</rotate>                     
        <width>#{options[:width]}</width>                
        <align>#{options[:align]}</align>                       
        <text_color>#{options[:text_color]}</text_color>             
        <text_size>#{options[:text_size]}</text_size>               
        <text>  
          <![CDATA[#{label}]]>
        </text>        
      </label>
    } 
    @config.root.search("labels").append(xml)
    end

    def add_graph(gid)    
      new_graph = Hpricot(File.read(@graph_file))
      new_graph.at("graph")["gid"] = gid
      @config.root.search("graphs").append new_graph.to_s
    end

    def graph(gid)
      yield HpricotTraverser.new(@config.root.search("graph[@gid=#{gid}]"))
    end

    def to_xml
      @config.to_xml
    end

    def save(file)
      File.open(file,"w") { |f| f << @config.to_s }
    end

  end

end
