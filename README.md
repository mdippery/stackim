**stack.im** is a URL shortener for Stack Overflow profiles. It lets you turn this:

    stackoverflow.com/users/00000/me

into this:

    stack.im/me

You're looking at the source code for the app that powers this service.

# That's it?

Yep.

# That's stupid. Why not just use bit.ly?

You could, but I think it's cool to have a Stack Overflow-specific shortener. `stack.im/me` just looks cooler than `http://bit.ly/12cjLbv`.

# How do I create a URL?

We're all programmers here, so just use `curl`:

    $ curl -X PUT -d "stackid=00000" http://stack.im/me

(That is, make a PUT request to the shortened URL you desire with your Stack Overflow user ID as a parameter.)

# Are you Jeff Atwood or Joel Spolsky in disguise?

No, nor is **stack.im** affiliated in any way with Stack Overflow, its founders, or its creators. I just made it for fun (and because I wanted a short URL for my Stack Overflow to put on my résumé, bumper stickers, tattoos, and the inside of my underwear).
