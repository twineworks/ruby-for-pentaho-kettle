# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{serenity-odt}
  s.version = "0.2.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Tomas Kramar"]
  s.date = %q{2010-11-03}
  s.description = %q{    Embedded ruby for OpenOffice Text Document (.odt) files. You provide an .odt template
    with ruby code in a special markup and the data, and Serenity generates the document.
    Very similar to .erb files.
}
  s.email = %q{kramar.tomas@gmail.com}
  s.files = ["lib/serenity/debug.rb", "lib/serenity/escape_xml.rb", "lib/serenity/generator.rb", "lib/serenity/line.rb", "lib/serenity/node_type.rb", "lib/serenity/odteruby.rb", "lib/serenity/template.rb", "lib/serenity/xml_reader.rb", "lib/serenity.rb", "README.md", "Rakefile", "LICENSE", "spec/escape_xml_spec.rb", "spec/generator_spec.rb", "spec/odteruby_spec.rb", "spec/spec_helper.rb", "spec/support/matchers/be_a_document.rb", "spec/support/matchers/contain_in.rb", "spec/template_spec.rb", "spec/xml_reader_spec.rb", "fixtures/advanced.odt", "fixtures/footer.odt", "fixtures/header.odt", "fixtures/loop.odt", "fixtures/loop_table.odt", "fixtures/table_rows.odt", "fixtures/variables.odt"]
  s.require_paths = ["lib"]
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{Embedded ruby for OpenOffice Text Document (.odt) files}
  s.test_files = ["spec/escape_xml_spec.rb", "spec/generator_spec.rb", "spec/odteruby_spec.rb", "spec/spec_helper.rb", "spec/support/matchers/be_a_document.rb", "spec/support/matchers/contain_in.rb", "spec/template_spec.rb", "spec/xml_reader_spec.rb", "fixtures/advanced.odt", "fixtures/footer.odt", "fixtures/header.odt", "fixtures/loop.odt", "fixtures/loop_table.odt", "fixtures/table_rows.odt", "fixtures/variables.odt"]

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<rubyzip>, [">= 0.9.1"])
      s.add_development_dependency(%q<rspec>, [">= 1.2.9"])
    else
      s.add_dependency(%q<rubyzip>, [">= 0.9.1"])
      s.add_dependency(%q<rspec>, [">= 1.2.9"])
    end
  else
    s.add_dependency(%q<rubyzip>, [">= 0.9.1"])
    s.add_dependency(%q<rspec>, [">= 1.2.9"])
  end
end
