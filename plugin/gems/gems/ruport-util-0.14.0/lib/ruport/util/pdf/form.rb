module Ruport
  module Util
    module FormHelpers

      def form_field(label,text,opts={})
        padding = Array(opts.delete(:padding) || 0)
        padding_top     = padding[0]
        padding_right   = padding[1] || padding_top
        padding_bottom  = padding[2] || padding_top
        padding_left    = padding[3] || padding_right
        
        x = opts.delete(:x) || left_boundary
        y = opts.delete(:y) || cursor
        width = opts.delete(:width) || full_width(x)
        height = opts.delete(:height)
      
        old_cursor = cursor
        old_font = pdf_writer.font_size
      
        move_cursor_to(y - 2 - padding_top)
        if opts[:style] == "vertical"
          draw_text "<b>#{label}</b>\n#{text}", :left => x + 10 + padding_left
          height ||= pdf_writer.font_height * (text.count("\n") + 2) + 10 +
            padding_top + padding_bottom
        else
          draw_text "<b>#{label}</b> #{text}", :left => x + 10 + padding_left
          height ||= pdf_writer.font_height + 10 + padding_top + padding_bottom
        end
        draw_border(x,y,width,height,opts)
      
        if opts[:maintain_cursor]
          move_cursor_to(old_cursor)
        else
          move_cursor(-8 - padding_bottom)
        end
        pdf_writer.font_size = old_font
      end
    
      def option_box(opt,choice,opts={})
        x = opts[:x] || left_boundary
        y = opts[:y] || cursor
        width = opts[:width] || 12
        height = opts[:height] || 12
      
        old_cursor = cursor
        old_font = pdf_writer.font_size
      
        if opt.eql?(choice)
          checked_box(x + 10, y - 3, width, height)
        else
          unchecked_box(x + 10, y - 3, width, height)
        end
        draw_text opts[:label], :left => x + 10 + width + 8,
          :y => y if opts[:label]
      
        opts[:maintain_cursor] ? move_cursor_to(old_cursor) : move_cursor(-8)
        pdf_writer.font_size = old_font
      end
    
      def checked_box(x,y,width=12,height=12)
        draw_border(x,y,width,height)
        pdf_writer.line(x, y, x + width, y - height)
        pdf_writer.line(x + width, y, x, y - height)
        pdf_writer.stroke
      end

      def unchecked_box(x,y,width=12,height=12)
        draw_border(x,y,width,height)
      end

      # Draws a box at the specified x and y coordinates (top left corner),
      # with the specified width and height
      def draw_border(x,y,width,height,opts={})
        border = Array(opts[:border] || 1)
        border_top    = border[0]
        border_right  = border[1] || border_top
        border_bottom = border[2] || border_top
        border_left   = border[3] || border_right
        
        draw_line(x, y, x + width, y,
          border_top) if border_top > 0
        draw_line(x + width, y, x + width, y - height,
          border_right) if border_right > 0
        draw_line(x, y - height, x + width, y - height,
          border_bottom) if border_bottom > 0
        draw_line(x, y, x, y - height,
          border_left) if border_left > 0
      end
      
      def draw_line(x1,y1,x2,y2,thickness)
        ss = PDF::Writer::StrokeStyle.new(thickness)
        pdf_writer.stroke_style(ss)
        pdf_writer.line(x1,y1,x2,y2)
        pdf_writer.stroke
      end
    
      # draws a horizontal line at y from x1 to x2
      def horizontal_line_at(y,x1,x2)
        pdf_writer.line(x1,y,x2,y)
        pdf_writer.stroke
      end
    
      def full_width(x=nil)
        x ? right_boundary - x : right_boundary - left_boundary
      end
      
    end
  end
end

module Ruport
  class Formatter::PDF
    alias_method :original_draw_text, :draw_text
    
    def draw_text(text,text_opts)
      y_pos = cursor
      original_draw_text(text,text_opts)
      move_cursor_to(y_pos) if text_opts[:maintain_cursor]
    end
  end
end
