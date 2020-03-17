###Abstract

Ftc means 'flatten the curve', this project is born to address the current corona-crisis in providing an 
app that informs users when they may be at risk of being infected but does so with as much privacy as possible.
It is a replacement of the chinese government's [app dubbed 'close contact detector'](https://thenextweb.com/apps/2020/02/11/coronavirus-app-detector/). 

###Objective

FTC will not persist any positions of it's users but only use them in memory for a short period of time. It 
will not ask users for any private data, not even an email-address. And the algorithm will work just as well as an app 
that creates a complete movement-profile of it's users. Once installed on a phone, the FTC-app will run in background, 
regularly anonymously transmitting the users geospatial position. If the user had been tested positively on Covid19, he may upload
a copy of his test results to our servers and if it is deemed legit, the user is then being considered a corona case for the next 21 days. 
Everyone who gets 4 meters or closer to him and has the app installed will be warned and everyone who has the app installed and has been in 
close proximity with the user in the near past will get a notification that there is a chance he or she got infected. 

###TODO
FTC is in a very early stage, I'm just hoping to point out what the objectives are and what needs to be done. 
Right now, this repository contains a POC, or showcase if you will, of the server-side, that is neither tested nor finished.
The description of the server-API is a .proto-file [here](https://github.com/berndhopp/ftc/blob/master/src/main/proto/ftc.proto).

####Client

A client-app for iOS and Android needs to be created. I was thinking Xamarin, but then I never created an app and neither do I have the time or
the knowledge to do this, so I leave this to another volunteer. I'd prefer an native app, but Xamarin would do too, I just  worry about 
the binary size. This is what the app should do:

    1) create a random, secure UUID
    2) open a channel to the grpc server-endpoint ( you mave run the server on your maching by checking out this repo and running it as a spring-boot service locally )
    3) periodically send UUIDAndPosition with the created UUID and the current geo-position to the service ( see the proto-file )
    4) if the riskFactor returned by the service is > 0, inform the user with a notification that he may be at risk of covid19
    5) the frontend-part of the app should allow the user to send a mail to infections@ftcapp.org with a copy of his (positive) test results and 
       the UUID that was created in step 1. It may be a better option to create a separate endpoint for this instead of using email, but
       this way it's easier to implement. 
      
####Server

##### Hadoop/Spark

First and foremost the crucial algorithm should be ported to hadoop or spark in order to make the app 
scaleable, see [here](https://github.com/berndhopp/ftc/blob/master/src/main/java/org/ftc/server/engine/FtcEngineImpl.java).  
    
##### Heroku / Postgres

The PAAS of choice for me is Heroku and I will do some changes on the server to make it deployable to Heroku, which also means the database 
will need to switch from H2 to Postgres. 

####Marketing

This app is will not work unless it is being installed by a large portion of the population. "Population", does not necessarily mean
all of the country, but for example 60% of berliners need to have the app installed before it will be useful in Berlin. Being in a lockdown
and having decreased fluctuation in population will be helpful here also. 

I don't have a marketing-budget, so I was thinking about

    1) tell your friends
    2) print flyers and stickers with a QR-code to the app in the store, say why people should install it on the flyers/stickers
    3) if you know IT people, tell them and ask them if they may be able to help out with something ( programming, administration )
    4) if you're a politician or have connections to politics, an official government-endorsement would help us tremendously
     
 