# LCN分布式事务框架

## 框架特点

1. 支持各种基于spring的db框架
2. 兼容springcloud、dubbo
3. 使用简单，demo详细，项目开源
4. 基于切面的强一致性事务框架
5. 高可用性，模块可以依赖dubbo或者springcloud的集群方式做集群化，TxManager也可以做集群化


## 目录说明

lorne-tx-core 是LCN分布式事务框架的切面核心类库

dubbo-transaction 是LCN dubbo分布式事务框架

springcloud-transaction 是LCN springcloud分布式事务框架

tx-manager 是LCN 分布式事务协调器（TxManager）


## 关于框架的设计原理

见 [TxManager](https://github.com/1991wangliang/tx-lcn/blob/master/tx-manager/README.md)

## demo 说明

dubbo版本的demo [dubbo-demo](https://github.com/1991wangliang/dubbo-lcn-demo)

springcloud版本的demo [springcloud-demo](https://github.com/1991wangliang/springcloud-lcn-demo)


技术交流群：554855843