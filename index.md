# Abstract
Voxel-based soft robots (VSRs) are a type of modular robots composed by interconnected soft and deformable blocks, i.e., voxels. Thanks to the softness of their bodies, VSRs may exhibit rich dynamic behaviors. One open question is what type of neural controller is most suitable for a given morphology and sensory apparatus in a given environment. One observation is that artificial neural networks with state may be able to cope with the dynamical nature of VSR bodies and their morphological computation. In this work, we consider four types of controllers, i.e., multilayer perceptrons (MLPs, stateless), recurrent neural networks (RNNs), spiking neural networks (SNNs) without homeostasis, and SNNs with homeostasis. We consider three robot morphologies tested for locomotion, where each morphology is investigated in simulation with three different types and number of sensors. Neural network controllers are optimized with neuroevolution, and the experimental results are compared in terms of effectiveness, efficiency, and generalization ability. In addition, we analyze the resulting behavior of the robots systematically. Our results show that RNNs are typically more effective while MLPs are often the weakest controllers, particularly for robots with few sensors. However, SNNs are more capable in terms of generalization and the mechanism of homeostasis is often beneficial. Finally, we show that RNNs and SNNs with homeostasis produce a more wide variety of behaviors.

[Paper info and full text](https://giorgia-nadizar.github.io/#/publications/nadizar2023experimental)


# Videos

## Biped
<video width="600" height="450" controls>
  <source src="https://github.com/giorgia-nadizar/NeuralModelsVSR/raw/gh-pages/videos/biped.mov" type="video/mp4">
Your browser does not support the video tag.
</video>

## Worm
<video width="600" height="450" controls>
  <source src="https://github.com/giorgia-nadizar/NeuralModelsVSR/raw/gh-pages/videos/worm.mov" type="video/mp4">
Your browser does not support the video tag.
</video>

## Comb
<video width="600" height="450" controls>
  <source src="https://github.com/giorgia-nadizar/NeuralModelsVSR/raw/gh-pages/videos/comb.mov" type="video/mp4">
Your browser does not support the video tag.
</video>
