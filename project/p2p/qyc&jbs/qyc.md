## 3.3 第三次迭代
### 3.3.1 分解系统组件

本迭代分解服务端节点的注册-发现模块。

### 3.3.2 确定架构驱动因素

| # |架构驱动因素|重要性|难易度|
|---|---|-|-|
|1|场景10：用户请求服务|高|高|
|2|场景11：用户请求其他用户的信息|高|中|
|3|场景3：系统故障|中|中|
|4|场景6：断线重连|低|中|

### 3.3.3 选择架构模式

#### 3.3.3.1 设计关注点

|质量属性|设计关注点|子关注点|
|---|---|-|
|可用性|预防故障|重启<br>数据完整性|
|可用性|故障检测|监控服务端节点|
|可用性|故障恢复|对客户端节点透明|
|可靠性|客户端节点信息正确可用|检测失效客户端节点|

#### 3.3.3.2 候选模式

**重启**

|#|模式|时间|服务不可用|
|---|---|-|-|
|1|冷启动|>1分钟|是|
|2|热备份|>0.3秒|可能|
|3|多主机备份|无|否|

选择模式：模式2

理由：冷启动需要超过1分钟的时间，这个期间系统处于服务不可用的状态，这是无法接受的。多主机备份复杂度太高，实现困难，故选择热备份。

**数据完整性**

|#|模式|存储负载|处理器负载|
|---|---|-|-|
|1|检查点|1秒每分钟|无|
|2|检查点+变化日志|1秒每分钟+每秒30条消息|无|
|3|检查点+捆绑日志|1秒每分钟+每x秒1条消息|无|
|4|检查点+同步备份|1秒每分钟+每x秒1条消息|每x秒同步一次主机和备份的状态|

选择模式：模式3

理由：光靠检查点不能完全恢复系统状态；对服务端节点的访问是时间不均匀的，每产生一次变化就记录一条日志可能会导致很大的存储负载；同步主机的状态到备份则会消耗一定的处理器，因此选择检查点+捆绑日志。

**监控服务端节点**

|#|模式|信息负载|
|---|---|-|
|1|心跳机制|1|
|2|Ping&Echo|2|
|3|客户端节点检测故障|0|

选择模式：模式1

理由：心跳机制实现简单，通信量也没有Ping&Echo那么多。客户端节点检测到故障并不能告知服务端节点，因而选择心跳机制。

**对客户端节点透明**

|#|模式|通信方式|超时位置|
|---|---|-|-|
|1|客户端节点处理故障|单播|客户端节点|
|2|代理处理故障|单播|代理|
|3|基础设施处理故障|广播|基础设施内部|

选择模式：模式2

理由：在客户端处理故障要求客户端节点了解故障的细节，可能会导致系统不够鲁棒；在基础设施内处理故障，广播会占用较大的带宽，因此选择用代理。

**检测失效客户端节点**

|#|模式|反应速度|通信负载|
|---|---|-|-|
|1|心跳机制|快|每秒1次|
|2|客户端检测+广播|慢|1+N次|

选择模式：模式1

理由：客户端检测失效节点并向其他人广播会占用大量的通信带宽，并且网络的更新有延时，其他客户端节点不一定能及时得知失效节点，因此选择心跳机制。

### 3.3.4 候选模式与对应ASR

| # | 模式类型 | 选择的模式 | 架构驱动 |
| -- | -- | -- | -- |
|1|重启|热备份|场景10：用户请求服务<br>场景3：系统故障|
|2|数据完整性|检查点+捆绑日志|场景10：用户请求服务<br>场景3：系统故障|
|3|监控服务端节点|心跳机制|场景10：用户请求服务<br>场景3：系统故障|
|4|对客户端节点透明|代理处理故障|场景10：用户请求服务<br>场景3：系统故障|
|5|检测失效客户端节点|心跳机制|场景11：用户请求其他用户的信息<br>场景6：断线重连|

### 3.3.5 架构视图

**c&c视图**

![server-register c&c](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/%E6%B3%A8%E5%86%8C-%E5%8F%91%E7%8E%B0c%26c.jpg)

**模块视图**

![server-register model](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/%E6%B3%A8%E5%86%8C-%E5%8F%91%E7%8E%B0model.jpg)


## 3.4 第四次迭代
### 3.4.1 分解系统组件

本迭代分解客户端节点的网络模块。

