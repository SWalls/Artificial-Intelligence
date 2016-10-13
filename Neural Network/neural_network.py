import matplotlib.pyplot as plt
import numpy as np
np.random.seed(42)
        
def sigmoid(x):
    return 1 / (1 + np.exp(-x))

def dsigmoid(x):
    return x * (1 - x)

class NetworkLayer(object):

    def forward(self, X):
        return X

    def backward(self, err):
        return err

class DenseNetworkLayer(NetworkLayer):

    def __init__(self, size, next_layer_size, learning_rate):
        self.weights = np.random.rand(size, next_layer_size)
        self.learning_rate = learning_rate

    def forward(self, X):
        self.incoming = X
        act = X.dot(self.weights)
        return act

    def backward(self, err):
        update = self.incoming.T.dot(err)
        corrected = err.dot(self.weights.T)
        self.weights += self.learning_rate * update
        return corrected

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
        return outgoing

    def iteration(self, i, X, y):
        pred = self.propogateForward(X)
        deriv_err = self.calculateDerivError(y, pred)
        change = self.propogateBackward(deriv_err)
        self.reportAccuracy(i, y, pred)
        return change

    def reportAccuracy(self, i, y, pred):
        pred = np.round(pred)
        count = np.count_nonzero(y - pred)
        correct = len(pred) - count
        print "Epoch %d: %.4f%% correct" % (i, float(correct)*100.0 / len(pred))

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
    X, y = loadDataset('breast_cancer.csv', 50)
    print "\nX-norm:"
    print X
    print "\nY:"
    print y
    print "\nX-shape: %s, Y-shape: %s" % (X.shape, y.shape)
    model = NeuralNetwork()
    model.addLayer(DenseNetworkLayer(9,9,0.01))
    model.addLayer(SigmoidNetworkLayer())
    model.addLayer(DenseNetworkLayer(9,1,0.01))
    model.addLayer(SigmoidNetworkLayer())
    model.checkGradient(X, y)
    model.train(X, y, 10)
