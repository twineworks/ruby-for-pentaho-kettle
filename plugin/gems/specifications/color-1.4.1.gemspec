# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{color}
  s.version = "1.4.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Austin Ziegler", "Matt Lyon"]
  s.cert_chain = ["-----BEGIN CERTIFICATE-----\nMIIDNjCCAh6gAwIBAgIBADANBgkqhkiG9w0BAQUFADBBMQ8wDQYDVQQDDAZhdXN0\naW4xGTAXBgoJkiaJk/IsZAEZFglydWJ5Zm9yZ2UxEzARBgoJkiaJk/IsZAEZFgNv\ncmcwHhcNMDcxMTMwMDE0OTAwWhcNMDgxMTI5MDE0OTAwWjBBMQ8wDQYDVQQDDAZh\ndXN0aW4xGTAXBgoJkiaJk/IsZAEZFglydWJ5Zm9yZ2UxEzARBgoJkiaJk/IsZAEZ\nFgNvcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDOSg1riVV22ord\nq0t4YVx57GDPMqdjlnQ5M7D9iBnnW0c8pifegbb0dm+mC9hJSuPtcJS53+YPTy9F\nwlZbjI2cN+P0QLUUTOlZus2sHq7Pr9jz2nJf8hCT7t5Vlopv1N/xlKtXqpcyEkhJ\nJHTrxe1avGwuq8DIAIN01moQJ+hJlgrnR2eRJRazTGiXKBLGAFXDl/Agn78MHx6w\npzZ2APydo6Nuk7Ktq1MvCHzLzCACbOlYawFk3/9dbsmHhVjsi6YW+CpEJ2BnTy8X\nJBXlyNTk1JxDmcs3RzNr+9AmDQh3u4LcmJnWxtLJo9e7UBxH/bwVORJyf8dAOmOm\nHO6bFTLvAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgSwMB0GA1UdDgQW\nBBT9e1+pFfcV1LxVxILANqLtZzI/XTANBgkqhkiG9w0BAQUFAAOCAQEAhg42pvrL\nuVlqAaHqV88KqgnW2ymCWm0ePohicFTcyiS5Yj5cN3OXLsPV2x12zqvLCFsfpA4u\nD/85rngKFHITSW0h9e/CIT/pwQA6Uuqkbr0ypkoU6mlNIDS10PlK7aXXFTCP9X3f\nIndAajiNRgKwb67nj+zpQwHa6dmooyRQIRRijrMKTgY6ebaCCrm7J3BLLTJAyxOW\n+1nD0cuTkBEKIuSVK06E19Ml+xWt2bdtS9Wz/8jHivJ0SvUpbmhKVzh1rBslwm65\nJpQgg3SsV23vF4qkCa2dt1FL+FeWJyCdj23DV3598X72RYiK3D6muWURck16jqeA\nBRvUFuFHOwa/yA==\n-----END CERTIFICATE-----\n"]
  s.date = %q{2010-02-07}
  s.description = %q{The capabilities of the Color library are limited to pure mathematical
manipulation of the colours based on colour theory without reference to colour
profiles (such as sRGB or Adobe RGB). For most purposes, when working with the
RGB and HSL colours, this won't matter. However, some colour models (like CIE
L*a*b*) are not supported because Color does not yet support colour profiles,
giving no meaningful way to convert colours in absolute colour spaces (like
L*a*b*, XYZ) to non-absolute colour spaces (like RGB).}
  s.email = ["austin@rubyforge.org", "matt@postsomnia.com"]
  s.extra_rdoc_files = ["History.txt", "Install.txt", "Licence.txt", "Manifest.txt", "README.txt"]
  s.files = ["History.txt", "Install.txt", "Licence.txt", "Manifest.txt", "README.txt", "Rakefile", "lib/color.rb", "lib/color/cmyk.rb", "lib/color/css.rb", "lib/color/grayscale.rb", "lib/color/hsl.rb", "lib/color/palette.rb", "lib/color/palette/adobecolor.rb", "lib/color/palette/gimp.rb", "lib/color/palette/monocontrast.rb", "lib/color/rgb-colors.rb", "lib/color/rgb.rb", "lib/color/rgb/metallic.rb", "lib/color/yiq.rb", "setup.rb", "test/test_adobecolor.rb", "test/test_all.rb", "test/test_cmyk.rb", "test/test_color.rb", "test/test_css.rb", "test/test_gimp.rb", "test/test_grayscale.rb", "test/test_hsl.rb", "test/test_monocontrast.rb", "test/test_rgb.rb", "test/test_yiq.rb"]
  s.homepage = %q{http://color.rubyforge.org/}
  s.rdoc_options = ["--main", "README.txt"]
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{color}
  s.rubygems_version = %q{1.5.0}
  s.summary = %q{Colour management with Ruby}
  s.test_files = ["test/test_adobecolor.rb", "test/test_all.rb", "test/test_cmyk.rb", "test/test_color.rb", "test/test_css.rb", "test/test_gimp.rb", "test/test_grayscale.rb", "test/test_hsl.rb", "test/test_monocontrast.rb", "test/test_rgb.rb", "test/test_yiq.rb"]

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<rubyforge>, [">= 2.0.3"])
      s.add_development_dependency(%q<gemcutter>, [">= 0.3.0"])
      s.add_development_dependency(%q<archive-tar-minitar>, ["~> 0.5.1"])
      s.add_development_dependency(%q<hoe>, [">= 2.5.0"])
    else
      s.add_dependency(%q<rubyforge>, [">= 2.0.3"])
      s.add_dependency(%q<gemcutter>, [">= 0.3.0"])
      s.add_dependency(%q<archive-tar-minitar>, ["~> 0.5.1"])
      s.add_dependency(%q<hoe>, [">= 2.5.0"])
    end
  else
    s.add_dependency(%q<rubyforge>, [">= 2.0.3"])
    s.add_dependency(%q<gemcutter>, [">= 0.3.0"])
    s.add_dependency(%q<archive-tar-minitar>, ["~> 0.5.1"])
    s.add_dependency(%q<hoe>, [">= 2.5.0"])
  end
end
