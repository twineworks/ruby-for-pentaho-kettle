#!/usr/bin/env ruby -w 
require 'test/helper'    
testcase_requires 'dbi'

$VERBOSE = nil
   
describe "A Query" do

   before :each do
     @sources = {
       :default => {
         :dsn => 'ruport:test',  :user => 'greg',   :password => 'apple' },
       :alternative => {
         :dsn => "ruport:test2", :user => "sandal", :password => "harmonix" },
     }
     Ruport::Query.add_source :default,     @sources[:default]
     Ruport::Query.add_source :alternative, @sources[:alternative]
 
     @columns = %w(a b c)
     @data = [ [[1,2,3],[4,5,6],[7,8,9]],
               [[9,8,7],[6,5,4],[3,2,1]],
               [[7,8,9],[4,5,6],[1,2,3]], ]
     @datasets = @data.dup
 
     @sql = [ "select * from foo", "create table foo ..." ]
     @sql << @sql.values_at(0, 0).join(";\n")
     @sql << @sql.values_at(1, 0).join(";\n")
     @query = {
      :plain      => Ruport::Query.new(@sql[0]),
      :sourced    => Ruport::Query.new(@sql[0], :source        => :alternative),
      :paramed    => Ruport::Query.new(@sql[0], :params        => [ 42 ]),
      :paramed_ar => Ruport::Query.new([@sql[0], 69, 777]),
      :raw        => Ruport::Query.new(@sql[0], :row_type      => :raw),
      :resultless => Ruport::Query.new(@sql[1]),
      :multi      => Ruport::Query.new(@sql[2]),
      :mixed      => Ruport::Query.new(@sql[3]),
     }
   end

   if Object.const_defined? :DBI
 
     it "should have a nil result on execute" do
       query = @query[:plain]
       setup_mock_dbi(1)
 
       query.execute.should == nil
     end
   
     it "should allow execute to work with sources" do
        query = @query[:sourced]
        setup_mock_dbi(1, :source => :alternative)
      
        query.execute.should == nil
      end
   
      it "should allow execute to accept parameters" do
        query = @query[:paramed]
        setup_mock_dbi(1, :params => [ 42 ])

        query.execute.should == nil   
      end  
   
      it "should allow execute to accept parameters (ActiveRecord style)" do
        query = @query[:paramed_ar]
        setup_mock_dbi(1, :params => [ 69, 777 ])

        query.execute.should == nil   
      end  
   
      it "should return nil for empty results" do
        query = @query[:resultless]
        setup_mock_dbi(1, :resultless => true, :sql => @sql[1])

        query.result.should be_nil  
      end      
   
      it "should return last query result for multiple statements" do
        query = @query[:multi]
        setup_mock_dbi(2)
      
        get_raw(query.result).should == @data[1] 
      end
   
      it "should allow raw mode" do
        query = @query[:raw]
        setup_mock_dbi(1)
        
        query.result.should == @data[0]
      end  
   
      it "should allow reading from file with .sql extension" do
       File.should_receive(:read).
         with("query_test.sql").
         and_return("select * from foo\n")
       
       query = Ruport::Query.new "query_test.sql"
       query.sql.should == "select * from foo" 
     end      
                 
      it "should allow reading from file with explicit :file argument" do
        File.should_receive(:read).
        with("query_test").
        and_return("select * from foo\n")  
      
        query = Ruport::Query.new(:file => "query_test")
        query.sql.should == "select * from foo" 
        
        query = Ruport::Query.new(:string => "query_test")
        query.sql.should == "query_test"       
      end
   
      it "should raise a LoadError if the file is not found" do
        File.should_receive(:read).
          with("query_test.sql").
          and_raise(Errno::ENOENT)
      
        lambda { query = Ruport::Query.new "query_test.sql" }.
          should raise_error(LoadError)
      end
   
      it "should support an each() iterator" do
        query = @query[:plain]
        setup_mock_dbi(2)
      
        result = []; query.each { |r| result << r.to_a }
        result.should == @data[0]
                     
        result = []; query.each { |r| result << r.to_a }
        result.should == @data[1]
      end  

      it "should iterate the last data set if multiple queries" do
        query = @query[:multi]
        setup_mock_dbi(2)
      
        result = []; query.each { |r| result << r.to_a }
        result.should == @data[1]
      end
   
      it "should raise a LocalJumpError if a block is not given" do
        lambda { @query[:plain].each }.should raise_error(LocalJumpError)
      end
   
      it "should allow selecting sources" do
        query = @query[:plain]
        query.select_source :alternative
        get_query_source(query).should == @sources[:alternative]
      
        query.select_source :default
        get_query_source(query).should == @sources[:default] 
      end
 
      it "should initialize a temporary source" do
        query = Ruport::Query.new "<unused>", @sources[:alternative]
        get_query_source(query).should == @sources[:alternative]
      end
 
      it "should initialize multiple temporary source" do
        query1 = Ruport::Query.new "<unused>", @sources[:default]
        query2 = Ruport::Query.new "<unused>", @sources[:alternative]
      
        get_query_source(query1).should == @sources[:default]
        get_query_source(query2).should == @sources[:alternative] 
      end  
 
      it "should allow conversion to table" do
        query = @query[:raw]
        setup_mock_dbi(3, :returns => [@data[0]])
      
        query.result.should == @data[0]
        query.to_table.should == @data[0].to_table(@columns)
        query.result.should == @data[0]
      end
   
      it "should support conversion to csv" do
        query = @query[:plain]
        setup_mock_dbi(1)
        
        csv = @data[0].to_table(@columns).as(:csv)
        query.to_csv.should == csv
      end
     
      it "should raise error when DSN is missing" do
        lambda {
          Ruport::Query.add_source :foo, :user => "root", :password => "fff"
        }.should raise_error(ArgumentError)
      end


      it "should be able to set new defaults" do
        Ruport::Query.add_source :default, :dsn      => "dbi:mysql:test",
                                           :user     => "root",
                                           :password => ""
        Ruport::Query.default_source.dsn.should == "dbi:mysql:test"
        Ruport::Query.default_source.user.should == "root" 
        Ruport::Query.default_source.password.should == "" 
      end

      it "should allow setting multiple sources" do
        Ruport::Query.add_source :foo, :dsn => "dbi:mysql:test"
        Ruport::Query.add_source :bar, :dsn => "dbi:mysql:test2"
        Ruport::Query.sources[:foo].dsn.should == "dbi:mysql:test"  
        Ruport::Query.sources[:bar].dsn.should == "dbi:mysql:test2" 
      end

    end
     
   private
   def setup_mock_dbi(count, options={})  
     sql = options[:sql] || @sql[0]
     source = options[:source] || :default
     resultless = options[:resultless]
     params = options[:params] || []
     
     @dbh = mock("database_handle")
     @sth = mock("statement_handle")
     def @dbh.execute(*a, &b); execute__(*a, &b); ensure; sth__.finish if b; end
     def @sth.each; data__.each { |x| yield(x.dup) }; end
     def @sth.fetch_all; data__; end
     
     DBI.should_receive(:connect).exactly(count).times.
       with(*@sources[source].values_at(:dsn, :user, :password)).
       and_yield(@dbh)
     c = @dbh.should_receive(:execute__).exactly(count).times.with(sql, *params)
     c.and_yield(@sth)
     c.and_return(@sth)
     @dbh.stub!(:sth__).and_return(@sth)
     @sth.should_receive(:finish).with().exactly(count).times
     unless resultless
       @sth.stub!(:fetchable?).and_return(true) 
       @sth.stub!(:column_names).and_return(@columns) 
       if options[:returns]        
         if Array == options[:returns]       
            @sth.should_receive(:data__).any_number_of_times.and_return(*options[:returns])  
         else        
           @sth.should_receive(:data__).any_number_of_times.and_return(*Array(options[:returns])) 
         end
       else
         @sth.should_receive(:data__).any_number_of_times.and_return(*@datasets)
       end
     else
       @sth.stub!(:fetchable?).and_return(false)
       @sth.stub!(:column_names).and_return([])
       @sth.stub!(:cancel)
       @sth.should_receive(:data__).exactly(0).times
     end                
   end
   
   def get_query_source(query)
     [ :dsn, :user, :password ].inject({}) do |memo, var|
       memo.update var => query.instance_variable_get("@#{var}")
     end
   end
 
   def get_raw(table)
     table.map { |row| row.to_a }
   end
end
