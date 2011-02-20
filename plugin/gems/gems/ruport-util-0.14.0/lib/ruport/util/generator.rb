module Ruport
  class Generator #:nodoc:
  extend FileUtils

  module Helpers  #:nodoc:
    def format_class_name(string)
      string.downcase.split("_").map { |s| s.capitalize }.join
    end

    def check_for_files
      if File.exist? "lib/reports/#{ARGV[1]}.rb"
        raise "Report #{ARGV[1]} exists!"
      end

      if File.exist? "lib/controllers/#{ARGV[1]}.rb"
        raise "Controller #{ARGV[1]} exists!"
      end
    end
  end

  begin
    require "rubygems"
  rescue LoadError
    nil
  end
  require "ruport"
  require "ruport/util"

  def self.build(proj)
    @project = proj
    build_directory_structure
    build_init
    build_config
    build_utils         
    build_rakefile 
    build_readme
     
    puts "\nSuccessfully generated project: #{proj}"
  end 
  
  def self.write_file(path,options={})
    options = {:io => STDOUT}.merge(options)
    m = "#{project}/#{path}"
    options[:io].puts "  #{m}"
    if options[:file]
       options[:file] << yield   
    else             
      File.open(m,"w") { |f| f << yield }
    end
  end                                
  
  def self.build_init
    write_file("lib/init.rb") { INIT }
  end

  # Generates a trivial rakefile for use with Ruport.
  def self.build_rakefile
    write_file("Rakefile") { RAKEFILE }
  end  
  
  def self.build_readme
     write_file("README") { README }
  end

  # Generates the build.rb and sql_exec.rb utilities
  def self.build_utils           
    m = "#{project}/util/build"   
    puts "  #{m}"
    File.open(m,"w") { |f| f << BUILD } 
    chmod(0755, m)
    
    m = "#{project}/util/sql_exec"  
    puts "  #{m}"
    File.open(m,"w") { |f| f << SQL_EXEC }
    chmod(0755, m)
  end

  # sets up the basic directory layout for a Ruport application
  def self.build_directory_structure
    mkdir project        
    puts "creating directories.."
    %w[ test config output data data/models lib lib/reports 
        lib/controllers sql util ].each do |d|
      m="#{project}/#{d}" 
      puts "  #{m}"
      mkdir(m)
    end
    
    puts "creating files.."
    %w[reports helpers controllers templates].each { |f|
      m = "#{project}/lib/#{f}.rb"
      puts "  #{m}"
      touch(m)
    }
  end

  def self.build_config
    write_file("config/environment.rb") { CONFIG }
  end

  # returns the project's name
  def self.project; @project; end

RAKEFILE = <<'END_RAKEFILE'
begin; require "rubygems"; rescue LoadError; end
require "rake/testtask"

task :default => [:test]

Rake::TestTask.new do |test|
  test.libs    << "test"
  test.pattern =  'test/**/test_*.rb'
  test.verbose =  true
end

task :build do
  if ENV['report']
    sh "ruby util/build report #{ENV['report']}"
  elsif ENV['controller']
    sh "ruby util/build controller #{ENV['controller']}"
  elsif ENV['model']
    sh "ruby util/build model #{ENV['model']}"
  end
end

task :run do
  sh "ruby lib/reports/#{ENV['report']}.rb"
end
END_RAKEFILE

CONFIG = <<END_CONFIG
require "ruport"

# Uncomment and modify the lines below if you want to use query.rb
#
# Ruport::Query.add_source :default, :user => "root",
#                                    :dsn  => "dbi:mysql:mydb"     

# Uncomment and modify the lines below if you want to use AAR
#  
# require "active_record"
# require "ruport/acts_as_reportable"
# ActiveRecord::Base.establish_connection(
#      :adapter  => 'mysql',
#      :host     => 'localhost',
#      :username => 'name',
#      :password => 'password',
#      :database => 'mydb'
# )

END_CONFIG

BUILD = <<'END_BUILD'
#!/usr/bin/env ruby

require 'fileutils'
require 'lib/init.rb'
require "ruport/util"
include FileUtils
include Ruport::Generator::Helpers

unless ARGV.length > 1
  puts "usage: build [command] [options]"
  exit
end

class_name = format_class_name(ARGV[1])

if ARGV[0].eql? "report"
  check_for_files
  File.open("lib/reports.rb", "a") { |f| 
    f.puts("require \"lib/reports/#{ARGV[1]}\"")
  }
REP = <<EOR
require "lib/init"
class #{class_name} < Ruport::Report

  def renderable_data(format)

  end
  
