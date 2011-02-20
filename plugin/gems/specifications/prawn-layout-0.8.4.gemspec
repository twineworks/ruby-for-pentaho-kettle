# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{prawn-layout}
  s.version = "0.8.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Gregory Brown"]
  s.date = %q{2010-02-24}
  s.description = %q{  An extension to Prawn that provides table support and other layout functionality
}
  s.email = %q{  gregory.t.brown@gmail.com}
  s.extra_rdoc_files = ["README"]
  s.files = ["examples/grid/column_gutter_grid.rb", "examples/grid/show_grid.rb", "examples/grid/multi_boxes.rb", "examples/grid/bounding_boxes.rb", "examples/grid/simple_grid.rb", "examples/page_layout/padded_box.rb", "examples/page_layout/lazy_bounding_boxes.rb", "examples/example_helper.rb", "examples/table/table_header_underline.rb", "examples/table/currency.csv", "examples/table/addressbook.csv", "examples/table/fancy_table.rb", "examples/table/table.rb", "examples/table/table_widths.rb", "examples/table/table_border_color.rb", "examples/table/table_alignment.rb", "examples/table/cell.rb", "examples/table/table_colspan.rb", "examples/table/table_header_color.rb", "lib/prawn/layout.rb", "lib/prawn/table.rb", "lib/prawn/table/cell.rb", "lib/prawn/layout/grid.rb", "lib/prawn/layout/page.rb", "spec/table_spec.rb", "spec/spec_helper.rb", "spec/grid_spec.rb", "Rakefile", "README"]
  s.homepage = %q{http://prawn.majesticseacreature.com}
  s.rdoc_options = ["--title", "Prawn Documentation", "--main", "README", "-q"]
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{prawn}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{An extension to Prawn that provides table support and other layout functionality}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
