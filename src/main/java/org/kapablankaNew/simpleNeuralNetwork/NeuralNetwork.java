package org.kapablankaNew.simpleNeuralNetwork;

import java.util.*;

public class NeuralNetwork {
    private final List<Layer> layers;

    private final Topology topology;

    public NeuralNetwork(Topology topology){
        this.topology = topology;
        layers = new ArrayList<>();
        createInputLayer();
        createHiddenLayers();
        createOutputLayer();
    }

    public Layer getLayer(int index){
        return layers.get(index);
    }

    private void createInputLayer(){
        List<Neuron> inputNeurons = new ArrayList<>();
        //filling the layer with neurons
        for (int i = 0; i < topology.getInputCount(); i++){
            //input neuron always have 1 input
            Neuron neuron = new Neuron(1, NeuronType.Input);
            inputNeurons.add(neuron);
        }
        //creating layer ad adding this in list of layers
        Layer inputLayer = new Layer(inputNeurons, NeuronType.Input);
        layers.add(inputLayer);
    }

    private void createHiddenLayers(){
        for (int i = 0; i < topology.getHiddenLayers().size(); i++){
            List<Neuron> hiddenNeurons = new ArrayList<>();
            //getting last layer (its size is the number of inputs of each neuron in this layer layer)
            Layer lastLayer = layers.get(layers.size() - 1);
            //filling the layer with neurons
            for (int j = 0; j < topology.getCountOfNeuronsInLayer(i); j++){
                Neuron neuron = new Neuron(lastLayer.getCount());
                hiddenNeurons.add(neuron);
            }
            //creating layer ad adding this in list of layers
            Layer hiddenLayer = new Layer(hiddenNeurons);
            layers.add(hiddenLayer);
        }
    }

    private void createOutputLayer(){
        List<Neuron> outputNeurons = new ArrayList<>();
        //getting last layer (its size is the number of inputs of each neuron in the output layer)
        Layer lastLayer = layers.get(layers.size() - 1);
        //filling the layer with neurons
        for (int i = 0; i < topology.getOutputCount(); i++){
            Neuron neuron = new Neuron(lastLayer.getCount(), NeuronType.Output);
            outputNeurons.add(neuron);
        }
        //creating layer ad adding this in list of layers
        Layer outputLayer = new Layer(outputNeurons, NeuronType.Output);
        layers.add(outputLayer);
    }

    public List<Neuron> predict(List<Double> inputSignals){
        //for feed forward sending signals to input neurons
        sendSignalsToInputNeurons(inputSignals);
        //after this go through all the other layers
        feedForwardAllLayersAfterInput();
        //return list of output neurons
        return layers.get(layers.size() - 1).getNeurons();
    }

    private void sendSignalsToInputNeurons(List<Double> inputSignals){
        for (int i = 0; i < inputSignals.size(); i++){
            //each input neuron accepts only one input signal
            List<Double> signal = new ArrayList<>(Collections.singletonList(inputSignals.get(i)));
            //getting neuron
            Neuron neuron = layers.get(0).getNeuron(i);
            //sending a signal to neuron
            neuron.feedForward(signal);
        }
    }

    private void feedForwardAllLayersAfterInput(){
        for(int i = 1; i < layers.size(); i++){
            Layer layer = layers.get(i);
            //getting list of outputs signals with previous layer
            List<Double> previousLayerSignals = layers.get(i - 1).getOutputSignals();
            for (Neuron neuron : layer.getNeurons()){
                //sending input signals to neuron
                neuron.feedForward(previousLayerSignals);
            }
        }
    }

    //method for the correction of weights
    public void learnBackPropagation(DataSet dataSet, int numberOfSteps){
        for (int i = 0; i < numberOfSteps; i++){
            //going trough dataset
            for (int j = 0; j < dataSet.getSize(); j++){
                //getting lists of input signal and expected results from dataset
                List<Double> inputs = dataSet.getInputSignals(j);
                List<Double> expectedResults = dataSet.getExpectedResult(j);
                //first - calculate result
                //second - calculate errors
                //third - correct weights
                this.predict(inputs);
                this.calculateErrors(expectedResults);
                this.updateWeights(topology.getLearningRate());
            }
        }
    }

    private void calculateErrors(List<Double> expectedResults){
        //errors calculate from output layer to input
        //because errors in this layer depend on the errors of the next layer
        for (int i = layers.size() - 1; i >= 0; i--){
            //get layer
            Layer currentLayer = layers.get(i);
            for (int j = 0; j < currentLayer.getNeurons().size(); j++){
                //for output layer pass a expected result
                if (currentLayer.getLayerType() == NeuronType.Output){
                    currentLayer.getNeuron(j).calculateError(expectedResults.get(j));
                } else {
                    //for another layers - pass a next layer for calculating the error
                    currentLayer.getNeuron(j).calculateError(layers.get(i + 1), j);
                }
            }
        }
    }

    private void updateWeights(double learningRate){
        //weights update from output layer to input
        for (int i = layers.size() - 1; i >= 0; i--){
            //getting layer
            Layer currentLayer = layers.get(i);
            for (int j = 0; j < currentLayer.getNeurons().size(); j++){
                //update weights of all neurons except input
                if (currentLayer.getLayerType() != NeuronType.Input){
                    currentLayer.getNeuron(j).learnBackPropagation(learningRate);
                }
            }
        }
    }
}