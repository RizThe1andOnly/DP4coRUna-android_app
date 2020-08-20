# DP4coRUna Android-App

App designed to obtain data on corona infections and advise users on what locations to are safe or not.

Currently in process of developing skeletons of tools that will be used towards the ultimate goal. Tools being developed at the moment include:
- Means by which to detcect where a user is currently:
    - Utilization of google apis to detect what building user is in
    - Utilize sensor data to obtain room level data; what room user is in inside of building.
        - Current sensor data gathered include: light, sound, geomagnetic field strength, cell tower information, wifi access point rssi values

- Building network over which data can be transferred across app users for verification of data and other collaborations. Also methods of transferring data across this network.

- Machine Learning models that will be trained to detect room features by collaborating devices when verification requests come through the network.
    - Right now using multinomial logistic regression (softmax) for features
    - Utilizing DeepLearning4J machine learning libraries to accomplish this at the moment.
    


## Upcoming Demo:
Will feature the integration of the above three functionalities.

The App will be able to obtain location data, send it over the network to be verified, and run machine learning model to obtain probabilities on which label the features (from learned data) may belong to.