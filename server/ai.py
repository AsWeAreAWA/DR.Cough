#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Source: http://pybrain.org/docs/tutorial/fnn.html
from pybrain.datasets import ClassificationDataSet,SequenceClassificationDataSet
from pybrain.utilities import percentError
from pybrain.tools.shortcuts import buildNetwork
from pybrain.supervised.trainers import BackpropTrainer
from pybrain.structure.modules import SoftmaxLayer

# Only needed for data generation and graphical output
from pylab import ion, ioff, figure, draw, contourf, clf, show, plot
from scipy import diag, arange, meshgrid, where
from numpy.random import multivariate_normal
from random import normalvariate


def generate_data(n=400):
    INPUT_FEATURES = 2
    CLASSES = 3
    means = [(-1, 0), (2, 4), (3, 1)]
    cov = [diag([1, 1]), diag([0.5, 1.2]), diag([1.5, 0.7])]
    alldata = ClassificationDataSet(INPUT_FEATURES, 1, nb_classes=CLASSES)
    minX, maxX = means[0][0], means[0][0]
    minY, maxY = means[0][1], means[0][1]
    for i in range(n):
        for klass in range(CLASSES):
            features = multivariate_normal(means[klass], cov[klass])
            #print [klass]
            x, y = features
            minX, maxX = min(minX, x), max(maxX, x)
            minY, maxY = min(minY, y), max(maxY, y)
            alldata.addSample(features, [klass])
    return {'minX': minX, 'maxX': maxX,
            'minY': minY, 'maxY': maxY, 'd': alldata}



def load_data():
    f = open("data.in","r").readlines()
    alldata = ClassificationDataSet(41,1,nb_classes=10)
    minX, maxX = 0,1
    minY, maxY = 0,1
    for line in f:
        line = line.split(" ")
        rez = int(line[0])
        y = [0] * 10
        y[rez] = 1
        X = [int(x) for x in line[1:]]
        for i in range(35):
            alldata.addSample(X,[rez])
    return {'minX': minX, 'maxX': maxX,
            'minY': minY, 'maxY': maxY, 'd': alldata}    

def perceptron(hidden_neurons=5, weightdecay=0.01, momentum=0.1):
    INPUT_FEATURES = 41
    CLASSES = 10
    HIDDEN_NEURONS = hidden_neurons
    WEIGHTDECAY = weightdecay
    MOMENTUM = momentum

    # Generate the labeled set
    g = load_data()

    #g = generate_data2()
    alldata = g['d']
    minX, maxX, minY, maxY = g['minX'], g['maxX'], g['minY'], g['maxY']

    # Split data into test and training dataset
    tstdata, trndata = alldata.splitWithProportion(0.25)

    trndata._convertToOneOfMany()  # This is necessary, but I don't know why
    tstdata._convertToOneOfMany()  # http://stackoverflow.com/q/8154674/562769

    print("Number of training patterns: %i" % len(trndata))
    print("Input and output dimensions: %i, %i" % (trndata.indim,
                                                   trndata.outdim))
    print("Hidden neurons: %i" % HIDDEN_NEURONS)
    print("First sample (input, target, class):")

    #print(trndata['input'][0], trndata['target'][0], trndata['class'])

    fnn = buildNetwork(trndata.indim, HIDDEN_NEURONS, trndata.outdim,bias=True)
                       #outclass=SoftmaxLayer)

    trainer = BackpropTrainer(fnn, dataset=trndata, momentum=MOMENTUM,
                              verbose=True, weightdecay=WEIGHTDECAY)

     # Visualization

    for i in range(20):
        trainer.trainEpochs(10)
        trnresult = percentError(trainer.testOnClassData(),
                                 trndata['class'])
        tstresult = percentError(trainer.testOnClassData(
                                 dataset=tstdata), tstdata['class'])

        print("epoch: %4d" % trainer.totalepochs,
              "  train error: %5.2f%%" % trnresult,
              "  test error: %5.2f%%" % tstresult)


        test = [0]*41
        test[0],test[3],test[40],test[39],test[38],test[37],test[36] = 1,1,1,1,1,1,1
        print fnn.activate(test)

    return fnn
    """
    # Visualization
    ticksX = arange(minX-1, maxX+1, 0.2)
    ticksY = arange(minY-1, maxY+1, 0.2)
    X, Y = meshgrid(ticksX, ticksY)

    # need column vectors in dataset, not arrays
    griddata = ClassificationDataSet(INPUT_FEATURES, 1, nb_classes=CLASSES)
    for i in range(X.size):
        griddata.addSample([X.ravel()[i], Y.ravel()[i]], [0])

    for i in range(20):
        trainer.trainEpochs(1)
        trnresult = percentError(trainer.testOnClassData(),
                                 trndata['class'])
        tstresult = percentError(trainer.testOnClassData(
                                 dataset=tstdata), tstdata['class'])

        print("epoch: %4d" % trainer.totalepochs,
              "  train error: %5.2f%%" % trnresult,
              "  test error: %5.2f%%" % tstresult)
        out = fnn.activateOnDataset(griddata)
        # the highest output activation gives the class
        out = out.argmax(axis=1)
        out = out.reshape(X.shape)

        figure(1)  # always print on the same canvas
        ioff()  # interactive graphics off
        clf()   # clear the plot
        for c in [0, 1, 2]:
            here, _ = where(tstdata['class'] == c)
            plot(tstdata['input'][here, 0], tstdata['input'][here, 1], 'o')
        if out.max() != out.min():  # safety check against flat field
            contourf(X, Y, out)   # plot the contour
        ion()  # interactive graphics on
        draw()  # update the plot

    ioff()
    show()
    """


def calc():
    from argparse import ArgumentParser, ArgumentDefaultsHelpFormatter

    parser = ArgumentParser(formatter_class=ArgumentDefaultsHelpFormatter)

    # Add more options if you like
    parser.add_argument("-H", metavar="H", type=int, dest="hidden_neurons",
                        default=5,
                        help="number of neurons in the hidden layer")
    parser.add_argument("-d", metavar="W", type=float, dest="weightdecay",
                        default=0.01,
                        help="weightdecay")
    parser.add_argument("-m", metavar="M", type=float, dest="momentum",
                        default=0.1,
                        help="momentum")
    args = parser.parse_args()

    return perceptron(args.hidden_neurons, args.weightdecay, args.momentum)


