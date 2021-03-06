---
title: OAuth2
---

> 在使用之前，您还是需要对 OAuth2 开放授权协议具有一定的认识。

## 简述

在大多数情况之下，此协议是作为第三方客户端进行接入的时候进行使用，例如 QQ 的 OAuth2 授权码流程如下：

1. 在第三方客户端应用（例如某个社交网站，域名为 `example.com` ）点击 “使用 QQ 帐号进行登录”。
2. 跳转到 QQ 登录的网页，此时域名为 `qq.com`，也就是到了 QQ 的 OAuth 授权服务器之上了，这个时候输入帐号密码就是在他的网页上面进行输入的就是安全的。
3. 在 `qq.com` 登录成功以后，他会跳转到第三方客户端指定的一个地址，我们称之为 **回调地址**，同时携带一个 **授权码** 的参数（大多数情况下是 url 参数）。
4. 我们在回调地址之中获取到 **授权码** 参数，然后携带这个参数以及我们第三方客户端的信息（一般是客户端id和客户端密钥）去请求指定的接口获取用户的个人信息或者登录凭证。

这样一个 OAuth2 的一个第三方客户端进行授权操作就完成了。在这个过程中有如下几点好处：

1. 保护用户账户信息安全：输入用户帐号密码的过程是在应用中完成的而不是第三方客户端那里完成的（即使用 QQ 登录时，帐号密码的输入是在 `qq.com` 的域名下完成的）
2. 当有新的客户端接入时变得极其容易，只需要添加客户端的信息即可。
3. 有效管理客户端权限：例如某些客户端只能够读取用户信息而不能够修改用户信息，完全可以通过使用客户端 id 进行具体的接口、权限管理

## 权限控制

那么在 OAuth2 我们如何来进行用户的权限管理和权限鉴定呢？在一般情况下，可以用通过 `scope` 来完成这么一个过程。

> `scope` 可以简单可以复杂也可以完全没有。

### 不使用 scope

不使用 scope 意味着你所有的资源都是开放的，在这种情况下一般都是只提供 **读取** 接口，例如允许某个第三方客户端读取用户信息、角色信息等。

- 好处：简单、快捷
- 坏处：只能读取无法修改

### 简单 scope

简单的 `scope` 一般就只提供几种描述动作的操作符，最为常见的是 `READ`、`WRITE`、`ALL`；通过第三方客户端接入，每个客户端可以获取到不同的权限。

例如： A 应用可以修改用户的信息，其申请的 `scope` 应该为 `WRITE`，B 应用只能够读取用户信息，其申请的 `scope` 应该位 `READ`。

- 好处：简单的资源操作控制
- 坏处：没有对资源的具体操作限制

### 复杂的 scope

复杂的 `scope` 就涉及到对于某个资源的详细控制，可以把它看成 `ACL` 权限控制。

- 比如 `message:READ` 表示对 `message` 的资源具有 `READ` 权限
- 比如 `user:WRITE` 表示对 `user` 的资源具有 `WRITE` 权限

这样的权限控制基本覆盖大多数的使用情况

- 好处：详细的资源权限控制
- 坏处：实现具有一定难度，不可动态修改，灵活性较低

### RBAC 动态模型

在我们的应用中，涉及到了一个最为重要的东西：**角色（ROLE）**。我们使用 **RBAC(Role-based access control)** 进行我们的权限管理。 

![rbac](/images/rbac.png)

所以在使用 `scope` 的情况对我们来说是不适用的，这种情况下我们如何去管理权限呢？我更希望使用 OAuth2 来完全的管理我们的用户角色等信息而不是再去引入一些其他的东西，我将它与 RBAC 授权模型进行搭配使用。如何做呢？

1. 授权服务器的资源只提供 **读** 权限，所有的客户端只能够读取用户的信息，而不能够修改任何信息。
2. 下发令牌时，提供用户用的 `scope` 信息以外的 `ROLE` 信息。
3. 提供一个独立客户端专门来进行修改授权服务器的资源，即：**授权中心**。

角色我们会给客户端，那么图中的 **资源** 应该是什么呢？那应该是第三方客户端的资源，由第三方客户端自己掌控。

换句话说，第三方客户端可以很自由的选择权限控制方式。

1. 使用 `scope`：我们不使用 `scope` 但是不代表第三方客户端不能使用，在客户端足够简单的情况下，可以使用 `scope` 来对你的应用进行权限控制。
2. 使用 `role`：在应用使用的时候，我们会下发给客户端用户的角色信息，客户端完全可以使用他进行 RBAC 权限控制
3. 使用自己的用户系统：如果你想完全脱离我们的应用使用，完全可以在得到我们给予的用户信息以后，自己创建用户并使用一套新的用户管理系统，这个也是 OAuth2 最为本质的使用方式。

## 授权模式

前面我们提到的 QQ 的栗子就是授权模式中的授权码模式。在我们的授权服务器中提供四种授权模式：

1. 授权码模式
2. 密码模式
3. 手机验证码模式
4. 邮箱验证码模式

对于不同的模式

- 安全级别： 1 > 3 > 4 > 2
- 复杂级别： 1 > 3 = 4 > 2
- 上手难度： 1 > 2 = 3 = 4 
- 推荐指数： 1 > 2 > 3 = 4

### 授权码模式

```text

     +----------+
     | Resource |
     |   Owner  |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier      +---------------+
     |         -+----(A)-- & Redirection URI ---->|               |
     |  User-   |                                 | Authorization |
     |  Agent  -+----(B)-- User authenticates --->|     Server    |
     |          |                                 |               |
     |         -+----(C)-- Authorization Code ---<|               |
     +-|----|---+                                 +---------------+
       |    |                                         ^      v
      (A)  (C)                                        |      |
       |    |                                         |      |
       ^    v                                         |      |
     +---------+                                      |      |
     |         |>---(D)-- Authorization Code ---------'      |
     |  Client |          & Redirection URI                  |
     |         |                                             |
     |         |<---(E)----- Access Token -------------------'
     +---------+       (w/ Optional Refresh Token)


   Note: The lines illustrating steps (A), (B), and (C) are broken into
   two parts as they pass through the user-agent.

```