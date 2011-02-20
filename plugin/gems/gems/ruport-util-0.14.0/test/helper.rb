begin
  require "rubygems"
  gem "ruport", "=1.6.0" 
rescue LoadError
  nil
end   

class Array
  def to_table(columns)
    Table(columns) { |t| each { |r| t << r }}   
  end
end

require "spec"
require 'ruport'
this = File.dirname(__FILE__)
lib = File.expand_path(File.join(this, '..', 'lib'))
$LOAD_PATH.unshift(lib)
require "ruport/util"

# Use this to require optional dependencies where tests are expected to fail if
# a library is not installed, for example Hpricot or Scruffy.
# It will be parsed by the wrapper and marked.

def testcase_requires(*following)
  following.each do |file|
    require(file.to_s)
  end
rescue LoadError => ex
  puts ex
  puts "Can't run #{$0}: #{ex}"
  puts "Usually you should not worry about this failure, just install the"
  puts "library and try again (if you want to use that feature later on)"
end
