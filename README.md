# Mol-Dorm (宿舍管理系统)

这是一个基于 Spring Boot 的多模块宿舍管理系统后端项目。

## 📁 项目结构

- **mol-common**: 公共模块，包含核心工具类、MyBatis Plus 配置、安全配置 (Sa-Token) 等。
- **mol-dorm-biz**: 宿舍业务模块，处理宿舍分配、报修、水电费等逻辑。
- **mol-server**: 系统基础服务模块，处理用户管理、权限、组织架构、学生信息等。
- **mol-launcher**: 项目启动模块，包含主启动类。

## 🛠️ 技术栈

- **核心框架**: Spring Boot 3.5.5+
- **数据库 ORM**: MyBatis Plus
- **权限认证**: Sa-Token
- **数据库**: MySQL 8.4+
- **构建工具**: Maven 3.9+

## 🚀 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.9+
- MySQL 8.4+

### 2. 数据库初始化

1. 创建数据库（例如命名为 `mol_dorm`）。
2. 执行项目根目录下的 `dorm.sql` 脚本，初始化表结构和数据。

### 3. 配置修改

请检查以下配置文件，确保数据库连接信息正确：

- `mol-launcher/src/main/resources/application.yml` (或引用的子配置文件)
- `mol-common/mol-common-core/src/main/resources/common-base.yml` (公共配置，包含数据库、Redis)
### 4. 启动项目

进入项目根目录，运行以下命令启动：

```
# 使用 Maven Wrapper 运行
./mvnw spring-boot:run -pl mol-launcher
```

或者在 IDE (IntelliJ IDEA / Eclipse) 中打开项目，找到 `mol-launcher` 模块下的 `MolLauncherApplication` 类运行 main 方法。

## ⚠️ 注意事项

- 请确保 `src/main/resources/mapper` 下的 XML 文件完整，否则可能会导致部分业务报错。
- 默认端口(9090)设置请参考配置文件。

## 📄 License

[MIT License](https://www.google.com/search?q=LICENSE)
