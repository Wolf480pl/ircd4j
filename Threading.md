Threading
---------

* Netty always calls us from at most one thread per connection at any given time
* We must process IRC messages from a particular connection strictly in order
* but we don't need to respond to a message before we start processing the next one
  which means we can have a long pipeline where every stage can be in a separate thread
* Messages affecting particular user's state (nick, joined channels, etc.) need not to come
  from her connection - she can be kicked, killed, force-renamed, etc. remotely,
  but these need to be somehow put in order between her own messages, when we send her
  feedback about her state, so these messages
  *must be processed in the same thread that messages the user sends*
* As a result, all write access to a particular User object happens from the same thread
* Messages not affecting user's state (i.e. those notifying him about others' state/actions)
  can be sent to the user directly from other threads

In any of the above, "the same thread" still allows for moving the task between threads
as long as it's handled by at most one thread at the same time, like in Netty 5
