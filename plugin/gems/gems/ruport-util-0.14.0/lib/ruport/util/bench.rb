# A strange little benchmarking utility that uses Ruport
# to output the results of benchmark suites. This is primarily an internal
# developers tool for Ruport, but you might find it interesting or useful.
#
# Example:
#
#  require "ruport"  
#  require "ruport/util/bench"
#  include Ruport::Bench
#
#  class MyFormat < Ruport::Formatter;
#     renders :nothing, :for => Ruport::Renderer::Row
#  end
#
#  record = Ruport::Data::Record.new [1,2,3]
#
#  bench_suite do
#    N = 10000   
#    bench_case("as(:nothing)",N) { record.as(:nothing) }
#    bench_case("to_nothing",N) { record.to_nothing }
#  end 
# 
#  # output
#
#  Running Bench Suite...
#  > as(:nothing)... ok[2.12905] 
#  > to_nothing... ok[2.00278] 
#  +--------------------------------------+
#  |     name     | iterations | realtime |
#  +--------------------------------------+
#  | as(:nothing) |      10000 | 2.12905  |
#  | to_nothing   |      10000 | 2.00278  |
#  +--------------------------------------+
#  Suite run time: 4.13183  
#
module Ruport::Bench
  
  require "benchmark"  

  def bench_case(name,n,&block)
    2.times { 
      @run = 0
      n.times do
        @bench_prepare.call if @bench_prepare
        @run += Benchmark.realtime { block.call } 
      end
    }     
    @bench_results << { "name" => name, "iterations" => n, 
                        "realtime" => (time = sprintf("%.5f",@run)) } 
    STDERR.puts "> #{name}... ok[#{time}] "
    @bench_prepare = nil
  end      

  def bench_prepare(&block)
    @bench_prepare = block
  end
  
  def bench_suite(&block)
    STDERR.puts "Running Bench Suite..."
    @bench_results = Table(%w[name iterations realtime]) 
    block.call   
    puts @bench_results   
    puts "Suite run time: " << sprintf("%.5f",@bench_results.sigma("realtime"))
  end  
  
end
