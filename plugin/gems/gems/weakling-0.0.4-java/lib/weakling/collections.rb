require 'refqueue'

module Weakling
  class IdHash
    include Enumerable
    
    def initialize
      @hash = Hash.new
      @queue = Weakling::RefQueue.new
    end

    class IdWeakRef < Weakling::WeakRef
      attr_accessor :id
      def initialize(obj, queue)
        super(obj, queue)
        @id = obj.__id__
      end
    end

    def [](id)
      _cleanup
      if wr = @hash[id]
        return wr.get rescue nil
      end

      return nil
    end

    def add(object)
      _cleanup
      wr = IdWeakRef.new(object, @queue)

      @hash[wr.id] = wr

      return wr.id
    end

    def _cleanup
      while ref = @queue.poll
        @hash.delete(ref.id)
      end
    end

    def each
      @hash.each {|id, wr| obj = wr.get rescue nil; yield [id,obj] if obj}
    end
  end
end