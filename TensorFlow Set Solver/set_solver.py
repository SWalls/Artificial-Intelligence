from keras.models import Sequential
from keras.layers import Dense
from scipy import misc
import numpy as np
numpy.random.seed(42)

def load_image(filename):
    return misc.imread(filename)

