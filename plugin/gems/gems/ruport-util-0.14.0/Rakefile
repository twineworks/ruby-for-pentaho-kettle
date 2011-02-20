begin
  require "rubygems"
rescue LoadError
  nil
end

require 'rake'
require "rake/rdoctask"
require "rake/gempackagetask"

require 'spec/rake/spectask'

task :default => [:test]

desc "Run all tests"
Spec::Rake::SpecTask.new('test') do |t|
  t.spec_files = FileList['test/test_*.rb']
end

desc "Generate specdocs for examples for inclusion in RDoc"
Spec::Rake::SpecTask.new('specdoc') do |t|
  t.spec_files = FileList['test/test_*.rb']
  t.spec_opts = ["--format", "rdoc"]
  t.out = 'EXAMPLES.rd'
end

desc "Generate HTML report for failing examples"
Spec::Rake::SpecTask.new('failing_examples_with_html') do |t|
  t.spec_files = FileList['test/test_*.rb']
  t.spec_opts = ["--format", "html:failing_examples.html", "--diff"]
  t.fail_on_error = false
end

spec = Gem::Specification.new do |spec|
  spec.name = "ruport-util"
  spec.version = "0.14.0"
  spec.platform = Gem::Platform::RUBY
  spec.summary = "A set of tools and helper libs for Ruby Reports"
  spec.files =  Dir.glob("{example,lib,test,bin}/**/**/*") +
                      ["Rakefile"]
  
  spec.require_path = "lib"
  
  spec.test_files = Dir[ "test/test_*.rb" ]
  spec.bindir = "bin"
  spec.executables = FileList["rope", "csv2ods"]
  spec.has_rdoc = true
  spec.extra_rdoc_files = %w{INSTALL}
  spec.rdoc_options << '--title' << 'ruport-util Documentation' <<
                       '--main' << 'INSTALL' << '-q'
  spec.add_dependency('ruport', ">=1.6.0")
  spec.add_dependency('mailfactory',">=1.2.3")
  spec.add_dependency('rubyzip','>=0.9.1')
  spec.author = "Gregory Brown"
  spec.email = "  gregory.t.brown@gmail.com"
  spec.rubyforge_project = "ruport"
  spec.homepage = "http://code.rubyreports.org"
  spec.description = <<END_DESC
  ruport-util provides a number of utilities and helper libs
  for Ruby Reports
END_DESC
end

Rake::RDocTask.new do |rdoc|
  rdoc.rdoc_files.include( "COPYING", "INSTALL",
                           "LICENSE", "lib/" )
  rdoc.main     = "INSTALL"
  rdoc.rdoc_dir = "doc/html"
  rdoc.title    = "Ruport Documentation"
end

Rake::GemPackageTask.new(spec) do |pkg|
  pkg.need_zip = true
  pkg.need_tar = true
end

begin
  require 'rcov/rcovtask'
  Rcov::RcovTask.new do |t|
    t.test_files = Dir[ "test/test_*.rb" ]
  end
rescue LoadError
  nil
end

## RSpec Wrapper

require 'test/helper/layout'

class String
  def /(obj)
    File.join(self, obj.to_s)
  end
end

SPEC_BASE = File.expand_path('test')

# ignore files with these paths
ignores = [ './helper/*', './helper.rb' ]

files = Dir[SPEC_BASE/'**'/'*.rb']
ignores.each do |ignore|
  ignore_files = Dir[SPEC_BASE/ignore]
  ignore_files.each do |ignore_file|
    files.delete File.expand_path(ignore_file)
  end
end

files.sort!

spec_layout = Hash.new{|h,k| h[k] = []}

files.each do |file|
  name = file.gsub(/^#{SPEC_BASE}/, '.')
  dir_name = File.dirname(name)[2..-1] || './'
  task_name = 'wrap' + ([:test] + dir_name.split('/')).join(':')
  spec_layout[task_name] << file
end

desc "Test all"
task "wraptest" => [] do
  wrap = SpecWrap.new(*spec_layout.values.flatten)
  wrap.run
end
