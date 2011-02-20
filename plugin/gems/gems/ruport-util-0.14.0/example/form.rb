require "ruport"
require "ruport/util"

class SimpleForm < Ruport::Renderer

  stage :form_header, :form_body
  finalize :form

  required_option :name, :userid, :fruit, :email

  class PDF < Ruport::Formatter::PDF
    renders :pdf, :for => SimpleForm
    include Ruport::Util::FormHelpers
    
    def build_form_header
      add_text "Sample Form", :font_size => 16, :justification => :center
    end

    def build_form_body
      options.text_format = { :font_size => 10 }
      form_field "Name:", options.name, :y => 650, :x => 125, :width => 200, 
                                        :border => [1,0,1,1]
      form_field "User ID:", options.userid, :y => 650, :width => 150, :x => 325
      form_field "Email:", options.email, :y => 630, :width => 350, :x => 125,
                                                     :border => [0,1,1]

      draw_text "Favorite Fruit: ", :left => 280, :y => 600
      option_box :apple, options.fruit, :label => "Apple", :y => 600, :x => 340
      option_box :banana, options.fruit, :label => "Banana", :y => 600, :x => 400
      
    end

    def finalize_form
      render_pdf
    end

  end

end

puts SimpleForm.render_pdf { |r|
  r.name = "Gregory Brown"
  r.userid = "sandal"
  r.fruit = :banana
  r.email = "gregory.t.brown@gmail.com"
}
