##########################################################################################
# Ruby Scripting Step for Kettle 
# Created by Slawomir Chodnicki 
# http://type-exit.org
##########################################################################################
# Using Ruby Gems 
#
# Ruby Gems is a library package system for the Ruby language. Most non-standard
# ruby libraries are packaged into gems and made avaialble for download at online
# repositories. See http://docs.rubygems.org/ for details.
#
# The Gem Home Directory
#
# The ruby scripting step uses the <kettle-dir>/plugins/steps/Ruby/gems folder
# as its default GEM_HOME. A few gems are bundled with the ruby scripting step
# for demonstration purposes. Feel free to delete them if you don't need those. 
#
# You can specify a different GEM_HOME directory for a scripting step in the 
# "Ruby Runtime" tab at the bottom of the ruby step dialog. Should you wish to 
# generally use a different GEM_HOME without having to specify it each time you 
# can define the Kettle variable "RUBY_GEM_HOME", which will then be used whenever
# you don't specify a different gem home manually in the dialog.
#
# Please note that there's little support for native ruby extensions in JRuby. It
# is probably best to choose a GEM_HOME that contains gems installed by the JRuby
# gem command, since it selects gem versions most compatible with JRuby when
# installing them. 
#
# How to Maintain the Gems Folder
#
# If you'd like to keep the gems folder at <kettle-dir>/plugins/steps/Ruby/gems
# here's how to properly invoke the gems command to maintain the gems. 
#
# Change to <kettle-dir>/plugins/steps/Ruby directory and invoke the JRuby gem command
# java -Djruby.gem.home=gems -jar lib/jruby-complete.jar -S gem 
#
# To updade outdated gems, you'd type:
# java -Djruby.gem.home=gems -jar lib/jruby-complete.jar -S gem update outdated --remote
#
# To list available gems with "pdf" in their name you'd type:
# java -Djruby.gem.home=gems -jar lib/jruby-complete.jar -S gem query -n pdf --remote
#
# To install the twitter gem from repostitory you'd type
# java -Djruby.gem.home=gems -jar lib/jruby-complete.jar -S gem install twitter --remote
#
# See http://docs.rubygems.org/ for the full gem command reference
# See http://ruby-toolbox.com/ for popular ruby gems
##########################################################################################
