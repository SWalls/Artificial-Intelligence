from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten, Convolution2D, MaxPooling2D
from keras.optimizers import Adam
from db_functions import *
import numpy as np
# np.random.seed(42)
import tensorflow as tf
tf.python.control_flow_ops = tf

class CardRecognitionNN:

    def __init__(self, db):
        self.db = db

    def create_convnn_model(self):
        model = Sequential()

        # First convolutional layer
        model.add(Convolution2D(32, 10, 10, border_mode='valid', input_shape=(100, 75, 3)))
        model.add(Activation('relu'))
        model.add(MaxPooling2D(pool_size=(2, 2)))

        # Second convolutional layer
        model.add(Convolution2D(64, 10, 10))
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

    def verify(self, X, y):
        print "Verifying random data entry..."
        idx = np.random.randint(0, len(y))
        print "y[%d] =" % idx,
        print y[idx]
        label = np.where(y[idx]==0.9)[0] + 9
        print "label = %d," % label,
        print "description =",
        print CardDatabase.get_description(label)
        image_arr = X[idx]
        plt.imshow(image_arr)
        plt.show()
        # normalize(X)
        # image_arr = X[idx]
        # plt.imshow(image_arr)
        # plt.show()

    def train(self):
        shapes = [Shape.SQUIGGLE]
        numbers = [Number.TWO]
        colors = [Color.GREEN, Color.PURPLE, Color.RED]
        patterns = [Pattern.EMPTY, Pattern.FILLED, Pattern.STRIPES]

        X_train, y_train = self.db.load_card_data(Type.TRAIN, shapes, numbers, colors, patterns)
        X_test, y_test = self.db.load_card_data(Type.VALIDATION, shapes, numbers, colors, patterns)
        print "X-shape: %s, y-shape: %s" % (X_train.shape, y_train.shape)
        self.verify(X_train, y_train)

        # model = create_convnn_model()
        # minibatch_size = 32
        # model.fit(X_train, y_train,
        #             batch_size=minibatch_size,
        #             nb_epoch=100,
        #             validation_data=(X_test, y_test),
        #             verbose=1)

class CardVisibilityNN:

    def __init__(self, db):
        self.db = db

    def create_nn_model(self):
        1

    def train(self):
        X_train, y_train, X_test, y_test = self.db.load_visibility_data()
        print "X-shape: %s, y-shape: %s" % (X_train.shape, y_train.shape)

        # model = create_nn_model()
        # minibatch_size = 32
        # model.fit(X_train, y_train,
        #             batch_size=minibatch_size,
        #             nb_epoch=100,
        #             validation_data=(X_test, y_test),
        #             verbose=1)

db = CardDatabase("cards.h5")
recognition_nn = CardRecognitionNN(db)
recognition_nn.train()
visibility_nn = CardVisibilityNN(db)
visibility_nn.train()