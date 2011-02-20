# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{transaction-simple}
  s.version = "1.4.0"

  s.required_rubygems_version = nil if s.respond_to? :required_rubygems_version=
  s.authors = ["Austin Ziegler"]
  s.cert_chain = nil
  s.date = %q{2007-02-03}
  s.description = %q{Transaction::Simple provides a generic way to add active transaction support to objects. The transaction methods added by this module will work with most objects, excluding those that cannot be Marshal-ed (bindings, procedure objects, IO instances, or singleton objects).}
  s.email = %q{austin@rubyforge.org}
  s.extra_rdoc_files = ["History.txt", "Install.txt", "Licence.txt", "Readme.txt"]
  s.files = ["History.txt", "Install.txt", "Licence.txt", "Manifest.txt", "Rakefile", "Readme.txt", "lib/transaction/simple.rb", "lib/transaction/simple/group.rb", "lib/transaction/simple/threadsafe.rb", "lib/transaction/simple/threadsafe/group.rb", "setup.rb", "test/test_all.rb", "test/test_broken_graph.rb", "test/test_transaction_simple.rb", "test/test_transaction_simple_group.rb", "test/test_transaction_simple_threadsafe.rb"]
  s.homepage = %q{http://rubyforge.org/projects/trans-simple}
  s.require_paths = ["lib"]
  s.required_ruby_version = Gem::Requirement.new(">= 1.8.2")
  s.rubyforge_project = %q{trans-simple}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{Simple object transaction support for Ruby.}
  s.test_files = ["test/test_all.rb"]

  if s.respond_to? :specification_version then
    s.specification_version = 1

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<hoe>, [">= 1.1.7"])
    else
      s.add_dependency(%q<hoe>, [">= 1.1.7"])
    end
  else
    s.add_dependency(%q<hoe>, [">= 1.1.7"])
  end
end
