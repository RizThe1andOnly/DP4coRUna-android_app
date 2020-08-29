# DP4coRUna Android-App

App designed to obtain data on corona infections and advise users on what locations to are safe or not.

** Check Documentation Folder for some documentation on the project so far **

Currently in process of developing skeletons of tools that will be used towards the ultimate goal. Tools being developed at the moment include:
- Means by which to detcect where a user is currently:
    - Utilization of google apis to detect what building user is in
    - Utilize sensor data to obtain room level data; what room user is in inside of building.
        - Current sensor data gathered include: light, sound, geomagnetic field strength, cell tower information, wifi access point rssi values

- Building network over which data can be transferred across app users for verification of data and other collaborations. Also methods of transferring data across this network.

- Machine Learning models that will be trained to detect room features by collaborating devices when verification requests come through the network.
    - Right now using multinomial logistic regression (softmax) for features
    - Utilizing DeepLearning4J machine learning libraries to accomplish this at the moment.
    

##What we have so far:
(Asterisks  with number on them represent things that need to be addressed further. See what needs to be done section)

- We can sample sensors for the following: light, geomagnetic field strength, wifi access point rrsi values and mac address, cell tower info (*1), sound level of room (*2).

- Refer to Network documentation separate from this file. (*3)

- Do softmax (multinomial logistic regression). (*4)

## What needs to be done:
(Asterisk + #) corresponds to above

- Implement means of detecting movement and transition from being in motion to being stationary. Then upon this event, compare rssi value before and after the motion through algo described in CollabLoc paper.

- (*1) returns  a list of cell tower id based on "number of radio devices in android device". Should investigate this further and decide which information the app needs. For now the 1st item in the list is being used.

- (*2) Sound level recordings are not reliable as is, they will require further processing. The sampled sound level is a relative number based on device, so this value will mean different things for different devices and is not consistent. One way to solve this may be to get a value as a percentage of the device's max recording capabilities.

- (*3) Further work to be done with networking are addressed in network doc.

- (*4) Requires a method using the built in functionalities of DeepLearning4J to get a train and test set then run the model. Currently there is no splitting of data. Also a method needs to be implemented that matches label names with the outputs provides and returns that data.

- Also currently the different components are not connected based on a cohesive model. The implementation in progress is using services to detect movement and based on that call wifi access point scanners and carryout logic with them. Then use the network services to transfer data through network and then have receiving devices to run already trained ML model.
    
