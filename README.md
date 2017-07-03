# LCN分布式事务框架

## 框架特点

1. 支持各种基于spring的db框架
2. 兼容springcloud、dubbo
3. 使用简单，代码完全开源
4. 基于切面的强一致性事务框架
5. 高可用，模块可以依赖dubbo或springcloud的集群方式做集群化，TxManager也可以做集群化

## 使用示例

分布式事务发起方：
```java

    @Override
    @TxTransaction
    public boolean hello() {

        testDao.save();

        boolean res =  test2Service.test();//远程调用方

        int v = 100/0;

        return true;
    }
    
```

分布式事务被调用方(test2Service的业务实现类)
```java

    @Override
    public boolean test() {
     
        testDao.save();
        
        return true;
    }

```

说明：只需要在分布式事务的**开启方**添加`@TxTransaction`注解即可。详细使用步骤见demo


## 目录说明

lorne-tx-core 是LCN分布式事务框架的切面核心类库

dubbo-transaction 是LCN dubbo分布式事务框架

springcloud-transaction 是LCN springcloud分布式事务框架

tx-manager 是LCN 分布式事务协调器（TxManager）


## 关于框架的设计原理

见 [TxManager](https://github.com/1991wangliang/tx-lcn/blob/master/tx-manager/README.md)

## demo 说明

demo里包含jdbc\hibernate\mybatis版本的demo

dubbo版本的demo [dubbo-demo](https://github.com/1991wangliang/dubbo-lcn-demo)

springcloud版本的demo [springcloud-demo](https://github.com/1991wangliang/springcloud-lcn-demo)


技术交流群：554855843