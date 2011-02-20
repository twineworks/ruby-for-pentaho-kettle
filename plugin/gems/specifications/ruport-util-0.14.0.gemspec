# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{ruport-util}
  s.version = "0.14.0"

  s.required_rubygems_version = nil if s.respond_to? :required_rubygems_version=
  s.authors = ["Gregory Brown"]
  s.cert_chain = nil
  s.date = %q{2008-04-02}
  s.description = %q{ruport-util provides a number of utilities and helper libs for Ruby Reports}
  s.email = %q{  gregory.t.brown@gmail.com}
  s.executables = ["rope", "csv2ods"]
  s.extra_rdoc_files = ["INSTALL"]
  s.files = ["example/data", "example/data/amline_settings.xml", "example/data/blank.xlsx", "example/data/amline_graph.xml", "example/data/blank.ods", "example/draw_graph.rb", "example/form.rb", "example/gruff_report.rb", "example/ods.rb", "example/invoice_report.rb", "example/managed_report.rb", "example/mailer.rb", "example/graph_report.rb", "lib/ruport", "lib/ruport/util", "lib/ruport/util/graph", "lib/ruport/util/graph/scruffy.rb", "lib/ruport/util/graph/amline.rb", "lib/ruport/util/graph/o_f_c.rb", "lib/ruport/util/graph/gruff.rb", "lib/ruport/util/pdf", "lib/ruport/util/pdf/form.rb", "lib/ruport/util/bench.rb", "lib/ruport/util/graph.rb", "lib/ruport/util/ods.rb", "lib/ruport/util/query.rb", "lib/ruport/util/report_manager.rb", "lib/ruport/util/generator.rb", "lib/ruport/util/xls.rb", "lib/ruport/util/mailer.rb", "lib/ruport/util/report.rb", "lib/ruport/util/invoice.rb", "lib/ruport/util/xls_table.rb", "lib/ruport/util/ods_table.rb", "lib/ruport/util.rb", "lib/open_flash_chart.rb", "test/helper", "test/helper/layout.rb", "test/helper/wrap.rb", "test/samples", "test/samples/data.csv", "test/samples/foo.rtxt", "test/samples/people.ods", "test/samples/people.xls", "test/test_format_xls.rb", "test/helper.rb", "test/test_hpricot_traverser.rb", "test/test_query.rb", "test/test_report_manager.rb", "test/test_graph_renderer.rb", "test/test_graph_ofc.rb", "test/test_mailer.rb", "test/test_report.rb", "test/test_invoice.rb", "test/test_format_ods.rb", "test/test_ods_table.rb", "test/test_xls_table.rb", "bin/csv2ods", "bin/rope", "Rakefile", "INSTALL"]
  s.homepage = %q{http://code.rubyreports.org}
  s.rdoc_options = ["--title", "ruport-util Documentation", "--main", "INSTALL", "-q"]
  s.require_paths = ["lib"]
  s.required_ruby_version = Gem::Requirement.new("> 0.0.0")
  s.rubyforge_project = %q{ruport}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{A set of tools and helper libs for Ruby Reports}
  s.test_files = ["test/test_format_xls.rb", "test/test_hpricot_traverser.rb", "test/test_query.rb", "test/test_report_manager.rb", "test/test_graph_renderer.rb", "test/test_graph_ofc.rb", "test/test_mailer.rb", "test/test_report.rb", "test/test_invoice.rb", "test/test_format_ods.rb", "test/test_ods_table.rb", "test/test_xls_table.rb"]

  if s.respond_to? :specification_version then
    s.specification_version = 1

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<ruport>, [">= 1.6.0"])
      s.add_runtime_dependency(%q<mailfactory>, [">= 1.2.3"])
      s.add_runtime_dependency(%q<rubyzip>, [">= 0.9.1"])
    else
      s.add_dependency(%q<ruport>, [">= 1.6.0"])
      s.add_dependency(%q<mailfactory>, [">= 1.2.3"])
      s.add_dependency(%q<rubyzip>, [">= 0.9.1"])
    end
  else
    s.add_dependency(%q<ruport>, [">= 1.6.0"])
    s.add_dependency(%q<mailfactory>, [">= 1.2.3"])
    s.add_dependency(%q<rubyzip>, [">= 0.9.1"])
  end
end
