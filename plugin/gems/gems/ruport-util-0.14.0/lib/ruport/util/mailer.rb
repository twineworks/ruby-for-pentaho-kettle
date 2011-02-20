# mailer.rb
#  Created by Gregory Brown on 2005-08-16
#  Copyright 2005 (Gregory Brown) All Rights Reserved.
#  This product is free software, you may distribute it as such
#  under your choice of the Ruby license or the GNU GPL
#  See LICENSE for details
require "net/smtp"
require "forwardable"
require "ruport/util/report"

module Ruport
  
  # === Overview
  #
  # This class uses SMTP to provide a simple mail sending mechanism.
  # It also uses MailFactory to provide attachment and HTML email support. 
  #
  class Mailer

    class InvalidMailerConfigurationError < RuntimeError #:nodoc:   
    end
    
    extend Forwardable
   
    # Creates a new Mailer object. Optionally, you can select a mailer by label
    #
    # Example:
    #
    #   a = Mailer.new        # uses the :default mailer
    #   a = Mailer.new :foo   # uses :foo mail config from Ruport::Config
    #
    def initialize( mailer_label=:default )
      select_mailer(mailer_label)
      mail_object.from = @mailer.address if mail_object.from.to_s.empty?
      rescue
        raise "you need to specify a mailer to use"
    end
   
    def_delegators( :@mail, :to, :to=, :from, :from=, 
                           :subject, :subject=, :attach, 
                           :text, :text=, :html, :html= )

    class << self
      # :call-seq:
      #  add_mailer(mailer_name, options)
      #
      # Creates or retrieves a mailer configuration. Available options:
      # <b><tt>:host</tt></b>::         The SMTP host to use.
      # <b><tt>:address</tt></b>::      Address the email is being sent from.
      # <b><tt>:user</tt></b>::         The username to use on the SMTP server
      # <b><tt>:password</tt></b>::     The password to use on the SMTP server. 
      #                                 Optional.
      # <b><tt>:port</tt></b>::         The SMTP port to use. Optional, defaults
      #                                 to 25.
      # <b><tt>:auth_type</tt></b>::    SMTP authorization method. Optional, 
      #                                 defaults to <tt>:plain</tt>.
      # <b><tt>:mail_class</tt></b>::   If you don't want to use the default 
      #                                 <tt>MailFactory</tt> object, you can 
      #                                 pass another mailer to use here.
      #                               
      # Example (creating a mailer config):
      #   add_mailer :alternate, :host => "mail.test.com", 
      #                          :address => "test@test.com",
      #                          :user => "test", 
      #                          :password => "blinky"
      #                          :auth_type => :cram_md5
      #
      # Example (retreiving a mailer config):
      #   mail_conf = mailers[:alternate]  #=> <OpenStruct ..>
      #   mail_conf.address                #=> test@test.com
      #
      def add_mailer(name,options)
        mailers[name] = OpenStruct.new(options)
        check_mailer(mailers[name],name)
      end

      # Alias for <tt>mailers[:default]</tt>.
      def default_mailer
        mailers[:default]
      end

      # Returns all the <tt>mailer</tt>s defined
      def mailers; @mailers ||= {}; end

      private

      # Verifies that you have provided a host for your mailer.
      def check_mailer(settings, label) # :nodoc:
        raise InvalidMailerConfigurationError unless settings.host
      end

    end

    # Sends the message.
    #
    # Example:
    #
    #   mailer.deliver :from => "gregory.t.brown@gmail.com",
    #                  :to   => "greg7224@gmail.com"
    #
    def deliver(options={})           
      to = options.delete(:to)
      mail_object.to = Array(to).join(",")
      options.each { |k,v| send("#{k}=",v) if respond_to? "#{k}=" }
      
      Net::SMTP.start(@host,@port,@host,@user,@password,@auth) do |smtp|
        smtp.send_message((options[:mail_object] || mail_object).to_s, options[:from], to)
      end
    end

    private

    def select_mailer(label)
      @mailer      = self.class.mailers[label]
      @host       = @mailer.host
      @user       = @mailer.user
      @password   = @mailer.password
      @address    = @mailer.address
      @port       = @mailer.port       || 25
      @auth       = @mailer.auth_type  || :plain
      @mail_class = @mailer.mail_class
    end

    def mail_object
      return @mail if @mail
      return @mail ||= @mail_class.new if @mail_class
      require "mailfactory"
      @mail ||= MailFactory.new
    end
    
  end

  class Report
    # Creates a new Mailer and sets the <tt>to</tt> attribute to the addresses
    # specified. Yields a Mailer object, which can be modified before delivery.
    #
    def send_to(adds)
      use_mailer(:default) unless @mailer
      m = Mailer.new
      yield(m)
      m.send(:select_mailer,@mailer)
      m.deliver :from => m.from, :to => adds
    end

    # Sets the active mailer to the Ruport::Mailer source requested by <tt>label</tt>.
    def use_mailer(label)
      @mailer = label
    end

    def_delegator Ruport::Mailer, :add_mailer
 
  end




end
