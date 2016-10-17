import matplotlib.pyplot as plt
import numpy as np
from math import floor
# np.random.seed(42)
        
def sigmoid(x):
    return 1 / (1 + np.exp(-x))

def dsigmoid(x):
    return x * (1 - x)

class NetworkLayer(object):

    def forward(self, X):
        return X

    def backward(self, err):
        return err

    def reset(self):
        return

class DenseNetworkLayer(NetworkLayer):

    def __init__(self, size, next_layer_size, learning_rate):
        self.size = size
        self.next_layer_size = next_layer_size
        self.weights = np.random.rand(size, next_layer_size)
        self.learning_rate = learning_rate

    def forward(self, X):
        self.incoming = X
        act = X.dot(self.weights)
        return act

    def backward(self, err):
        self.update = self.incoming.T.dot(err)
        corrected = err.dot(self.weights.T)
        self.weights += self.learning_rate * self.update
        return corrected

    def reset(self):
        self.weights = np.random.rand(self.size, self.next_layer_size)

class SigmoidNetworkLayer(NetworkLayer):

    def forward(self, X):
        act = sigmoid(X)
        self.outputs = act
        return act

    def backward(self, err):
        err = (err * dsigmoid(self.outputs))
        return err

class NeuralNetwork:

    def __init__(self):
        self.layers = []

    def reset(self):
        for layer in self.layers:
            layer.reset()
    
    def addLayer(self, layer):
        self.layers.append(layer)

    def propogateForward(self, X):
        # propogate forward
        incoming = X
        for i in range(0, len(self.layers)):
            incoming = self.layers[i].forward(incoming)
        return incoming

    def propogateBackward(self, err):
        # propogate backward
        # iterate in reverse order
        outgoing = err
        for i in range(len(self.layers)-1, -1, -1):
            outgoing = self.layers[i].backward(outgoing)
        return self.layers[0].update

    def iteration(self, i, X, y):
        pred = self.propogateForward(X)
        # print "Error: %.4f" % self.calculateError(y, pred)
        deriv_err = self.calculateDerivError(y, pred)
        change = self.propogateBackward(deriv_err)
        # self.reportAccuracy("Epoch %d:" % i, y, pred)
        return change

    def reportAccuracy(self, i, y, pred):
        pred = np.round(pred)
        count = np.count_nonzero(y - pred)
        correct = len(pred) - count
        print "%s %.4f%% correct" % (i, float(correct)*100.0 / len(pred))

    def calculateDerivError(self, y, pred):
        return 2*(y - pred)

    def calculateError(self, y, pred):
        return (np.sum(np.power((y - pred), 2)))

    def train(self, X, y, epochs):
        for i in range(0, epochs):
            self.iteration(i, X, y)

    def checkGradient(self, X, y):
        epsilon = 1E-5
        wShape = self.layers[0].weights.shape
        twoDim = True if len(wShape) > 1 else False

        ### Adjust weight slightly to the right
        if twoDim: self.layers[0].weights[1][0] += epsilon
        else: self.layers[0].weights[1] += epsilon
        pred1 = self.propogateForward(X)
        err1 = self.calculateError(y, pred1)

        ### Adjust weight slightly to the left
        if twoDim: self.layers[0].weights[1][0] -= 2*epsilon
        else: self.layers[0].weights[1] -= 2*epsilon
        pred2 = self.propogateForward(X)
        err2 = self.calculateError(y, pred2)

        ### Calculate midpoint of errors
        print err1, err2
        numeric = (err2 - err1) / (2*epsilon)
        print "Error midpoint: %f" % numeric

        ### Calculate derivative of the real error to see if it matches
        # adjust weight back to normal
        if twoDim: self.layers[0].weights[1][0] += epsilon
        else: self.layers[0].weights[1] += epsilon
        change = self.iteration(0,X,y)

        if twoDim: print "Real derivative: %f" % change[1][0]
        else: print "Real derivative: %f" % change[1]

def loadDataset(filename='breast_cancer.csv', rows=0):
    if(rows == 0):
        my_data = np.genfromtxt(filename, delimiter=',', skip_header=1)
    else:
        my_data = np.genfromtxt(filename, delimiter=',', skip_header=1)[:rows]

    # The labels of the cases
    # Raw labels are either 4 (cancer) or 2 (no cancer)
    # Normalize these classes to 0/1
    y = (my_data[:, 10] / 2) - 1
    y = y.reshape((y.shape[0],1))

    # Case features
    X = my_data[:, 1:10]

    # Normalize the features to (0, 1)
    X_norm = X / X.max(axis=0)

    return X_norm, y

if __name__=="__main__":
    np.set_printoptions(precision=2)
    X, y = loadDataset()
    # print "\nX-norm:"
    # print X
    # print "\nY:"
    # print y
    print "\nX-shape: %s, Y-shape: %s" % (X.shape, y.shape)
    model = NeuralNetwork()
    model.addLayer(DenseNetworkLayer(9,9,0.001))
    model.addLayer(SigmoidNetworkLayer())
    model.addLayer(DenseNetworkLayer(9,1,0.001))
    model.addLayer(SigmoidNetworkLayer())
    model.checkGradient(X, y)
    # do k-fold test
    k = 10
    inc = int(floor(len(X) / k))
    for i in range(0, k):
        model.reset()
        # train ANN on k-1 equally sized subsets of the data
        for j in range(0, k-1):
            if j != i:
                model.train(X[j*inc:(j+1)*inc], y[j*inc:(j+1)*inc], 1000)
        # test the ANN's performance on the held out subset
        pred = model.propogateForward(X[i*inc:(i+1)*inc])
        model.reportAccuracy("k not include %d:" % i, y[i*inc:(i+1)*inc], pred)