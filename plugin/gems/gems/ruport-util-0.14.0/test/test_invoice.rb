require 'test/helper'

describe 'Invoice' do
  before :all do
    @invoice = Ruport::Controller::Invoice
    @data = Table(%w[a b c]) << [1,2,3]
  end

  it 'should raise if the required options are not set' do
    lambda{ @invoice.render_pdf }.
      should raise_error(Ruport::Controller::RequiredOptionNotSet)
  end

  it "shouldn't raise if options are given correct in hash form" do
    lambda do
      @invoice.render_pdf :data => @data,
        :customer_info => 'blah', :company_info => 'also blah',
        :order_info => 'Incredibly Blah', :comments => 'Super Blah'
    end.should_not raise_error
  end

  it "shouldn't raise if options are given correct in block form" do
    lambda do
      @invoice.render_pdf do |r|
        r.customer_info = 'blah'
        r.company_info  = 'also blah'
        r.order_info    = 'Incredibly Blah'
        r.comments      = 'Super Blah'
        r.data          = @data
      end
    end.should_not raise_error
  end
end
