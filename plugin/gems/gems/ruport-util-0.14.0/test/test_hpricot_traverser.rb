require 'test/helper'       
require 'ruport/util/graph/amline'    
Ruport.quiet { testcase_requires 'hpricot' }

describe "A simple traversal" do

  before :each do
    xml = "<foo><bar><baz></baz></bar></foo>"
    @root = Hpricot(xml)
    @traverser = Amline::HpricotTraverser.new(@root)
  end

  it "should allow accessing nested attributes" do
    @root.at("foo").at("bar").at("baz").should ==
    @traverser.foo.bar.baz.root
  end

  it "should allow setting nested attributes" do
    @traverser.foo.bar.baz = "kittens"
    @root.at("foo/bar/baz").innerHTML.should == "kittens"
  end

  it "should be a true bro about missing elements" do

      lambda { @traverser.foo.bart.baz }.
        should raise_error(Amline::UnknownOption)

      lambda { @traverser.foo.bart = "baz" }.
        should raise_error(Amline::UnknownOption)
      
      lambda { @traverser.foot }.
        should raise_error(Amline::UnknownOption)    
        
  end

  it "should allow accessing the Hpricot elements via !" do
   @traverser.foo!().class.should ==  Hpricot::Elem
  end

  it "should convert values to strings when setting tags" do
    @traverser.foo.bar.baz = 10
    @traverser.foo.bar.baz!().innerHTML.should == "10"
  end

end
