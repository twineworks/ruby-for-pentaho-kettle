require 'test/helper'
require 'net/smtp'

describe 'Mailer' do
  before :all do
    @default_opts = {
      :user     => "inky",
      :host     => "mail.example.com", 
      :address  => "sue@example.com", 
      :password => "chunky"
    }

    @other_opts = {
      :user     => "blinky",
      :host     => "moremail.example.com",
      :address  => "clyde@example.com",
      :password => "bacon"
    }

    @mailer = Ruport::Mailer

    @mailer.add_mailer :default, @default_opts
    @mailer.add_mailer :other, @other_opts

    @default_mailer = @mailer.new :default
    @other_mailer = @mailer.new :other
  end

  before :each do
    @mail_fields = { :to      => 'clyde@example.com',
                     :from    => 'sue@example.com',
                     :subject => 'Hello',
                     :text    => 'This is a test' }
  end

  def values_for(mailer, *keys)
    keys = keys.flatten.map{|k| k.to_s}
    keys.map{|k| mailer.instance_variable_get("@#{k}") }
  end

  def check(mailer, expected)
    keys = [:host, :address, :user, :password]
    values = values_for(mailer, *keys)
    values.should == expected.values_at(*keys)
  end

  def mock_report_mailer
    @smtp = mock('SMTP')
    @smtp.stub!(:send_message).and_return('250 ok')
    Net::SMTP.should_receive(:start).and_return("250 ok")
  end

  def mock_mailer(count, mailer = @default_mailer)
    values = values_for(mailer, :host, :port, :host, :user, :password, :auth)

    @smtp = mock('SMTP')

    Net::SMTP.should_receive(:start).
      with(*values).
      exactly(count).times.
      and_yield(@smtp)
    @smtp.should_receive(:send_message).
      with( an_instance_of(String), 
            an_instance_of(String), 
            an_instance_of(String) ).any_number_of_times.
      and_return('250 ok')
    @smtp.should_receive(:send_message).
      with(an_instance_of(String),an_instance_of(String), nil).any_number_of_times.
      and_raise(Net::SMTPSyntaxError)
  end

  it 'should have default mailer' do
    @mailer.mailers[:default].should == @mailer.default_mailer
  end

  it 'should have equal options inside and outside of default mailer' do
    check @default_mailer, @default_opts
  end

  it 'should have equal options inside and outside of other mailer' do
    check @other_mailer, @other_opts
  end

  it 'should raise if no default mailer is set' do
    default = @mailer.mailers.delete :default

    lambda{ @mailer.new }.
      should raise_error(RuntimeError, 'you need to specify a mailer to use')

    @mailer.mailers[:default] = default
  end

  it 'should raise if configuration is invalid' do
    lambda{ @mailer.add_mailer :bar, :user => :foo, :address => 'foo@bar.com' }.
      should raise_error(Ruport::Mailer::InvalidMailerConfigurationError)

    lambda{ @mailer.add_mailer :bar, :host => 'localhost' }.
      should_not raise_error
  end

  it 'should send emails with default' do
    mock_mailer 1
    @default_mailer.deliver(@mail_fields).should == '250 ok'
  end

  it 'should send emails with other default' do
    mock_mailer 1, @other_mailer
    @other_mailer.deliver(@mail_fields).should == '250 ok'
  end

  it 'should send mail without to field' do
    mock_mailer 1
    hash = @mail_fields.dup
    hash.delete :to
    lambda{ @default_mailer.deliver(hash) }.
      should raise_error(Net::SMTPSyntaxError)
  end

  it 'should send mail with HTML' do
    mock_mailer 1
    hash = @mail_fields.dup
    hash.delete(:text)
    hash[:html] = '<p>This is a test.</p>'
    @default_mailer.deliver(hash).should == '250 ok'
  end

  it 'should send mail with attachment' do
    mock_mailer 1
    @default_mailer.attach 'test/samples/data.csv'
    @default_mailer.deliver(@mail_fields).should == '250 ok'
  end

  it 'should have information about the mail' do
    dfm = @default_mailer

    { :to      => ['foo@bar.com'],
      :from    => ['foo@bar.com'],
      :subject => ['RuportDay!']
    }.each do |meth, value|
      assign_meth = "#{meth}="

      dfm.instance_eval{ @mail.send(assign_meth, value) }
      dfm.send(meth).should == value

      dfm.send(assign_meth, value)
      dfm.send(meth).should == value
    end
  end

  it 'should be able to select a mailer' do
    mailer = @mailer.new(:default)
    check mailer, @default_opts
    mailer.send(:select_mailer, :other)
    check mailer, @other_opts
  end

  it 'should send reports with default mailer' do
    report = Ruport::Report.new
    mock_report_mailer
    report.send_to(@other_opts[:address]){|mail|
      mail.subject = 'Test Report'
      mail.text = 'Test'
    }.should == '250 ok'
  end
end