### 3.4.2 确定架构驱动因素

|#|架构驱动因素|重要性|难易度|
|---|---|-|-|
|1|场景10：用户请求服务|高|中|
|2|场景6：断线重连|高|中|
|3|场景11：用户请求其他用户的信息|高|中|
|4|功能需求5：参加考试|中|中|

### 3.4.3 选择架构模式

#### 3.4.3.1 设计关注点

|质量属性|设计关注点|子关注点|
|---|---|-|
|可用性|预防故障|监测相邻节点|
|可用性|从故障中恢复|重启<br>数据完整性|
|可靠性|减少无效数据|数据交换|

#### 3.4.3.2 候选模式

**监测相邻节点**

|#|模式|通信开销|连接信息的时效性|
|-|-|-|-|
|1|心跳机制|随着网络增大而增大|高|
|2|超时重试机制|小|低|

选择模式：模式2

理由：采用心跳机制检测相邻节点的连接是否有效，虽然可以保证客户端节点的连接信息的时效性，但是随着P2P网络增大，心跳机制带来的通信开销也会增大，最终增大到不可接受的地步。相比之下，超时重试机制实现简单，开销较低，因此选择超时重试机制。

**重启**

|#|模式|时间|服务不可用|
|---|---|-|-|
|1|冷启动|>5秒|是|
|2|热备份|>0.3秒|可能|
|3|多主机备份|无|否|

选择模式：模式1

理由：对于客户端节点而言，多主机备份运行程序是不现实的；热备份虽然启动时间快，但是实现起来较为复杂。相比之下，遇到问题就重启更符合一般用户的操作习惯，5秒以上的等待时间也并非不能接受，故选择冷启动。

**数据完整性**

这一部分放在客户端节点的数据持久化模块实现。

**数据交换**

|#|模式|频率|通信负载|
|-|-|-|-|
|1|快泛洪|每分钟一次|高|
|2|慢泛洪|每五分钟一次|低|

选择模式：模式2

理由：若客户端频繁向相邻节点交换数据，会占用较大带宽，影响网络的正常通信，因此选择频率较低的慢泛洪。

### 3.4.4 候选模式与对应ASR

| # | 模式类型 | 选择的模式 | 架构驱动 |
| -- | -- | -- | -- |
|1|监测相邻节点|超时重试机制|场景10：用户请求服务<br>场景6：断线重连|
|2|重启|冷启动|场景10：用户请求服务|
|3|数据完整性|N/A|场景6：断线重连<br>功能需求5：参加考试|
|4|数据交换|慢泛洪|场景11：用户请求其他用户的信息|

### 3.4.5 架构视图

![client-register c&c](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/client-register%20c%26c.jpg)

![client-register model](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/client-register%20model.jpg)

## 3.5 第五次迭代
### 3.5.1 分解系统组件

本迭代分解客户端节点的数据持久化模块。

### 3.5.2 确定架构驱动因素

|#|架构驱动因素|重要性|难易度|
|---|---|-|-|
|1|约束1：用户的数据不得被泄露和非法修改|高|高|
|2|场景7：学生数据防止被攻击和篡改|高|高|
|3|场景3：系统故障|高|中|
|4|场景6：断线重连|高|中|
|5|功能需求5：参加考试|高|中|
|6|功能需求1：课程签到|中|低|
|7|功能需求2：发言举手|中|低|
|8|功能需求3：参与讨论|中|低|
|9|功能需求6：信息推送|中|低|

### 3.5.3 选择架构模式

#### 3.2.3.1 设计关注点

| 质量属性 | 设计关注点 | 子关注点  |
| -------- | ---------- | --------- |
|安全性|用户数据安全|防止泄露<br>防止非法修改|
|可用性|故障预防|数据完整性|

#### 3.5.3.2 候选模式

**防止泄露**

|#|模式|实现难度|
|-|-|-|
|1|数据加密|低|
|2|限制数据库远程访问|低|
|3|限制数据库远程访问+数据加密|中|

选择模式：模式3

理由：单单给数据加密并不能防止数据被泄露，只是增加了解读数据的难度；限制远程访问数据库可以有效防御外部的恶意访问，但却不能很好的应对本地攻击，因此综合采取两种方式来防止数据泄露。

**防止非法修改**

