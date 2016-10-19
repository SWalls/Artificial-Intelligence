from matplotlib import pyplot as plt
import h5py
import numpy as np

class Type:
    TRAIN = "train"
    VALIDATION = "validation"

class Shape:
    SQUIGGLE = "squiggle"
    OVAL = "oval"
    DIAMOND = "diamond"

class Number:
    ONE = "one"
    TWO = "two"
    THREE = "three"

class Color:
    GREEN = "green"
    PURPLE = "purple"
    RED = "red"

class Pattern:
    EMPTY = "empty"
    FILLED = "filled"
    STRIPES = "stripes"

# Number of Set cards to consider. 81 = full deck
NUM_LABELS = 9

def convertToOneHot(index, size):
    row = np.zeros(size)
    row[index % size] = 0.9
    return row

def normalize(X):
    X = X.astype('float')
    maxes = X.max(axis=0)
    print maxes.shape
    for j in range(len(maxes)):
        for i in range(3):
            minval = maxes[...,i].min()
            maxval = maxes[...,i].max()
            if minval != maxval:
                maxes[...,i] -= minval
                maxes[...,i] *= (255.0/(maxval-minval))
    X *= 1/maxes

class CardDatabase:
    
    def __init__(self, db_filename):
        self.db_filename = db_filename

    def load_visibility_data(self):
        X_train = [] # data
        y_train = [] # labels
        X_test = [] # data
        y_test = [] # labels
        with h5py.File(self.db_filename,'r') as hf:
            train_visible = hf.get("train/visible")
            for image in train_visible.items():
                X_train.append(np.array(image[1][()]))
                y_train.append(1) # visible
            train_invisible = hf.get("train/invisible")
            for image in train_invisible.items():
                X_train.append(np.array(image[1][()]))
                y_train.append(0) # not visible
            test_visible = hf.get("validation/visible")
            for image in test_visible.items():
                X_test.append(np.array(image[1][()]))
                y_test.append(1) # visible
            test_invisible = hf.get("validation/invisible")
            for image in test_invisible.items():
                X_test.append(np.array(image[1][()]))
                y_test.append(0) # not visible
        X_train = np.array(X_train)
        y_train = np.array(y_train)
        X_test = np.array(X_test)
        y_test = np.array(y_test)
        return X_train, y_train, X_test, y_test

    def load_card_data(self, type, shapes, numbers, colors, patterns):
        X = [] # data
        y_raw = [] # labels
        with h5py.File(self.db_filename,'r') as hf:
            data_group = hf.get(type)
            for shape in shapes:
                shape_group = data_group.get(shape)
                for number in numbers:
                    number_group = shape_group.get(number)
                    for color in colors:
                        color_group = number_group.get(color)
                        for pattern in patterns:
                            pattern_group = color_group.get(pattern)
                            label = CardDatabase.get_label(shape, number, color, pattern)
                            for image in pattern_group.items():
                                X.append(np.array(image[1][()]))
                                y_raw.append(label)
        X = np.array(X)
        # normalize(X)
        # Convert y to correct data structure with rows of labels
        y = []
        for label in y_raw:
            y.append(convertToOneHot(label, NUM_LABELS))
        y = np.array(y)
        return X, y

    @staticmethod
    def get_label(shape, number, color, pattern):
        label = 0
        # shape
        if(shape == Shape.SQUIGGLE): label += 0
        elif(shape == Shape.OVAL): label += 27
        elif(shape == Shape.DIAMOND): label += 54
        # number
        if(number == Number.ONE): label += 0
        elif(number == Number.TWO): label += 9
        elif(number == Number.THREE): label += 18
        # color
        if(color == Color.GREEN): label += 0
        elif(color == Color.PURPLE): label += 3
        elif(color == Color.RED): label += 6
        # pattern
        if(pattern == Pattern.EMPTY): label += 0
        elif(pattern == Pattern.FILLED): label += 1
        elif(pattern == Pattern.STRIPES): label += 2
        return label

    @staticmethod
    def get_description(label):
        shape = Shape.SQUIGGLE
        number = Number.ONE
        color = Color.GREEN
        pattern = Pattern.EMPTY
        while(label >= 27):
            if(shape == Shape.SQUIGGLE): shape = Shape.OVAL
            elif(shape == Shape.OVAL): shape = Shape.DIAMOND
            label -= 27
        while(label >= 9):
            if(number == Number.ONE): number = Number.TWO
            elif(number == Number.TWO): number = Number.THREE
            label -= 9
        while(label >= 3):
            if(color == Color.GREEN): color = Color.PURPLE
            elif(color == Color.PURPLE): color = Color.RED
            label -= 3
        while(label >= 1):
            if(pattern == Pattern.EMPTY): pattern = Pattern.FILLED
            elif(pattern == Pattern.FILLED): pattern = Pattern.STRIPES
            label -= 1
        return [shape, number, color, pattern]

    def show_image(self, type, shape, number, color, pattern, image_index):
        with h5py.File(self.db_filename,'r') as hf:
            group = hf.get(type+"/"+shape+"/"+number+"/"+color+"/"+pattern)
            image_arr = np.array(group.get("img-%d"%image_index))
            plt.imshow(image_arr)
            plt.show()

if __name__ == "__main__":
    # do unit tests
    label = CardDatabase.get_label(Shape.SQUIGGLE, Number.ONE, Color.GREEN, Pattern.EMPTY)
    print label, CardDatabase.get_description(label)
    label = CardDatabase.get_label(Shape.SQUIGGLE, Number.TWO, Color.PURPLE, Pattern.FILLED)
    print label, CardDatabase.get_description(label)
    label = CardDatabase.get_label(Shape.DIAMOND, Number.THREE, Color.RED, Pattern.STRIPES)
    print label, CardDatabase.get_description(label)
    label = CardDatabase.get_label(Shape.OVAL, Number.TWO, Color.GREEN, Pattern.FILLED)
    print label, CardDatabase.get_description(label)