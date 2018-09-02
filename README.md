# Tutorial on Machine Learning for Intelligent Mobile User Interfaces using TensorFlow
One key feature of TensorFlow includes the possibility to compile the trained model to run efficiently on mobile phones. This enables a wide range of opportunities for researchers and developers. In this tutorial, we teach attendees two basic steps to run neural networks on a mobile phone: Firstly, we will teach how to develop neural network architectures and train them in TensorFlow. Secondly, we show the process to run the trained models on a mobile phone.

We will mainly cover:
* [`01_Model-Training.ipynb`](https://github.com/interactionlab/imui-tutorial/blob/master/01_Model-Training.ipynb): In this notebook, we will train an artificial neural network to classify hand-written digits based on the MNIST dataset.
* [`02_Model-Export.ipynb`](https://github.com/interactionlab/imui-tutorial/blob/master/02_Model-Export.ipynb): This notebook loads the previously trained model and exports it as an .pb file. This enables the use of the model with TensorFlow Mobile (e.g. on Android).
* [`03_Model-Training-CNN.ipynb`](https://github.com/interactionlab/imui-tutorial/blob/master/03_Model-Training-CNN.ipynb): This notebook shows how to train a convolutional neural network based on the MNIST dataset (i.e. same as [`01_Model-Training.ipynb`](https://github.com/interactionlab/imui-tutorial/blob/master/01_Model-Training.ipynb), but with a CNN).


The compiled Android app to test the model trained in [`01_Model-Training.ipynb`](https://github.com/interactionlab/imui-tutorial/blob/master/01_Model-Training.ipynb) is available in the [Android Store](https://play.google.com/store/apps/details?id=io.interactionlab.tutorial_mobile_example).

<img src="./img/screenshot_download.png" height="100" /> <img src="./img/screenshot_detect.png" height="100" />

This tutorial will be/was held at:
* [MobileHCI'18, 3th September 2018, 09:00-17:30, Barcelona, Spain](https://mobilehci.acm.org/2018/2018/06/23/tutorials/#tut1)
* [PerDis'18, 6th June 2018, 10:00-17:00, Munich, Germany](http://pervasivedisplays.org/2018/program.html) 
* [MUM'17, 26th November 2017, 09:00-13:30, Stuttgart, Germany](http://www.mum-conf.org/2017/index.php?web=workshopsandtuts) - [Tutorial Website](https://interactionlab.io/imui-mum17/)
* [Mensch und Computer 2017, 10th September 2017, 11:00-15:30, Regensburg, Germany](http://muc2017.mensch-und-computer.de/programm/tutorials-mci/)
* [MobileHCI'17, 4th September 2017, 11:15-13:15, Vienna, Austria](https://mobilehci.acm.org/2017/tutorials-about.html) - [Tutorial Website](https://interactionlab.io/imui-mobilehci17/)

 
### Beyond the tutorial:
In this tutorial, we presented simple classification examples. Beyond that, we would like to also provide plug-and-play notebooks which we used for our published papers:
* (Regression) Estimating the Finger Orientation on Capacitive Touchscreens (ISS '17): https://github.com/interactionlab/Capacitive-Finger-Orientation-Estimation
* (Regression) Estimating Finger Position on Fully Touch Sensitive Smartphones (UIST '18): https://github.com/interactionlab/InfiniTouch 
* (Classification) Identifying Touches from Palms and Fingers on Capacitive Touchcsreens (CHI '18): http://github.com/interactionlab/PalmTouch
