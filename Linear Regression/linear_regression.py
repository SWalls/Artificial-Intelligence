import matplotlib.pyplot as plt
import numpy as np
import math
np.random.seed(422) # Get the same random numbers every time

class LinearRegression:

    def __init__(self, csv_file, variable_cols, learning_rate):
        ### Load the dataset
        self.variables = []
        self.data = np.genfromtxt(csv_file, delimiter=',', skip_header=1)[:10]
        for col in variable_cols:
            self.variables.append(self.data[:, col])
        ### How do we change the weight and the bias to make the line's fit better?
        self.learning_rate = learning_rate

    def learn(self):
        y = self.variables[0] # djia
        x = self.variables[1] # temperature
        ### Init the model parameters
        weight = np.random.randint(math.ceil(y[0]/x[0]), size=1)[0]
        bias = np.random.randint(math.ceil(y[0]-x[0]), size=1)[0]
        print "Initial weight: %f, Initial bias: %f" % (weight, bias)
        cost = 2
        while cost > 1:
            error = (x*weight+bias) - y
            # print error
            cost = np.sum(np.power(error, 2))
            # print cost
            weight -= np.sum(self.learning_rate * error * x / len(x))
            bias -= np.sum(self.learning_rate * error * 1.0 / len(x))
            # print "New weight: %f, New bias: %f" % (weight, bias)
        print "FINAL weight: %f, FINAL bias: %f" % (weight, bias)
        ### Graph the dataset along with the line defined by the model
        xs = np.arange(0, 5)
        ys = xs * weight + bias
        plt.plot(x, y, 'r+', xs, ys, 'g-')
        plt.show()

model = LinearRegression(csv_file="djia_temp.csv", variable_cols=[1,2], learning_rate=0.1)
model.learn()