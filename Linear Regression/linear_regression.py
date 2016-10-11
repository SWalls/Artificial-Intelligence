import matplotlib.pyplot as plt
import numpy as np
import math
np.random.seed(42) # Get the same random numbers every time

def normalize(data):
    minIdx = 0
    maxIdx = 0
    for i in range(0, len(data)):
        if(data[i] > data[maxIdx]):
            maxIdx = i
        elif(data[i] < data[minIdx]):
            minIdx = i
    min = data[minIdx]
    max = data[maxIdx]
    diff = max - min
    for i in range(0, len(data)):
        data[i] = (data[i]-min) / diff
    return data

class LinearRegression:

    def __init__(self, csv_file, dependent_var_index, independent_vars_indexes, learning_rate):
        ### Load the dataset
        self.data = np.genfromtxt(csv_file, delimiter=',', skip_header=1)[:10]
        data_column = self.data[:, dependent_var_index]
        self.dependentVariable = normalize(data_column)
        first = True
        for col in independent_vars_indexes:
            data_column = self.data[:, col]
            if(first):
                self.independentVariables = [normalize(data_column)]
                first = False
            else:
                self.independentVariables = np.vstack((self.independentVariables, normalize(data_column)))
        print self.independentVariables
        ### How do we change the weight and the bias to make the line's fit better?
        self.learning_rate = learning_rate

    def learn(self):
        y = self.dependentVariable # djia
        x = self.independentVariables # temperature, etc
        ### Init the model parameters
        weights = np.random.rand(len(x))
        bias = np.random.rand(1)[0]
        print "Initial Weights:"
        print weights
        print "Initial Bias: %f" % (bias)
        error = (weights.dot(x)+bias) - y
        cost = np.sum(np.power(error, 2))
        print "Initial Cost: %f" % cost
        for i in range(0, 100):
            error = (weights.dot(x)+bias) - y
            # print "Error: %f" % error
            cost = np.sum(np.power(error, 2))
            # print "Cost: %f" % cost
            for j in range(len(x)):
                weights[j] = np.sum(-1.0 * self.learning_rate * error * x[j] / len(x[j]))
            bias -= np.sum(self.learning_rate * error * 1.0 / len(x))
            # print "New weights:"
            # print weights
            # print "New bias: %f" % bias
        print "FINAL Cost: %f" % (cost)
        print "FINAL Weights:"
        print weights
        print "FINAL Bias: %f" % (bias)
        ### Graph the dataset along with the line defined by the model
        xs = np.arange(0, 5)
        ys = xs*weights[0] + bias
        plt.plot(x[0], y, 'r+', xs, ys, 'g-')
        plt.show()

model = LinearRegression("djia_temp.csv", 1, [2], 0.001)
model.learn()