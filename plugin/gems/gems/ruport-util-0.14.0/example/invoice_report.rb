require "rubygems"
require "ruport"
require "ruport/util"

class SampleReport < Ruport::Report
  include Invoice

  def renderable_data(format)
    render_invoice do |i|
      i.data = Table("Item Number","Description","Price") { |t|
        t << %w[101 Erlang\ The\ Movie $1000.00]
        t << %w[102 Erlang\ The\ Book $45.95] 
      }
      i.company_info  = "Stone Code Productions\n43 Neagle Street"
      i.customer_info = "Gregory Brown\n200 Foo Ave.\n"
      i.comments      = "Hello Mike!  Hello Joe!"
      i.order_info    = "Some info\nabout your order"
      i.title         = "Invoice for 12.15.2006 - 12.31.2006"
      i.options do |o|
        o.body_width = 480
        o.comments_font_size = 12
        o.title_font_size = 10  
      end
    end
  end
end

a = SampleReport.new
a.save_as("invoice.pdf")
