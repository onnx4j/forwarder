# forwarder

## 简介
### Forwarder是什么

++Forwarder++是一个基于Java开发的，用于针对神经网络forward（前馈）操作的Framework。其面向ONNX标准规范，支持所有符合ONNX的模型加载并执行forward操作。

++Forwarder++并不实现具体的Operator（如：Acos、ArgMax、MatMul等），具体的Operator是由各种主流的神经网络框架作为++Forwarder++的Backend接入实现。我们可以使用TensorFlow或者DeepLearning4j作为Forwarder的Backend，并支持运行时动态切换不同的Backend来执行具体的forward操作。

### 解决什么问题
一般地，人工智能的项目开发由数据准备、预处理（数据清洗和归一化）、定义模型、模型训练、后处理、部署等环节，组成一条完成的生产Pipeline。

而++Forwarder++专注于服务端和嵌入式设备端的模型部署，目标是要解决，从模型训练完成后到模型提供服务这个环节中存在一系列问题。我们对用户使用何种神经网络框架进行模型的设计和如何进行模型的训练并无入侵性也并不做限制，只要该输出的模型能通过ONNX转换器转换为符合ONNX标准的模型即可使用++Forwarder++执行forward操作。

 
