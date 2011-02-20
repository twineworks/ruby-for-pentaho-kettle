# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{prawn-security}
  s.version = "0.8.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Brad Ediger"]
  s.date = %q{2010-02-24}
  s.description = %q{  Prawn/Security adds document encryption, password protection, and permissions to Prawn.
}
  s.email = %q{brad.ediger@madriska.com}
  s.extra_rdoc_files = ["README", "LICENSE", "COPYING"]
  s.files = ["examples/hello_foo.rb", "examples/example_helper.rb", "lib/prawn/arcfour.rb", "lib/prawn/security.rb", "spec/security_spec.rb", "spec/spec_helper.rb", "Rakefile", "README", "LICENSE", "COPYING"]
  s.homepage = %q{http://github.com/madriska/prawn-security/}
  s.rdoc_options = ["--title", "Prawn/Security Documentation", "--main", "README", "-q"]
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{prawn-security}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{Popular Password Protection & Permissions for Prawn PDFs}
  s.test_files = ["spec/security_spec.rb"]

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
