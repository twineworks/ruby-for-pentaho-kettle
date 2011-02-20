# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{pdf-writer}
  s.version = "1.1.8"

  s.required_rubygems_version = nil if s.respond_to? :required_rubygems_version=
  s.authors = ["Austin Ziegler"]
  s.autorequire = %q{pdf/writer}
  s.cert_chain = nil
  s.date = %q{2008-03-16}
  s.default_executable = %q{techbook}
  s.description = %q{This library provides the ability to create PDF documents using only native Ruby libraries. There are several demo programs available in the demo/ directory. The canonical documentation for PDF::Writer is "manual.pdf", which can be generated using bin/techbook (just "techbook" for RubyGem users) and the manual file "manual.pwd".}
  s.email = %q{austin@rubyforge.org}
  s.executables = ["techbook"]
  s.extra_rdoc_files = ["README", "ChangeLog", "LICENCE"]
  s.files = ["README", "LICENCE", "ChangeLog", "bin/techbook", "lib/pdf", "lib/pdf/charts", "lib/pdf/charts.rb", "lib/pdf/math.rb", "lib/pdf/quickref.rb", "lib/pdf/simpletable.rb", "lib/pdf/techbook.rb", "lib/pdf/writer", "lib/pdf/writer.rb", "lib/pdf/charts/stddev.rb", "lib/pdf/writer/arc4.rb", "lib/pdf/writer/fontmetrics.rb", "lib/pdf/writer/fonts", "lib/pdf/writer/graphics", "lib/pdf/writer/graphics.rb", "lib/pdf/writer/lang", "lib/pdf/writer/lang.rb", "lib/pdf/writer/object", "lib/pdf/writer/object.rb", "lib/pdf/writer/ohash.rb", "lib/pdf/writer/oreader.rb", "lib/pdf/writer/state.rb", "lib/pdf/writer/strokestyle.rb", "lib/pdf/writer/fonts/Courier-Bold.afm", "lib/pdf/writer/fonts/Courier-BoldOblique.afm", "lib/pdf/writer/fonts/Courier-Oblique.afm", "lib/pdf/writer/fonts/Courier.afm", "lib/pdf/writer/fonts/Helvetica-Bold.afm", "lib/pdf/writer/fonts/Helvetica-BoldOblique.afm", "lib/pdf/writer/fonts/Helvetica-Oblique.afm", "lib/pdf/writer/fonts/Helvetica.afm", "lib/pdf/writer/fonts/MustRead.html", "lib/pdf/writer/fonts/Symbol.afm", "lib/pdf/writer/fonts/Times-Bold.afm", "lib/pdf/writer/fonts/Times-BoldItalic.afm", "lib/pdf/writer/fonts/Times-Italic.afm", "lib/pdf/writer/fonts/Times-Roman.afm", "lib/pdf/writer/fonts/ZapfDingbats.afm", "lib/pdf/writer/graphics/imageinfo.rb", "lib/pdf/writer/lang/en.rb", "lib/pdf/writer/object/action.rb", "lib/pdf/writer/object/annotation.rb", "lib/pdf/writer/object/catalog.rb", "lib/pdf/writer/object/contents.rb", "lib/pdf/writer/object/destination.rb", "lib/pdf/writer/object/encryption.rb", "lib/pdf/writer/object/font.rb", "lib/pdf/writer/object/fontdescriptor.rb", "lib/pdf/writer/object/fontencoding.rb", "lib/pdf/writer/object/image.rb", "lib/pdf/writer/object/info.rb", "lib/pdf/writer/object/outline.rb", "lib/pdf/writer/object/outlines.rb", "lib/pdf/writer/object/page.rb", "lib/pdf/writer/object/pages.rb", "lib/pdf/writer/object/procset.rb", "lib/pdf/writer/object/viewerpreferences.rb", "demo/chunkybacon.rb", "demo/code.rb", "demo/colornames.rb", "demo/demo.rb", "demo/gettysburg.rb", "demo/hello.rb", "demo/individual-i.rb", "demo/pac.rb", "demo/qr-language.rb", "demo/qr-library.rb", "images/bluesmoke.jpg", "images/chunkybacon.jpg", "images/chunkybacon.png", "manual.pwd"]
  s.homepage = %q{http://rubyforge.org/projects/ruby-pdf}
  s.rdoc_options = ["--title", "PDF::Writer", "--main", "README", "--line-numbers"]
  s.require_paths = ["lib"]
  s.required_ruby_version = Gem::Requirement.new("> 0.0.0")
  s.rubyforge_project = %q{ruby-pdf}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{A pure Ruby PDF document creation library.}

  if s.respond_to? :specification_version then
    s.specification_version = 1

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<color>, [">= 1.4.0"])
      s.add_runtime_dependency(%q<transaction-simple>, ["~> 1.3"])
    else
      s.add_dependency(%q<color>, [">= 1.4.0"])
      s.add_dependency(%q<transaction-simple>, ["~> 1.3"])
    end
  else
    s.add_dependency(%q<color>, [">= 1.4.0"])
    s.add_dependency(%q<transaction-simple>, ["~> 1.3"])
  end
end
