##########################################################################################
# Ruby Script Step for Kettle
# Created by Twineworks GmbH
# http://twineworks.com
##########################################################################################
# Using Ruby Gems 
#
# Ruby Gems is a library package system for the ruby language. Most ruby
# libraries are packaged into gems and made available for download at online
# repositories. See https://rubygems.org/ for details.
#
# The Gem Home Directory
#
# The ruby scripting step uses the <plugin-dir>/gems folder as its default GEM_HOME.
#
# You can specify a different GEM_HOME directory for a scripting step in the 
# "Ruby Runtime" tab at the bottom of the ruby step dialog. Should you wish to 
# generally use a different GEM_HOME without having to specify it each time, you
# can define the Kettle variable "RUBY_GEM_HOME", which will then be used whenever
# you don't specify a different gem home manually in the dialog. The kettle.properties
# file would be a good location to define that variable.
#
# Please note that there's limited support for native ruby extensions in JRuby. It
# is probably best to choose a GEM_HOME that contains gems installed by the JRuby
# gem command, since it selects gem versions most compatible with JRuby when
# installing them. 
#
# How to Maintain the Gems Folder
#
# If you'd like to keep the gems folder at <plugin-dir>/gems,
# here's how to properly maintain it:
#
# Change to the <plugin-dir> directory and invoke the JRuby gem command to install
# bundler. Bundler will maintain your gems for you. See http://bundler.io/ for details.
#
# $ java -jar lib/jruby-complete*.jar -S gem install -i gems --no-rdoc --no-ri bundler
# > Successfully installed bundler-1.15.4
# > 1 gem installed
#
# Create a Gemfile for bundler to use. For example:
# --------------------------------------------------------------
# source 'http://rubygems.org'
#
#   gem 'chronic' # https://github.com/mojombo/chronic
# --------------------------------------------------------------
#
# Now run bundle to fetch your gems. The PATH variable makes sure JRuby finds the
# bundler gem, and the GEM_HOME and GEM_PATH variables ensure that bundler knows where to
# place them for you.
#
# $ PATH="gems/bin:$PATH" GEM_HOME="gems" GEM_PATH="gems" java -jar lib/jruby-complete*.jar -S bundle
# > Fetching gem metadata from http://rubygems.org/.................
# > Fetching version metadata from http://rubygems.org/..
# > Using bundler 1.15.4
# > Fetching chronic 0.10.2
# > Installing chronic 0.10.2
# > Bundle complete! 1 Gemfile dependency, 2 gems now installed.
# > Use `bundle info [gemname]` to see where a bundled gem is installed.
#
# With that in place you can now use the chronic gem in your scripts, like so:

require 'chronic'

{
	"date_guess" => Chronic::parse("tomorrow afternoon")
}
