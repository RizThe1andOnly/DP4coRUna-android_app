# DP4coRUna Android
Current Major Changes include:
- Creation of packages and seperation of classes/files.<br>
Now if a variable is declared "protected" it can't be accessed from a file/class in a different package, either it has to become "public" or public getter has to be created.

- LocationObject extends SensorReader which extends LocationGrabber. This means each SensorReader has access to LocationGrabber functions/variables (non-private ones) and LocationObject has access to SensorReader and LocationGrabber stuff. From now on use LocationObject instead of just SensorReader or LocationGrabber. 