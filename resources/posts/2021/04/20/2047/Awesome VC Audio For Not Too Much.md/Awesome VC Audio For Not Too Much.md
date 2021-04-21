Covid. Ugh, you know? Right?
Covidtide has been a lot of things, but one more fun aspect of has been the opportunity it's given all us audio nerds to show our faces to each other. You may know us by the boom mics dipping into our frame, but those of us in the cult can often identify each other just by the sound of each others' voices.
The quality of your voice makes a big difference to your presence in the meeting for anyone, though, not just hobbyists. With a bad setup you could be inaudible, overwhelmed by background noise, or just sound cheap. With a good setup, you might even sound [smarter and more authoritative](https://tips.ariyh.com/p/good-sound-quality-smarter).
So it's worth investing some money into your wfh audio setup. You can invest a lot of money and still get ineffective results, though. I see a lot of setups that clearly were not cheap, but fail to deliver great audio quality and presence.
![][20210419_105323]
You don't have to spend a lot of money to sound like a professional broadcaster, though. That's what my setup sounds like, according to my colleagues. I started with what I had lying around, and slowly iterated until I found something I'm confident recommending to anyone. It's simple and easy to use. It's not free, but it won't break the bank either.
I'll start by showing you my recommended setup, and then talk about the rationale afterward.

### TL;DR: BOM and How-To

Here's what you'll need to buy (I'll assume you already have headphones):

* $150 --- Yamaha AG03 3-channel USB mixer
* $99 --- Shure SM58 Cardioid Dynamic Mic (or other similar dynamic mic)
* $20 --- XLR microphone cable
* $99 --- Rode PSA1 Desk-Mounted Broadcast Microphone Boom Arm (or comparable)

After tax, you should be out less than $400.
Here's how to set it up:

1. Plug the microphone into the mixer. The plug is in the upper left hand corner.
2. Hook up the mixer to the computer (it should just plug up and go). Turn it on by pressing the power button on top (right next to "AG03 MIXING CONSOLE")
3. Press the "COMP/EQ" button to turn it on.
4. Switch the sound input and output on your computer and VC software to use the mixer.
5. Do a quick sound check:
    * Turn up the knob marked "GAIN" all the way up. Slide the fader next to it all the way down.
    * While looking at the red light next to the Gain knob labeled "PEAK", speak into the microphone as loud as you expect to be speaking. Get up close to the microphone for this; imagine you're excited about something.
    * While speaking like this, slowly dial down the knob until the red PEAK light stops blinking. (You want this knob to go as high as it can go without making that red light blink.)
    * Turn up the fader on the left to the bold line.
6. Now, with your headphones on, turn up the "monitor" knob. This will adjust the sound of your own voice. Make sure that you can hear yourself clearly at a comfortable volume level.
7. The knob in the middle at the bottom above the computer is the volume level of your computer audio. Adjust this to whatever is comfortable for you --- it's a volume knob, after all.

And that's it. You should be good to go. You can enable reverb for fun by pressing the "EFFECT" button. You can tweak the settings for the onboard effects by downloading Yamaha's software and adjusting it from your computer.

### Why This Setup Works

There are a lot of appealing cheap options for getting a "real" microphone setup hooked up to your computer, but in my experience a great sound requires a few things that get overlooked in other setups. I tried to work around them myself, and found that the alternatives didn't achieve the same effect or didn't win me simplicity/desk space/$$$ like I thought they would.
Here's why this setup works for me and why I recommend it:

* It doesn't take up much desk space.
* The microphone has the right sound.
* It includes a compressor so that you can be expressive.
* You can hear your sound in your earphones.
* It's the cheapest and least finnicky way to achieve all of the above.

So let's go over the recommendations and how they help make these things happen.

### Broadcaster Boom Stand

If you've tried it both ways, you'll know that this is a no-brainer. Desk mic stands are cheap; some mics like the Blue Yeti even have them built in. But where are you supposed to put them? Usually they want to go right where your keyboard is.
Just get a desk-mounted broadcaster boom mic stand. It's more expensive than some cheaper alternatives, but the right solution is always more pleasant than trying to hack around the wrong solution.

### The Sound: SM58 Cardioid Dynamic Mic

If you know anything about microphones, the SM58 is going to jump out as a boring choice. It's basically an SM57 with a pop filter on it.
Well, you'll need a pop filter. Have you ever heard a loud noise on a microphone from someone saying the "P" consonant? Pop filters stop that from happening. So you'll want one.
But why the SM57? It's near the bottom end of what you'd spend for a "professional" microphone, but nowadays there are a lot of other microphones at that price point. Condensor microphones used to be expensive, but now you can get a good one for the price of an SM57. So why not go for the condensor?
Well, a couple of reasons. First, the sort of condensors used for vocals are larger. They often don't have built-in pop filters, either. You'll either have to pay for a screen pop filter [like this](https://www.sweetwater.com/store/detail/ROKITPOP--gator-rok-it-pop-filter) or put on the bulky foam one they usually ship with. The Blue Yeti has a built in pop filter, but again --- it's pretty big.
But more importantly, the SM57 has the right sound.
Here's a picture for you:
![][marcmaron-obama-sm7]
This is Marc Maron interviewing President Barack Obama for his interview podcast, WTF. Check out those microphones.
Those microphones are SM-7s. They're one of the industry standards for broadcasting applications like this. They're also a notable vocal microphone --- Michael Jackson famously recorded all his vocals on them. Once you know what they look like, you'll see them everywhere.  
You probably don't want an SM-7. The microphone itself is $400, and requires at least $100 of additional preamps to sound decent. It's overkill for office work.
But the SM-7 is nothing but a beefed up SM57/SM58. They don't sound the same, but for $100 the 58 will get you in the right ballpark for your budget.

### Yamaha AG03 Mixer

![][20210419_131458]
This is the most specific recommendation on the list: I haven't found anything else on the market that suits. So: why this?
There are two major reasons: compression, and monitoring.

### Compression: Why You Want It

Compression is an audio effect that tries to bring up quiet sounds so that they're audible, while leaving the loud sounds alone.
If you're not an audio nerd, then you may never have heard of compression and why it's used. But if you've listened to music or radio or podcasts, you've heard this sound before. Almost every single recorded voice you've ever heard is compressed. Google Meet and Zoom compress your voice, too. So does the telephone. And in the real world, your mind constantly applies a psychosomatic compression to the sounds you're interested in.
Why do they do that? I don't have the background or the space to give a thorough answer to that question, but one simple answer is that it makes it so that you can be heard whether you're whispering, yelling, or somewhere in between. Usually we're somewhere in between. Without compression, either that "in between" is going to sound quiet, or "loud" will be downright painful.
A professional broadcaster with an actual budget will likely have a standalone hardware compressor that costs a few thousand dollars. We'll get by just fine with the AG03's built-in compressor.

### Why Not Software Compression?

Now, your computer can do a great job on compression for free. Reaper and Garage Band both have built-in compressors which work just fine. Rogue Amoeba's Audio Hijack also has great vocal processing tools. Why not use those?
I did exactly that in an earlier iteration of my setup. It works, but there are a few problems with it.

* If you want monitoring (and you really do --- see below), you need to install and configure some additional software to do routing on your machine. Every OS conforms to the stereotypes on this matter: it's expensive and easy on MacOS ($100 for Rogue Amoeba's Loopback), supremely annoying and subject to occasional breakage on Windows, and lol who knows on Linux.
* Software monitoring has a noticeable bit of latency, maybe 30ms or so. Latency is not good: it means that between the time you speak into the mic and the time you hear it, there's a bit of lag. Suffice to say that it's a problem you'd rather not have if you have the option.
* Something has to be running on your computer all the time to keep your audio rig going. It might occasionally need to be kicked in the shins to keep it in running order, too. This is fine if you're running a recording studio, but if your computer is primarily for work, this software will just be taking up space.
* It's a pain to switch to a different computer with a setup like this, because you have to install and configure the entire audio rig.

Even with all that, it might be worth it if it saved money. But the AG-03 is $150; a simple audio interface is $100, and Loopback is $100. So the AG-03 has none of the drawbacks above, and it's cheaper.

### Monitoring

![][howardstern-headphones]
Here's another setup. I'm not a big Howard Stern guy, but I wanted to show his setup to illustrate a couple of things:

* He uses a condensor mic! Maybe I'm wrong about the SM58. Do what works for you! Maybe you sound like Howard Stern!
* He has headphones on!

So did Marc Maron, of course. But unlike the SM-7 (which Stern doesn't use), you will _never_ see anyone speaking into a microphone as a professional who isn't using headphones or speakers.
Stern does interviews in a studio, a controlled environment, so he could choose to do interviews without headphones to put his subjects more at ease. But he doesn't, because without headphones you can't _monitor_ the sound you're making on air. That's what monitoring is: taking the sound going out to your audience, and putting it in your own ears.
Monitoring is a big deal. I don't think it's an exaggeration to say that it is the single biggest improvement you can make to your VC experience. That it's not commonplace is criminal.
Without monitoring, speaking on a video conference is like talking to someone through a window. How well can they hear what you're saying? Who knows? You can't hear what they're hearing.
That's what monitoring gives you. Doing this allows you to use the microphone and the compression you've applied to make the sound you intend to make, rather than guess what you sounded like and how audible you were.
You'll be surprised how much monitoring will open your eyes to the sound you're actually making. You might find yourself speaking more quietly. Most people will subconsciously raise the volume of their voice on a VC without monitoring. I know I did: prior to getting monitoring setup, my roommate regularly told me how loud I was when I was in a meeting. Now I'm the one who complains about him.
As I mentioned above, monitoring is a pain to setup in software, and yields imperfect results. This cheap $150 mixer will solve the problem perfectly, with 0ms of latency, and without any complicated software setup.

### Doing It Slightly Cheaper

If you want to save money on the above, you probably can. SM58s are as common and as indestructible as quartz, and are easily found at usable quality on the secondhand market for $70 or less. There are comparable equivalent mics, too --- just look for cardioid dynamic microphones with built in pop filters.
I wouldn't worry about getting a nice cable, either --- you're paying for durability with cables, and this one is likely to sit in one place most of its life.
There's no easy sub for the Yamaha AG03, unfortunately. It provides an audio interface and a hardware compressor in a small form factor. There's no cheaper equivalent on the market that does that.

[20210419_105323]: 20210419_105323.jpg width=312px height=234px

[marcmaron-obama-sm7]: marcmaron-obama-sm7.jpg width=357px height=201px

[20210419_131458]: 20210419_131458.jpg width=309px height=232px

[howardstern-headphones]: howardstern-headphones.jpg width=328px height=218px