end

EOR

TEST = <<EOR
require "test/unit"
require "lib/reports/#{ARGV[1]}"

class Test#{class_name} < Test::Unit::TestCase
  def test_flunk
    flunk "Write your real tests here or in any test/test_* file"
  end
end
EOR

  File.open("lib/reports/#{ARGV[1]}.rb", "w") { |f| f << REP }
  puts "reports file: lib/reports/#{ARGV[1]}.rb"
  puts "test file: test/test_#{ARGV[1]}.rb"
  puts "class name: #{class_name}" 
  File.open("test/test_#{ARGV[1]}.rb","w") { |f| f << TEST }  

elsif ARGV[0].eql? "controller"

  check_for_files
  File.open("lib/controllers.rb","a") { |f|
    f.puts("require \"lib/controllers/#{ARGV[1]}\"")
  }
REP = <<EOR
require "lib/init"

class #{class_name} < Ruport::Controller
  stage :#{class_name.downcase}
end

class #{class_name}Formatter < Ruport::Formatter

  # change to your format name, or add additional formats
  renders :my_format, :for => #{class_name}

  def build_#{class_name.downcase}
  
  end

end
EOR

TEST = <<EOR
require "test/unit"
require "lib/controllers/#{ARGV[1]}"

class Test#{class_name} < Test::Unit::TestCase
  def test_flunk
    flunk "Write your real tests here or in any test/test_* file"
  end
end
EOR
  puts "controller file: lib/contollers/#{ARGV[1]}.rb"
  File.open("lib/controllers/#{ARGV[1]}.rb", "w") { |f| f << REP }
  puts "test file: test/test_#{ARGV[1]}.rb"

  puts "class name: #{class_name}"
  File.open("test/test_#{ARGV[1]}.rb","w") { |f| f << TEST } 
elsif ARGV[0].eql? "model"
  if File.exist?("data/models/#{ARGV[1]}.rb")
    raise "Model #{class_name} exists!"
  end
  File.open("data/models.rb","a") { |f|
    f.puts("require \"data/models/#{ARGV[1]}\"")
  }
REP = <<EOR
class #{class_name} < ActiveRecord::Base
  
  acts_as_reportable
  
end
EOR
  puts "model file: data/models/#{ARGV[1]}.rb"
  File.open("data/models/#{ARGV[1]}.rb", "w") { |f| f << REP }
  puts "class name: #{class_name}"
else
  puts "Incorrect usage."
end
END_BUILD

SQL_EXEC = <<'END_SQL'
#!/usr/bin/env ruby

require "lib/init"

puts Ruport::Query.new(ARGF.read).result
END_SQL

INIT = <<END_INIT
begin
  require "rubygems"
  gem "ruport","=#{Ruport::VERSION}"
  gem "ruport-util","=#{Ruport::Util::VERSION}"
rescue LoadError 
  nil
end
require "ruport"
require "ruport/util"
require "lib/helpers"
require "config/environment" 
require "lib/templates"
END_INIT

README = <<END_README

== rope : A Code Generation Tool for Ruby Reports ==

# Overview

Rope provides you with a number of simple utilities that script away
much of your boilerplate code, and also provide useful tools for
development

# The Basics

-- Starting a new rope project

$ rope labyrith
creating directories..
  labyrith/test
  labyrith/config
  labyrith/output
  labyrith/data
  labyrith/data/models
  labyrith/lib
  labyrith/lib/reports
  labyrith/lib/controllers
  labyrith/templates
  labyrith/sql
  labyrith/log
  labyrith/util
creating files..
  labyrith/lib/reports.rb
  labyrith/lib/helpers.rb
  labyrith/lib/controllers.rb
  labyrith/lib/init.rb
  labyrith/config/environment.rb
  labyrith/util/build
  labyrith/util/sql_exec
  labyrith/Rakefile
  labyrith/README

Successfully generated project: labyrith

Once this is complete, you'll have a large number of mostly empty
folders laying around, along with some helpful tools at your disposal.

-- utilities

 * build : A tool for generating reports and formatting system extensions
 * sql_exec: A simple tool for getting a result set from a SQL file
             (possibly with ERb)
 * Rakefile: Script for project automation tasks.  

-- directories

 * test : unit tests stored here can be auto-run
 * config : holds a configuration file which is shared across your applications
 * reports : when reports are autogenerated, they are stored here
 * controllers : autogenerated formatting system extensions are stored here
 * models : stores autogenerated activerecord models 
 * templates : erb templates may be stored here
 * sql : SQL files can be stored here, which are pre-processed by erb
 * log : The logger will automatically store your logfiles here by default
 * util : contains rope related tools

