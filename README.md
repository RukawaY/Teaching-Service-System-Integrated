# Teaching-Service-System
Project for 2025 Software Engineering Basis

## 后端

首先启动`mysql`数据库，对于macOS使用`brew services start mysql`启动。

如果之前没有创建过数据库，在`mysql`中运行数据库初始化的sql文件`src/main/resources/init.sql`。

```bash
source /PATH/TO/init.sql
```

之后修改`src/main/resources/jdbc.properties`中的数据库连接信息，将`jdbc.password`改为你自己的`mysql`密码。

在终端执行以下命令：

```bash
mvn clean package dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp target/classes:$(cat cp.txt) com.Main.AppConfig
```

## 前端

首先切换到`front-end`目录下。

### 安装依赖

```bash
npm install
```

### 运行

```bash
npm run dev
```

**注：由于index.js中设置了不能手动修改URL，如果需要在没有后端的情况下运行，请修改以下两处：**

1. 将`src/router/index.js`中115-136行注释
2. 将`src/App.vue`中13、49行的`'invalid'`改为`'student'`, `'admin'`或`'teacher'`

**之后在浏览器中手动修改URL为`localhost:3000/home`，即可跳过登录页面进入主页面。**