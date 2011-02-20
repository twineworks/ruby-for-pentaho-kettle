module Hello

  GREETINGS = ["Hello!", "Hi!", "Hi there", "Guten Tag", "Good morning", "Bom dia", "Buenos dias", "Nice to see you!", "Bonjour", "Namaste"]

  def Hello.say
    Hello::GREETINGS.shuffle.first
  end

end