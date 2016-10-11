import matplotlib.pyplot as plt
import numpy as np
np.random.seed(42)

class NetworkLayer(object):

    def __init__(self, size, next_layer_size, learning_rate):
        self.size = size
        self.weights = np.random.rand(size, next_layer_size)
        self.learning_rate = learning_rate

    def thresholdFunction(self, x):
        return x

    def thresholdFunctionDeriv(self, x):
        return x

    def forward(self, X):
        self.incoming = X
        act = X.dot(self.weights)
        act = self.thresholdFunction(act)
        self.outputs = act
        return act

    def backward(self, err):
        err = err * self.thresholdFunctionDeriv(self.outputs)
        update = self.incoming.T.dot(err)
        self.weights += self.learning_rate * update
        return update

    def reportAccuracy(self, X, y):
        out = self.forward(X)
        out = np.round(out)
        count = np.count_nonzero(y - out)
        correct = len(X) - count
        print "%.4f" % (float(correct)*100.0 / len(X))

    def calculateDerivError(self, y, pred):
        return 2*(y - pred)

    def calculateError(self, y, pred):
        return (np.sum(np.power((y - pred), 2)))

class SigmoidNetworkLayer(NetworkLayer):

    def thresholdFunction(self, x):
        return 1 / (1 + np.exp(-x))

    def thresholdFunctionDeriv(self, x):
        return x * (1 - x)

class NeuralNetwork:

    def __init__(self, layer_sizes, learning_rate):
        self.layers = []
        nextLayerSize = 1
        for i in range(len(layer_sizes)-1, -1, -1):
            self.layers.insert(0, SigmoidNetworkLayer(layer_sizes[i], nextLayerSize, learning_rate))
            nextLayerSize = layer_sizes[i]

    def iteration(self, X, y):
        incoming = X
        for layer in self.layers:
            out = layer.forward(incoming)
            incoming = out
        err = self.layers[-1].calculateError(y, incoming)
        print err
        deriv_err = self.layers[-1].calculateDerivError(y, incoming)
        for layer in self.layers:
            layer.backward(deriv_err)

    def train(self, X, y, epochs):
        for i in range(0, epochs):
            self.iteration(X, y)

    def gradientChecker(model, X, y):
        epsilon = 1E-5

        model.weights[1] += epsilon
        out1 = model.forward(X)
        err1 = model.calculateError(y, out1)

        model.weights[1] -= 2*epsilon
        out2 = model.forward(X)
        err2 = model.calculateError(y, out2)

        numeric = (err2 - err1) / (2*epsilon)
        print numeric

        model.weights[1] += epsilon
        out3 = model.forward(X)
        err3 = model.calculateDerivError(y, out3)
        derivs = model.backward(err3)
        print derivs[1]

def loadDataset(filename='breast_cancer.csv'):
    my_data = np.genfromtxt(filename, delimiter=',', skip_header=1)

    # The labels of the cases
    # Raw labels are either 4 (cancer) or 2 (no cancer)
    # Normalize these classes to 0/1
    y = (my_data[:, 10] / 2) - 1

    # Case features
    X = my_data[:, :10]

    # Normalize the features to (0, 1)
    X_norm = X / X.max(axis=0)

    return X_norm, y

if __name__=="__main__":
    X, y = loadDataset()
    # X = X
    print X
    print y
    print X.shape, y.shape
    model = NeuralNetwork([10], 0.05)
    # gradientChecker(model, X, y)
    model.train(X, y, 100)
