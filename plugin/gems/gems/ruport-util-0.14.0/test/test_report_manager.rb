require 'test/helper'
   
class MikeSample < Ruport::Report; def generate; "Hello Mike"; end; end
class JoeSample < Ruport::Report; def generate; "Hello Joe"; end; end

MikeSample.acts_as_managed_report
JoeSample.acts_as_managed_report  

class FakeModel2; end
class FakeModel3; end

describe 'ReportManager' do
  before :all do
    @manager = Ruport::ReportManager
  end

  it 'should handle reports' do
    @manager['MikeSample'].should == MikeSample
    @manager['JoeSample'].should == JoeSample

    [ MikeSample, JoeSample ].should == @manager.reports
  end

  it 'should add models' do
    # only for testing, no need for it in production code
    manager = @manager.dup

    manager.models.should be_empty

    manager.add_model FakeModel2
    manager.models.should have(1).model

    manager.add_model FakeModel3
    manager.models.should have(2).models

    # let's make sure we can still grab these by name (a string)
    [ FakeModel2, FakeModel2 ].each do |model|
      manager[model.name].should == model
    end
  end
end
