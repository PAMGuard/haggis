# Detector for the Scottish Wild Haggis

The [Scottish Wild Haggis (*Haggis Scoticus*)](https://haggiswildlifefoundation.com/what-is-wild-haggis/) may or may not be an entirely mythical beast native to the Scottish Highlands. 

The primary purpose of this PAMGuard plugin module is therefore to demonstrate how to build detectors as plugin modules for PAMGuard. 

The detector itself is a simple energy sum detector, that will search for increases in energy above a user defined threshold. This is then followed by 
a rather silly classifier that generates some random numbers, modifies them a bit based on where the data are being collected, and a few other things, then 
makes a random classification to one of two (also entirely fictional) sub-species. 

All of this has been done in order to use as many PAMGuard features as possibe, including graphics overlays, data filters, symbol managers, a Tethys output module, etc. 

We hope that you don't waste too much time actually searching for Haggis in the Scottish Highlands (though we do recommend visiting the area to enjoy the wonderful scenery). 

We hope that you **do** find this example code useful, particularly if you're setting out on the journey of building your own PAMGuard module. 

Next year, should we be able to obtain sufficient high quality and accurately verified training data, we hope to build an AI classifier for the megafauna of Loch Ness.

Happy PAMGuarding

The PAMGuard Team. 
