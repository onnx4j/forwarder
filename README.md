# Forwarder

## 简介
### Forwarder是什么

Forwarder是一个基于Java开发的，用于针对神经网络forward/inference（前馈/推理）操作的Framework。其遵循ONNX（Open Neural Network Exchange）标准规范，支持所有符合ONNX规范的模型加载并执行forward操作。

Forwarder并不执行具体的Operator（如：Conv、Lstm、ArgMax、MatMul等）运算，具体的Operator运算负载是由Forwarder提供的Backend机制，让各种主流的神经网络框架接入为Backend实现具体的运算负载。我们可以使用TensorFlow、DeepLearning4j等，支持在Java环境下开发和运行的神经网络框架作为Forwarder的Backend。

开发者可以选择自己喜爱和熟悉的神经网络框架进行模型的定义和训练，通过ONNX官方提供的模型转型程序转为为有效的ONNX模型，然后可以使用Forwarder加载并执行forward/inference操作。

由于基于Java语言进行开发，我们可以有效地利用Java成熟的生态圈快速开发和交付与人工智能相关的软件项目。例如，我们可以使用Forwarder与Spring Boot结合，这样就能非常便捷地将训练好的模型，部署为一系列以WEB形式表现的接口以提供服务。

### 解决什么问题
一般地，人工智能的项目开发由数据准备、预处理（数据清洗和归一化等）、定义模型、模型训练、后处理、部署等环节，组成一条完整的生产Pipeline。

Forwarder专注于服务端和嵌入式设备端的模型部署与执行。目标是要解决，从模型训练完成后到模型提供服务这个环节中存在一系列问题。我们对用户使用何种神经网络框架进行模型的设计和如何进行模型的训练并无入侵性也并不做限制，只要该输出的模型能通过ONNX转换器转换为符合ONNX标准的模型即可使用Forwarder执行forward操作。

### 担心的问题
看到这个项目时，或许你最担心的是Forwarder的执行性能问题。这个也是作者在开始是最担心的一个问题，毕竟考虑到人工智能/神经网络的程序属于计算密集型，一般地都是以C/C++作为主要的开发语言，虚拟机好像并不适合处理这个艰巨的问题。非常坦诚地说，使用Java在这一领域进行开发，几乎能难达到C/C++的运行性能，如果你介意这点请无视此项目。

然而，作者认为Java是一个非常优秀的语言，有着非常成熟的生态圈，非常适合开发达到工业级的应用程序。而在处理forward/inference操作时，由于涉及JNI的调用，性能损耗是免不了的。但我们可以在最大程度上减少JNI的交互调用，并且降低JVM的堆内内存与堆外内存的拷贝情况，以保证整体性的损耗在可控范围内。在此基础上，在结合Java成熟的生态圈，保证所交付的程序稳定性与易维护性。

## 使用
### 运行要求
* Linux/MacOS/Windows
* Maven
* Oracle JRE 1.8
* 符合ONNX规范的模型文件（当前最高支持v9的指令集）
### 快速开始
开发者可根据需要使用Forwarder的Backend扩展机制自行实现运算负载，但为了展示如何快速开始使用Forwarder，开发者可以从我们提供的[forwarder.backend.tensorflow](https://github.com/onnx4j/forwarder.backend.tensorflow)或[forwarder.backend.dl4j](https://github.com/onnx4j/forwarder.backend.dl4j)中选择其中一个或多个Backend作为运算负载。

* 导入Maven依赖包
```
<!-- 使用基于Google Tensorflow的Forwarder Backend -->
<dependency>
  <groupId>org</groupId>
  <artifactId>forwarder.backend.tensorflow</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>

<!-- 使用基于Deeplearning4j的Forwarder Backend -->
<dependency>
  <groupId>org</groupId>
  <artifactId>forwarder.backend.dl4j</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
> 备注：由于当前项目还没有上传至Maven中心仓库，请开发者先自行浏览我们的github，checkout所有必须的项目。

* 加载与执行ONNX模型
```
String modelPath = "./model.onnx";

Forwarder f = Forwarder
  .config(
    Config.builder()
    .setDebug(true)
    .setMemoryByteOrder(ByteOrder.LITTLE_ENDIAN) // 内存存储顺序
    .setMemoryAllocationMode(AllocationMode.DIRECT) // 使用off-heap内存
    .build()
  )
  .load(modelPath)
  .executor("ray") // recursion:递归式图遍历执行器，ray:非递归式图遍历执行器

// 选择使用Tensorflow作为运算Backend
Backend<?> tfBackend = f.backend("Tensorflow");
try (Session<?> session = tfBackend.newSession()) {
  Tensor x2_0 = Tensor
    .builder(
      DataType.FLOAT, 
      Shape.create(2L, 1L), 
      Tensor.options()
    )
    .putFloat(3f)
    .putFloat(2f)
    .build();
  Tensor y0 = session.feed("x2:0", x2_0).forward().getOutput("y:0");
  System.out.println(y0.toString());
}

// 选择使用Deeplearning4J作为运算Backend
Backend<?> dl4jBackend = f.backend("DL4J");
try (Session<?> session = dl4jBackend.newSession()) {
  Tensor x2_0 = Tensor
    .builder(
      DataType.FLOAT, 
      Shape.create(2L, 1L), 
      Tensor.options()
    )
    .putFloat(3f)
    .putFloat(2f)
    .build();
  Tensor y0 = session.feed("x2:0", x2_0).forward().getOutput("y:0");
  System.out.println(y0.toString());
}
```

## 更多
### 项目路线图
* 实现对ONNX规范的所有指令集的支持
* 提供动态剪枝与精度缩减等模型运行时优化策略
* 更好地运行于服务器端与端设备的两种常见下的模型forward
### 联系我们
HarryLee
EMAIL: ineharrymicro@21cn.com
