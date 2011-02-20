# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{prawn}
  s.version = "0.8.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Gregory Brown"]
  s.date = %q{2010-02-24}
  s.description = %q{Prawn is a fast, tiny, and nimble PDF generator for Ruby}
  s.email = %q{  gregory.t.brown@gmail.com}
  s.files = ["lib/prawn.rb"]
  s.homepage = %q{http://wiki.github.com/sandal/prawn}
  s.post_install_message = %q{
  Welcome to Prawn, the best pure-Ruby PDF solution ever!
  This is version 0.8
   
  For those coming from Prawn 0.7 or earlier, note that this release has
  some API breaking changes as well as many new features.  *** You'll want 
  to know about these changes, as we will no longer be supporting
  Prawn 0.7 or any earlier version of Prawn***

  For details on what has changed, see:
    http://wiki.github.com/sandal/prawn/changelog

  If you have questions, contact us at:
    http://groups.google.com/group/prawn-ruby

  To submit a patch or report a bug, select the appropriate package below: 
    http://github.com/sandal/prawn
    http://github.com/sandal/prawn-layout
    http://github.com/madriska/prawn-security

  Prawn is meant for experienced Ruby hackers, so if you are new to Ruby, you
  might want to wait until you've had some practice with the language before
  expecting Prawn to work for you.  Things may change after 1.0, but for now
  if you're not ready to read source code and patch bugs or missing features 
  yourself (with our help), Prawn might not be the right fit.

  But if you know what you're getting yourself into, enjoy!
  }
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{prawn}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{A fast and nimble PDF generator for Ruby}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<prawn-core>, [">= 0.8.4", "< 0.9"])
      s.add_runtime_dependency(%q<prawn-layout>, [">= 0.8.4", "< 0.9"])
      s.add_runtime_dependency(%q<prawn-security>, [">= 0.8.4", "< 0.9"])
    else
      s.add_dependency(%q<prawn-core>, [">= 0.8.4", "< 0.9"])
      s.add_dependency(%q<prawn-layout>, [">= 0.8.4", "< 0.9"])
      s.add_dependency(%q<prawn-security>, [">= 0.8.4", "< 0.9"])
    end
  else
    s.add_dependency(%q<prawn-core>, [">= 0.8.4", "< 0.9"])
    s.add_dependency(%q<prawn-layout>, [">= 0.8.4", "< 0.9"])
    s.add_dependency(%q<prawn-security>, [">= 0.8.4", "< 0.9"])
  end
end