|#|模式|实现难度|安全性|性能开销|
|-|-|-|-|-|
|1|校验位|高|高|高|
|2|限制数据库访问权限|低|中|无|

选择模式：模式2

理由：虽然使用数据校验位安全性很高，但其实现难度也高，并且会带来额外的性能开销；限制数据库访问权限提供的安全性已经可以满足要求了，因此选择它。

**数据完整性**

|#|模式|存储负载|处理器负载|
|---|---|-|-|
|1|检查点|1秒每半分钟|无|
|2|检查点+变化日志|1秒每半分钟+每秒2条消息|无|
|3|检查点+捆绑日志|1秒每半分钟+每x秒1条消息|无|
|4|检查点+同步备份|1秒每半分钟+每x秒1条消息|每x秒同步一次主机和备份的状态|

选择模式：模式2

理由：单单使用检查点不能完全恢复出错前的现场信息，捆绑日志实现起来较为复杂，同步备份则由于客户端节点并未采取备份而不可行。考虑到实际使用中，用户的操作频率有一定的上限，使用变化日志带来的存储负担还可接受，故选择检查点+变化日志。

### 3.5.4 候选模式与对应ASR

| # | 模式类型 | 选择的模式 | 架构驱动 |
| -- | -- | -- | -- |
|1|防止泄露|限制数据库远程访问+数据加密|约束1：用户的数据不得被泄露和非法修改|
|2|防止非法修改|限制数据库访问权限|约束1：用户的数据不得被泄露和非法修改<br>场景7：学生数据防止被攻击和篡改|
|3|数据完整性|检查点+变化日志|场景3：系统故障<br>场景6：断线重连<br>功能需求1：课程签到<br>功能需求2：发言举手<br>功能需求3：参与讨论<br>功能需求5：参加考试<br>功能需求6：信息推送|

### 3.5.5 架构视图

![data c&c](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/data%20c%26c.jpg)

![data model](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/data%20model.jpg)

## 3.6 第六次迭代
### 3.6.1 分解系统组件

本迭代分解客户端节点的签到模块。

### 3.6.2 确定架构驱动因素

|#|架构驱动因素|重要性|难易度|
|---|---|-|-|
|1|场景1：大量用户短时间内进行数据量交换较小的操作|高|高|
|2|约束1：用户的数据不得被泄露和非法修改|高|中|
|3|功能需求1：课程签到|高|低|

### 3.6.3 选择架构模式

#### 3.6.3.1 设计关注点

| 质量属性 | 设计关注点 | 子关注点  |
| -------- | ---------- | --------- |
|性能|高并发|密集网络请求|
|安全性|数据安全|禁止非法修改<br>数据完整性|

#### 3.6.3.2 候选模式

**密集网络请求**

|#|模式|实现难度|并发性能|
|-|-|-|-|
|1|同步请求|低|<60（取决于硬件和容器）|
|2|集群+负载均衡|高|>10000（取决于硬件和容器）|
|3|消息队列|中|>150（取决于硬件和容器）|

选择模式：模式3

理由：使用同步的方式处理请求将不能满足场景1要求的5s内处理300人的请求；集群+负载均衡的并发性能最好，但是实现难度较大，不经济；使用消息队列缓存用户请求，实现难度中等，并发能力也足以满足要求。

**禁止非法修改**

|#|模式|实现难度|安全性|性能开销|
|-|-|-|-|-|
|1|校验位|高|高|高|
|2|限制数据库访问权限|低|中|无|

选择模式：模式2

理由：虽然使用数据校验位安全性很高，但其实现难度也高，并且会带来额外的性能开销；限制数据库访问权限提供的安全性已经可以满足要求了，因此选择它。

**数据完整性**

|#|模式|存储负载|处理器负载|
|---|---|-|-|
|1|检查点|1秒每半分钟|无|
|2|检查点+变化日志|1秒每半分钟+每秒2条消息|无|
|3|检查点+捆绑日志|1秒每半分钟+每x秒1条消息|无|
|4|检查点+同步备份|1秒每半分钟+每x秒1条消息|每x秒同步一次主机和备份的状态|

选择模式：模式3

理由：检查点不能完全保证数据完整性，使用变化日志在高并发的场景下会产生大量的日志，对磁盘的IO能力是一个较大的挑战；同步备份在客户端节点稍显小题大做，故而选择检查点+捆绑日志。

