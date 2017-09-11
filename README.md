# Ruby scripting for [pentaho-kettle](https://github.com/pentaho/pentaho-kettle)

[![Kettle 5.x](https://img.shields.io/badge/pentaho_kettle-5.x--7.x-4c7e9f.svg)](https://github.com/pentaho/pentaho-kettle)
[![Java 7+](https://img.shields.io/badge/java-7+-4c7e9f.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-LGPL2.1-4c7e9f.svg)](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/LICENSE.txt)
[![Travis Build Status](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle.svg?branch=master)](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle)
[![Appveyor Build status](https://ci.appveyor.com/api/projects/status/qd422po9spre0men/branch/master?svg=true)](https://ci.appveyor.com/project/slawo-ch/ruby-for-pentaho-kettle/branch/master)


This project provides a scripting step similar to the javascript and java class steps. The implementation is based on [JRuby](http://jruby.org). Thanks to JRuby's great Java interop, the scripting step also enables easy Java scripting in kettle.
## Supported versions of pentaho-kettle
The plugin is built and tested against the most recent versions of Kettle 5.x, 6.x and 7.x.

## How to get it?
Grab the latest release from the [releases](https://github.com/twineworks/ruby-for-pentaho-kettle/releases) page.

## How to install?
Decompress the release zip to `<kettle-dir>/plugins` and restart Spoon. The "Ruby Script" step will appear in the "Scripting" section of a transformation.

## How do I write ruby scripts in Kettle?
The ruby scripting step comes with a lot of samples. You can access them by opening a ruby step dialog and exploring the samples section on the left.
![Samples](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/images/screenshot.png)

## Features at a glance
 - rows are represented as hashes, indexed by field name
 - automatic conversion between all kettle data types and native ruby types
 - scripts have access to rows from info steps
 - scripts can send rows selectively to target steps
 - scripts may redirect rows to an error stream by using kettle's error handling feature
 - a script tab may be declared a **start script**, which executes only once before the first row arrives, useful for init tasks
 - a script tab may be declared an **end script**, which executes only after all incoming rows have been processed, useful for cleanup and summary tasks
 - a script tab may be declared a **lib script**, which can be imported by any other script tab when required
 - steps with no input can be used as row generators
 - Kettle step `$step` and transformation `$trans` objects are available in ruby scope for advanced scripting
 - you may call your favorite Java libraries from the ruby script
 - you may use ruby gems in Kettle transformations

## Where do I report bugs and issues?
Just open [issues](https://github.com/twineworks/ruby-for-pentaho-kettle/issues) on github.

## What about Ruby Gems?
You can use Ruby Gems, absolutely. As long as [JRuby](https://github.com/jruby/jruby) likes the gem, which usually means that the gem it has no unsupported native bindings, you may use gems as with any other ruby program. You can define where your `gem_home` is on a per-step basis or globally.

By default the plugin uses `<plugin-dir>/gems` as its gems directory. Here's an example for maintaining your gems at the
default location:

```bash
# go to your plugin installation directory
$ cd data-integration/plugins/ruby-for-kettle-<version>

# install bundler into 'gems'
$ java -jar lib/jruby-complete*.jar -S gem install -i gems --no-rdoc --no-ri bundler
Fetching: bundler-1.15.4.gem (100%)
Successfully installed bundler-1.15.4
1 gem installed

# create a simple Gemfile
$ echo "source 'http://rubygems.org'" > Gemfile
$ echo "gem 'chronic' # https://github.com/mojombo/chronic" >> Gemfile
 $ cat Gemfile
source 'http://rubygems.org'
gem 'chronic' # https://github.com/mojombo/chronic

# ask bundler to install gems into 'gems'
$ PATH="gems/bin:$PATH" GEM_HOME="gems" GEM_PATH="gems" java -jar lib/jruby-complete*.jar -S bundle
Fetching gem metadata from http://rubygems.org/.................
Fetching version metadata from http://rubygems.org/..
Using bundler 1.15.4
Fetching chronic 0.10.2
Installing chronic 0.10.2
Bundle complete! 1 Gemfile dependency, 2 gems now installed.
Use `bundle info [gemname]` to see where a bundled gem is installed.
```

Once the gems are in place, you can require it in your scripts like usual:

```ruby
require 'chronic'

{
	"date_guess" => Chronic::parse("tomorrow afternoon")
}
```


## How do I build the project?
```bash
mvn package
```
It creates the plugin zip in `target/ruby-for-pentaho-kettle-{version}-plugin.zip`.

## How do I run the test suite?
Create a package, then run the tests. The packaging process unzips the plugin into `target/ruby-for-pentaho-kettle-{version}`, which
enables kettle to find the plugin when running integration tests.
```bash
mvn package
mvn -DskipTests=false test
```

## How can I contribute?
If you'd like to contribute please fork the project, add the feature or bugfix and send a pull request. If your change majorly alters the way the plugin works, we should discuss it via an open issue first.

## License
The ruby-for-pentaho-kettle plugin uses the [LGPL 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) license.

## Support
Open source does not mean you're on your own. The ruby-for-pentaho-kettle plugin is developed by [Twineworks GmbH](http://twineworks.com). Twineworks offers commercial support and consulting services. [Contact us](mailto:hi@twineworks.com) if you'd like us to help with a project.
