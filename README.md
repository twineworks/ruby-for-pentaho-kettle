# Ruby-Scripting-for-Kettle brings Ruby scripting to the [PDI](http://kettle.pentaho.com) ETL tool, a.k.a. Kettle.

It provides a scripting transformation step similar to the JavaScript and User Defined Java Class steps. It is very simple to use, while also allowing complete access to every aspect of a Kettle transformation.  

The step is a work in progress, but it's reasonably stable now. Feel free to take a peek while I am working on the RC1. There are some very basic samples available from the samples tab in the plugin window. If you'd like a peek preview, here's how to install it:

## How to get it
Use the download button or checkout the source using the git url :)

## Installation
edit the build.properties file and edit kettle-dir to point to a Kettle 4.1 installation. The plugin will compile against Kettle libs from this installation. 

Execute the ant install target by typing: 
ant install

This will compile the plugin and copy it to plugins/steps/Ruby

Launch Spoon and find the ruby step in the "Scripting" section :)

## Features:

Rows are represented as a hashes, indexed by field name

Automatic conversion between all Kettle data types and native Ruby types

	Kettle Integer <-> Ruby Fixnum
	Kettle Number <-> Ruby Number
	Kettle BigNumber <-> Ruby BigDecimal
	Kettle Date <-> Ruby Time
	Kettle Binary <-> Ruby Array of Fixnum (Bytes)
	Kettle Serializable <-> Ruby Object 

Scripts provide the rows they generate as simple hashes 

Scripts may return nil to ignore rows and not produce any output

Scripts have access to rows from info steps

Scripts have access to rows from target steps

A script tab may be declared a **start script**, which executes only once before the first row arrives 

A script tab may be declared an **end script**, which executes only after all incoming rows have been processed 

A script tab may be declared a **lib script**, which is not executed at all unless it is imported by any other script

The step can be used as a row generator 

Internal step ($step) and transformation ($trans) Kettle objects are available in ruby scope for advanced scripting





