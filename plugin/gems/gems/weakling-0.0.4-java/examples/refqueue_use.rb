require 'weakling'
require 'java'

q = WeakRef::RefQueue.new
wr = WeakRef.new(Object.new, q)
puts "weakref object: #{wr.__id__}"

puts "running GC"
java.lang.System.gc

puts "weakref alive?: #{wr.weakref_alive?}"
puts "weakref object from queue: #{q.poll.__id__}"

