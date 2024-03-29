# 用JavaSocket编程开发聊天室
## 实验内容
1. 用Java图形用户界面编写聊天室服务器端和客户端， 支持多个客户端连接到一个服务器。每个客户端能够输入账号。
2. 可以实现群聊（聊天记录显示在所有客户端界面）。
3. 完成好友列表在各个客户端上显示。
4. 可以实现私人聊天，用户可以选择某个其他用户，单独发送信息。
5. 服务器能够群发系统消息，能够强行让某些用户下线。
6. 客户端的上线下线要求能够在其他客户端上面实时刷新。

## 架构业务分析(基于自身设计)
### （一）总体背景
<p>网络聊天程序的背景可以追溯到计算机网络的发展。随着互联网的普及，人们开始探索在虚拟空间中进行交流的可能性。最早的网络聊天程序可以追溯到上世纪80年代和90年代的互联网聊天室（Internet Relay Chat，简称 IRC）和即时通信工具（Instant Messaging）。这些工具为用户提供了一种方式，在互联网上找到其他人并与他们进行交谈。
<p>随着时间的推移，网络聊天程序逐渐演变和发展。社交媒体平台、在线游戏以及各种即时通讯应用程序如微信和Telegram等的出现，进一步丰富了网络聊天的形式和功能。这些应用程序通过引入更多的功能和特性，提供了更丰富、更个性化的交流体验。
<p>其中，匿名或临时聊天室是网络聊天程序的一个特殊类型。匿名聊天室允许用户在不透露个人身份的情况下进行聊天和交流。这种形式的聊天室通常用于分享兴趣、讨论敏感话题或寻求匿名意见。它们提供了一种安全和私密的环境，让用户可以自由表达自己的想法和情感。
<p>匿名或临时聊天室具有以下重要特点：
1. 隐私保护：匿名或临时聊天室对于那些希望保护个人身份和隐私的用户来说非常重要。在这些聊天室中，用户可以自由地表达意见、分享故事或寻求建议，而无需透露真实身份。这种匿名性可以使用户感到更加安全和放心。
2. 自由表达和讨论敏感话题：匿名或临时聊天室为用户提供了一个开放的平台，可以自由地表达观点、讨论敏感话题或分享个人经历，而不会担心被他人追踪或审查。这种自由可以鼓励人们参与深入的讨论，并促进信息的自由流通。
3. 心理支持和咨询：匿名或临时聊天室在心理健康领域具有重要作用。它们可以提供一个安全、私密的环境，让人们可以匿名地寻求心理支持、分享困扰和获得专业建议。这对于那些不愿意或无法面对面咨询的人来说，是一个宝贵的资源。
4. 社交互动与兴趣分享：匿名或临时聊天室还可用于社交互动和兴趣分享。用户可以加入特定主题或兴趣群组，与志同道合的人进行讨论和交流。这种形式的聊天室可以帮助人们拓展社交圈子、结识新朋友，并深入探讨共同的兴趣爱好。
<p>由此，本程序是基于聊天话题而实现的聊天室，具有匿名聊天室和即时聊天室的特点，允许用户创建一个临时或匿名账号加入聊天，并在离开后删除所有的聊天室记录和用户信息。

### （二）业务分析
1. 聊天话题管理
- 定义了四个基本的话题分区，分别是游戏，生活，时事，学习区。
- 用户可在创建聊天室时，选择每个聊天室的分区，来实现聊天室的话题分类。
- 程序将创建四个队列，来分别存储每个分区的聊天室基本信息。

2. 聊天室管理
- 聊天室的基本属性有：聊天室名称，标签（也就是话题分类），简介和管理员。聊天室名称作为聊天室的唯一标识，在创建时需进行重复识别判断。其中，设置第一个创建聊天室的用户即为这个聊天室的管理员。
- 当管理员离开聊天室之后，将删除此聊天室的聊天记录和用户信息。

3. 匿名连接处理
- 用户进入程序时，创建一个唯一的用户名，不涉及身份信息或注册登录。此处将进行重复识别判断。
- 当用户离开聊天室后，其用户信息将被删除或重新使用，也就是释放此用户名。

4. 消息传递
- 使用C/S架构来实现聊天室消息的转发；
- 通过消息发送时携带的标识符使特定聊天室特定用户进行消息的接收。
- 聊天信息可以是文本，表情符号和图片等内容。

5. 聊天记录管理
- 不保存长期聊天记录，遵循即聊即走的思想。
- 注意确保数据安全性，避免意外泄露或恢复被删除的记录。

6. 好友管理
- 由于本聊天室并不是一个基于人际关系划分的，所以并未为每一个用户开辟好友功能，只是简单的在聊天室的好友列表里记录当前进入此聊天室的用户信息。
- 当用户离开聊天室后，好友列表里对应的用户信息将自动删除。