# Generating a Report definition with rope

 $ ./util/build report ghosts
 report file: lib/reports/ghosts.rb
 test file: test/test_ghosts.rb
 class name: Ghosts

 $ rake
 (in /home/sandal/labyrinth)
 /usr/bin/ruby -Ilib:test
"/usr/lib/ruby/gems/1.8/gems/rake-0.7.1/lib/rake/rake_test_loader.rb"
"test/test_ghosts.rb"
 Loaded suite /usr/lib/ruby/gems/1.8/gems/rake-0.7.1/lib/rake/rake_test_loader
 Started
 F
 Finished in 0.001119 seconds.

   1) Failure:
   test_flunk(TestGhosts) [./test/test_ghosts.rb:6]:
   Write your real tests here or in any test/test_* file.

   1 tests, 1 assertions, 1 failures, 0 errors
   rake aborted!
   Command failed with status (1): [/usr/bin/ruby -Ilib:test
"/usr/lib/ruby/ge...]

   (See full trace by running task with --trace)

You can now edit lib/reports/ghosts.rb as needed and write tests for
it in test/test_ghosts.rb without having to hook up any underplumbing.

# Rope's Auto-generated Configuration File

-- Basic Details

roped projects will automatically make use of the configuration details in
config/environment.rb , which can be used to set up database
connections, Ruport's mailer, and other project information.

The default file is shown below.

require "ruport"

# Uncomment and modify the lines below if you want to use query.rb
#
# Ruport::Query.add_source :default, :user => "root",
#                                    :dsn  => "dbi:mysql:mydb"     

# Uncomment and modify the lines below if you want to use AAR
#  
# require "active_record"
# require "ruport/acts_as_reportable"
# ActiveRecord::Base.establish_connection(
#      :adapter  => 'mysql',
#      :host     => 'localhost',
#      :username => 'name',
#      :password => 'password',
#      :database => 'mydb'
# )

You'll need to tweak this as needed to fit your database configuration needs.
If you need to require any third party libraries which are shared across your
project, you should do it in this file.

# Custom rendering with rope generators.

-- By Example

 $ rope my_reverser
 $ cd my_reverser
 $ rake build controller=reverser

Edit test/test_reverser.rb to look like the code below:

 require "test/unit"
 require "lib/controllers/reverser"

 class TestReverser < Test::Unit::TestCase
   def test_reverser
     assert_equal "baz", Reverser.render_text("zab")
   end
 end

Now edit lib/controllers/reverser.rb to look like this:

 require "lib/init"

 class Reverser < Ruport::Controller
   stage :reverser
 end

 class ReverserFormatter < Ruport::Formatter

   renders :text, :for => Reverser

   def build_reverser
     output << data.reverse
   end

 end

The tests should pass.  You can now generate a quick report using this controller

 $ rake build report=reversed_report

Edit test/test_reversed_report.rb as such:

 require "test/unit"
 require "lib/reports/reversed_report"

 class TestReversedReport < Test::Unit::TestCase
   def test_reversed_report
     report = ReversedReport.new
     report.message = "hello"
     assert_equal "olleh", report.to_text
   end
 end

edit lib/reports/reversed_report.rb as below and run the tests.

 require "lib/init"  
 require "lib/controllers/reverser"
 class ReversedReport < Ruport::Report

   renders_with Reverser
   attr_accessor :message

   def generate
     message
   end

 end

# ActiveRecord integration the lazy way.

Ruport now has built in support for acts_as_reportable, which provides
ActiveRecord integration with ruport.

-- Setup details

Edit the following code in config/environment.rb 
(change as needed to match your config information)

 ActiveRecord::Base.establish_connection(
       :adapter  => 'mysql',
       :host     => 'localhost',
       :username => 'name',
       :password => 'password',
       :database => 'mydb'
 )

-- Generating a model 

Here is an example of generating the model file:

$ util/build model my_model
model file: data/models/my_model.rb             
class name: MyModel                             
  
This will create a barebones model that looks like this:

class MyModel < ActiveRecord::Base

  acts_as_reportable

end

The data/models.rb file will require all generated models,
but you can of course require specific models in your reports.  

This should be enought to get you started, but for more complex needs, 
check the appropriate acts_as_reportable / ActiveRecord documentation.   

# Getting Help / Reporting Problems

rope is an officially supported utility for the Ruby Reports project, in
the ruport-util package.   If you run into problems or have feature requests,
please contact us at http://list.rubyreports.org
END_README

  end
end
