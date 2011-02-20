module Ruport     
  class Controller #:nodoc:
    class Invoice < Ruport::Controller 
               
      required_option :customer_info,:company_info,:order_info,:comments

      stage :invoice_headers,:invoice_body,:invoice_footer
      
      finalize :invoice

      module InvoiceHelpers
        def build_company_header
          @tod = pdf_writer.y
          rounded_text_box(options.company_info) { |o| 
              o.radius    = 3  
              o.width     = options.header_width || 200
              o.height    = options.header_height || 50
              o.font_size = options.header_font_size || 10
              o.y         = pdf_writer.y
              o.x         = pdf_writer.absolute_left_margin + 10
          }
        end

        def build_customer_header
          move_cursor(-5)
          rounded_text_box(options.customer_info) { |o| 
              o.radius    = 3
              o.width     = options.header_width || 200
              o.height    = options.header_height || 50
              o.font_size = options.header_font_size || 10
              o.y         = pdf_writer.y
              o.x         = pdf_writer.absolute_left_margin + 10
          }
        end

        def build_title
          add_title(options.title) if options.title
        end
        
        def add_title( title )  
          rounded_text_box("<b>#{title}</b>") do |o|
            o.fill_color = Color::RGB::Gray80
            o.radius    = 5  
            o.width     = options.header_width || 200
            o.height    = options.header_height || 20
            o.font_size = options.header_font_size || 11
            o.x         = pdf_writer.absolute_right_margin - o.width 
            o.y         = pdf_writer.absolute_top_margin
          end
        end      

        def build_order_header
          if options.order_info
            rounded_text_box("<b>#{options.order_info}</b>") do |o|
              o.radius    = 5  
              o.heading   = "Billing Information"
              o.width     = options.header_width || 200
              o.height    = options.header_height || 80
              o.font_size = options.header_font_size || 10
              o.x         = pdf_writer.absolute_right_margin - o.width 
              o.y         = pdf_writer.absolute_top_margin - 25
            end 
          end
        end

        def horizontal_line(x1,x2)
          pdf_writer.line(x1,pdf_writer.y,x2,pdf_writer.y)
          pdf_writer.stroke
        end
                                  
      end
       
      class PDF < Ruport::Formatter::PDF
        
        include InvoiceHelpers
        renders :pdf, :for => Controller::Invoice

        def build_invoice_headers
          build_company_header
          build_customer_header
          build_title
          build_order_header
        end
        
        def build_invoice_body
          move_cursor_to 600
          draw_table data, :width => options.body_width || 450
        end

        def build_invoice_footer
         # footer
          pdf_writer.open_object do |footer|
            pdf_writer.save_state
            pdf_writer.stroke_color! Color::RGB::Black
            pdf_writer.stroke_style! ::PDF::Writer::StrokeStyle::DEFAULT
            if options.comments  
              
              move_cursor_to 60

              horizontal_line left_boundary + 20, right_boundary - 25

              move_cursor(-10)

              add_text(options.comments,:justification => :center, 
                                        :left => 0, :right => 0 )
                                        
            end

            pdf_writer.restore_state
            pdf_writer.close_object
            pdf_writer.add_object(footer, :all_pages)
          end
        end

        def finalize_invoice
          output << pdf_writer.render
        end

      end 
    end 
  end  
end

module Ruport::Report::Invoice
  def render_invoice(*args,&block)
    Ruport::Renderer::Invoice.render_pdf(*args,&block)
  end
end

