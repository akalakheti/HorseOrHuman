# HorseOrHuman
This is an android application that uses deep learning to classify whether a given image is of human or of horse. It uses the model sitting on Firebase that was trained from 'horse_or_man.ipynb' file.

## Files:
Horse_or_man.ipynb: This is the python notebook file where we write the code to train our Horse and human classifier. It also converts the trained .h5 model to .tflite format.

model.h5: This is the trained model obtained from Horse_or_man.ipynb.

model.tflite: It is a converted model for mobile version from model.h5 file.


When we use Firebase in our application, we could provide the model for appication remotely. We could also update the model and the user doesn't have to update his/her application.

### The model requires image of the size 128x128x3.

Video link: https://drive.google.com/open?id=1CIDrzk_4FN0p_5R8huXa6aD-hYuJyUMz
