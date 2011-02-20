# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{weakling}
  s.version = "0.0.4"
  s.platform = %q{java}

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Charles Oliver Nutter"]
  s.date = %q{2010-06-04}
  s.description = %q{A modified WeakRef impl for JRuby plus some weakref-related tools}
  s.email = ["headius@headius.com"]
  s.files = ["lib/refqueue.jar", "lib/weakling.rb", "lib/weakling/collections.rb", "ext/RefqueueService.java", "ext/org/jruby/ext/RefQueueLibrary.java", "examples/id_hash.rb", "examples/refqueue_use.rb", "HISTORY.txt", "README.txt", "weakling.gemspec", "Rakefile"]
  s.homepage = %q{http://github.com/headius/weakling}
  s.require_paths = ["lib"]
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{A modified WeakRef impl for JRuby plus some weakref-related tools}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
