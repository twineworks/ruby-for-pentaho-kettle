require "ruport"
require "ruport/util"
require "fileutils"
class MyReport < Ruport::Report

  renders_as_table
  
  def prepare
    add_mailer :default,
      :host => "mail.adelphia.net",
      :address => "gregory.t.brown@gmail.com"
   end

  def generate
    Table(%w[a b c]) { |t| t << [1,2,3] << [4,5,6] }
  end

end

MyReport.generate do |report|
 report.save_as "foo.pdf"
 report.send_to("gregory.t.brown@gmail.com") do |mail|
   mail.subject = "Sample report"
   mail.attach "foo.pdf"
   mail.text = <<-EOS
     this is a sample of sending an emailed report from within Ruport.
   EOS
 end
end