### 3.6.4 候选模式与对应ASR

| #   | 模式类型 | 选择的模式 | 架构驱动 |
| --- | -------- | ---------- | -------- |
|1|密集网络请求|消息队列|场景1：大量用户短时间内进行数据量交换较小的操作<br>功能需求1：课程签到|
|2|禁止非法修改|限制数据库访问权限|功能需求1：课程签到<br>约束1：用户的数据不得被泄露和非法修改|
|3|数据完整性|检查点+捆绑日志|功能需求1：课程签到<br>约束1：用户的数据不得被泄露和非法修改|

### 3.6.5 架构视图

![signup c&c](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/signup%20c%26c.jpg)

![signup model](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/signup%20model.jpg)

## 3.7 第七次迭代
### 3.7.1 分解系统组件

本迭代分解客户端节点的考试模块。

### 3.7.2 确定架构驱动因素

|#|架构驱动因素|重要性|难易度|
|---|---|-|-|
|1|场景6：断线重连|高|高|
|2|场景7：学生数据防止被攻击和篡改|高|高|
|3|约束1：用户的数据不得被泄露和非法修改|高|高|
|4|场景1：大量用户短时间内进行数据量交换较小的操作|中|中|

### 3.7.3 选择架构模式

#### 3.7.3.1 设计关注点

| 质量属性 | 设计关注点 | 子关注点  |
| -------- | ---------- | --------- |
|可用性|故障检测|快速反应|
|可用性|故障恢复|数据完整性|
|安全性|数据安全|防止泄露数据<br>禁止非法修改|
|性能|高并发|密集网络请求|

#### 3.7.3.2 候选模式

**快速反应**

|#|模式|反应时间|准确率|
|-|-|-|-|
|1|异常处理|<0.1秒|可能会误判|
|2|出错重试|<2秒|误判可能性较低|

选择模式：模式2

理由：在通过网络传输数据时，一出现异常就启动断线重连机制，这样做虽然反应快，但可能会把瞬时的网络波动误判成断线；出错重试机制虽然会花费更多时间，但并没有超出要求规定的5秒，且准确率更高，故选择之。

**数据完整性**

|#|模式|存储负载|实现难度|
|---|---|-|-|
|1|检查点|1秒每15秒|低|
|2|检查点+变化日志|1秒每半分钟+每秒2条消息|中|
|3|检查点+捆绑日志|1秒每半分钟+每x秒1条消息|中|
|4|检查点+同步备份|1秒每半分钟+每x秒1条消息|高|

选择模式：模式1

理由：虽然检查点不能完全恢复现场数据，但是场景7并没有要求要完全恢复现场信息，因此实现最简单的检查点完全够用。除此之外，进一步缩短检查点的周期，可以有效提高数据恢复程度。

**防止泄露数据**

这部分在数据持久化模块实现。

**禁止非法修改**

这部分在数据持久化模块实现。

**密集网络请求**

|#|模式|实现难度|并发性能|
|-|-|-|-|
|1|同步请求|低|<60（取决于硬件和容器）|
|2|集群+负载均衡|高|>10000（取决于硬件和容器）|
|3|消息队列|中|>150（取决于硬件和容器）|

选择模式：模式3

理由：使用同步的方式处理请求将不能满足场景1要求的5s内处理300人的请求；集群+负载均衡的并发性能最好，但是实现难度较大，不经济；使用消息队列缓存用户请求，实现难度中等，并发能力也足以满足要求。

### 3.7.4 候选模式与对应ASR

| #   | 模式类型 | 选择的模式 | 架构驱动 |
| --- | -------- | ---------- | -------- |
|1|快速反应|出错重试|场景6：断线重连|
|2|数据完整性|检查点|场景6：断线重连|
|3|防止泄露数据|N/A|场景7：学生数据防止被攻击和篡改<br>约束1：用户的数据不得被泄露和非法修改|
|4|禁止非法修改|N/A|场景7：学生数据防止被攻击和篡改<br>约束1：用户的数据不得被泄露和非法修改|
|5|密集网络请求|消息队列|场景1：大量用户短时间内进行数据量交换较小的操作|

### 3.7.5 架构视图

![exam c&c](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/exam%20c%26c.jpg)

![exam model](https://github.com/viccrubs/ClassroomAssistant/blob/master/project/p2p/qyc&jbs/img/exam%20model.jpg)
