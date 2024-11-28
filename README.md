# Vietnamese Vegetable Recognition

Vietnamese Vegetable Recognition on Android devices

# Introduction

In Southern Vietnam, the diversity of vegetables plays a crucial role in the region’s agriculture and economy. However, due to the different of species makes it difficult for many people in different regions, leading to confusion, misinformation, and improper handling techniques for those who are not experts in the field. In addition, there is a lack of existing mobile application tailored to recognizing Southern Vietnamese vegetables on Android platform. Therefore, we decide to develop an Android application that can utilized image recognition and machine learning technology to accurately detect and provide detailed information about Southern Vietnamese vegetables.

# Proposed Approaches

We choose YOLOv11 model simply it is designed to be fast, accurate, easy to use, and it can be deployed on edge devices, especially on mobile application. However, the existing of extensive feature maps in YOLO architecture is one of the most common problems which lead to high computation cost of the model, which often been neglected in the architecture design. To address this, we implement a light-weight, cost effective method known as "Cheap Operation", which minimize the number of parameters of the models, without sacrificing a lot on accuracy while maintain strong detection performance. We propose a modification to the backbone of YOLOv11 with GhostNet architecture combine with Convolutional Block Attention Module, while the neck and the head of the model remain unchanged.

# Dataset

A custom dataset we collected from various sources, a total of 26 classes. Dataset can be found at [here](https://universe.roboflow.com/nckh-oufrk/vietnamese-vegetation-detection)






