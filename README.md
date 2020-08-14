# DP4coRUna Android-App

Current Objective: Integrate Local Learning, Network, and Machine Learning components.

- Local Learning will sample location data and sensor based features and create JSON object with data.

- Networking will take JSON and transmit to another device through socket connections.

- Machine Learning will try to match lable of location with labels in receiving devices and/or use Multinomial Logistic Regression to obtain set of probabilities on which existing location the received location features match with. **The probabilities or labels will be returned via the network.**
