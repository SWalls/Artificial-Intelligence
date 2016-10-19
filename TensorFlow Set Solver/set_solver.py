from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten, Convolution2D, MaxPooling2D
from keras.optimizers import Adam
import numpy as np
from db_functions import *
np.random.seed(42)

def create_convnn_model():
    model = Sequential()

    # First convolutional layer
    model.add(Convolution2D(32, 5, 5, border_mode='valid', input_shape=(400, 300, 3)))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))

    # Second convolutional layer
    model.add(Convolution2D(64, 5, 5))
    model.add(Activation('relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))

    model.add(Flatten())
    model.add(Dense(1024))
    model.add(Activation('relu'))
    model.add(Dropout(0.5))

    model.add(Dense(9))
    model.add(Activation('softmax'))

    model.compile(loss='categorical_crossentropy', optimizer="adam", metrics=['accuracy'])

    return model

shapes = [Shape.SQUIGGLE]
numbers = [Number.TWO]
colors = [Color.GREEN, Color.PURPLE, Color.RED]
patterns = [Pattern.EMPTY, Pattern.FILLED, Pattern.STRIPES]

db = CardDatabase("cards.h5")
X_train, y_train = db.load_data(Type.TRAIN, shapes, numbers, colors, patterns)
X_test, y_test = db.load_data(Type.VALIDATION, shapes, numbers, colors, patterns)
model = create_convnn_model()
minibatch_size = 32
model.fit(X_train, y_train,
            batch_size=minibatch_size,
            nb_epoch=100,
            validation_data=(X_test, y_test),
            verbose=1)