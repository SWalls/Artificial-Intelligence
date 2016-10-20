from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten, Convolution2D, MaxPooling2D
from keras.optimizers import Adam
from db_functions import *
import os
import numpy as np
# np.random.seed(42)
import tensorflow as tf
tf.python.control_flow_ops = tf

def normalize(X):
    return X/255.

class ConvolutionalNN:

    def __init__(self, db):
        self.db = db

    def create_model(self, in_shape, out_size):
        model = Sequential()

        # First convolutional layer
        model.add(Convolution2D(32, 3, 3, border_mode='valid', input_shape=in_shape))
        model.add(Activation('relu'))
        # model.add(MaxPooling2D(pool_size=(2, 2)))

        # Second convolutional layer
        model.add(Convolution2D(64, 3, 3))
        model.add(Activation('relu'))
        # model.add(MaxPooling2D(pool_size=(2, 2)))

        model.add(Flatten())
        # model.add(Dense(1024))
        # model.add(Activation('relu'))
        model.add(Dropout(0.5))

        model.add(Dense(out_size))
        model.add(Activation('softmax'))

        model.compile(loss='categorical_crossentropy', optimizer="adam", metrics=['accuracy'])

        return model

class CardRecognitionNN(ConvolutionalNN):

    def verify(self, X, y):
        print "Verifying random data entry..."
        idx = np.random.randint(0, len(y))
        print "y[%d] =" % idx,
        print y[idx]
        label = np.where(y[idx]==0.9)[0] + 9
        print "label = %d," % label,
        print "description =",
        print CardDatabase.get_description(label)
        image_arr = X[idx].reshape(40, 30, 3)
        plt.imshow(image_arr)
        plt.show()

    def train(self):
        shapes = [Shape.SQUIGGLE]
        numbers = [Number.TWO]
        colors = [Color.GREEN, Color.PURPLE, Color.RED]
        patterns = [Pattern.EMPTY, Pattern.FILLED, Pattern.STRIPES]

        X_train, y_train = self.db.load_card_data(Type.TRAIN, shapes, numbers, colors, patterns)
        X_test, y_test = self.db.load_card_data(Type.VALIDATION, shapes, numbers, colors, patterns)
        print "Recognition... X-shape: %s, y-shape: %s" % (X_train.shape, y_train.shape)
        # self.verify(X_train, y_train)
        X_train = normalize(X_train)
        X_test = normalize(X_test)

        model = self.create_model((40, 30, 3), 9)
        model_filename = 'recognition_model_weights.h5'
        if(os.path.isfile(model_filename)):
            print "Loaded recognition weights from file!"
            model.load_weights(model_filename)
        else:
            minibatch_size = 32
            model.fit(X_train, y_train,
                        batch_size=minibatch_size,
                        nb_epoch=100,
                        validation_data=(X_test, y_test),
                        verbose=1)
            model.save_weights(model_filename)
        return model

class CardVisibilityNN(ConvolutionalNN):

    def train(self):
        X_train, y_train, X_test, y_test = self.db.load_visibility_data()
        print "Visibility... X-shape: %s, y-shape: %s" % (X_train.shape, y_train.shape)
        X_train = normalize(X_train)
        X_test = normalize(X_test)

        model = self.create_model((40, 30, 3), 2)
        model_filename = 'visibility_model_weights.h5'
        if(os.path.isfile(model_filename)):
            print "Loaded visibility weights from file!"
            model.load_weights(model_filename)
        else:
            minibatch_size = 32
            model.fit(X_train, y_train,
                        batch_size=minibatch_size,
                        nb_epoch=100,
                        validation_data=(X_test, y_test),
                        verbose=1)
            model.save_weights(model_filename)
        return model
