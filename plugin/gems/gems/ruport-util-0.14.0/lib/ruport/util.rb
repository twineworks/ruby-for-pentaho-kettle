module Ruport
  module Util
    VERSION = "0.14.0"

    file = __FILE__
    file = File.readlink(file) if File.symlink?(file)
    dir = File.dirname(file)
    BASEDIR = File.expand_path(File.join(dir, '..', '..'))
    LIBDIR = File.expand_path(File.join(dir, '..'))
  end
end

require "ruport/util/report"
require "ruport/util/graph"
require "ruport/util/invoice"
require "ruport/util/report_manager"
require "ruport/util/mailer"
require "ruport/util/bench"
require "ruport/util/generator"
require "ruport/util/pdf/form"
require "ruport/util/ods" 
require "ruport/util/query"
require "ruport/util/xls"
require "ruport/util/ods_table"
require "ruport/util/xls_table"
