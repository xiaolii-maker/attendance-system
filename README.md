# 班级考勤管理系统

## 项目简介

班级考勤管理系统是一个基于 Spring Boot 开发的 Web 应用，用于管理学生考勤记录。系统支持教师发布打卡任务、学生扫码打卡、请假审批、批量导入数据等功能。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.0 | 后端框架 |
| Spring Security | 3.1.0 | 用户认证与权限控制 |
| Spring Data JPA | 3.1.0 | 数据持久层 |
| Thymeleaf | 3.1.0 | 前端模板引擎 |
| MySQL | 8.0/9.2 | 数据库 |
| Bootstrap | 5.3.0 | 前端样式 |
| Apache POI | 5.2.3 | Excel 文件解析 |
| Maven | - | 项目构建工具 |

## 功能特性

### 用户管理
- 用户注册与登录（Spring Security 认证）
- BCrypt 密码加密
- 角色权限控制（学生/教师/管理员）

### 学生管理
- 学生信息增删改查
- 分页查询展示
- Excel 批量导入学生名单

### 考勤打卡
- 教师发布打卡任务（生成任务码）
- 学生输入任务码 + 学号打卡
- 打卡时间限制（任务开始~任务结束）
- 打卡备注功能

### 考勤记录
- 个人考勤记录查看（学生）
- 全班考勤记录查看（教师/管理员）
- 按课程、班级、日期、状态筛选
- 分页展示

### 请假管理
- 学生提交请假申请（提前请假/补交假条）
- 教师审批请假申请
- 审批意见填写

### 批量导入
- Excel 批量导入学生
- Excel 批量导入考勤
- 数据验证与去重

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

#### 1. 克隆项目

git clone https://github.com/xiaolii-maker/attendance-system.git
cd attendance-system

#### 2. 创建数据库

登录 MySQL，执行：

CREATE DATABASE attendance_system CHARACTER SET utf8mb4;
USE attendance_system;
#### 3. 修改配置文件
编辑 src/main/resources/application.properties：

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=你的MySQL密码

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8080
#### 4. 运行项目

mvn spring-boot:run
#### 5. 访问系统
打开浏览器访问：http://localhost:8080/page/login

默认账号
角色	用户名	密码
管理员	admin	123456
教师	teacher_wang	123456
学生	2024001	123456

项目打包
打包命令

mvn clean package -DskipTests

运行jar包

java -jar target/attendance-system-0.0.1-SNAPSHOT.jar

指定端口运行

java -jar target/attendance-system-0.0.1-SNAPSHOT.jar --server.port=8081
#### 部署说明
课堂使用（局域网）
查看电脑IP：ipconfig（Windows）或 ifconfig（Mac/Linux）

确保电脑和学生手机连接同一个WiFi

启动项目：java -jar attendance-system-0.0.1-SNAPSHOT.jar

学生访问：http://10.67.168.152:8080/task/scan

生产环境部署（云服务器）
安装 JDK 17 和 MySQL

创建生产环境配置文件 application-prod.properties

修改数据库连接为远程地址

上传 jar 包到服务器

运行：java -jar xxx.jar --spring.profiles.active=prod

### 数据库文档
#### 1.user（用户表）
字段名	类型	说明	备注
id	BIGINT	主键	自增
username	VARCHAR(50)	用户名	唯一
password	VARCHAR(100)	密码	BCrypt加密
real_name	VARCHAR(50)	真实姓名	
role	VARCHAR(20)	角色	ADMIN/TEACHER/STUDENT
create_time	DATETIME	创建时间	
#### 2.student（学生表）
字段名	类型	说明	备注
student_id	VARCHAR(20)	学号	主键
name	VARCHAR(50)	姓名	
class_name	VARCHAR(50)	班级	
major	VARCHAR(50)	专业	
gender	VARCHAR(10)	性别	
age	INT	年龄	
create_time	DATETIME	创建时间	
#### 3.course（课程表）
字段名	类型	说明	备注
course_id	VARCHAR(20)	课程编号	主键
course_name	VARCHAR(100)	课程名称	
class_name	VARCHAR(50)	班级名称	
teacher_id	BIGINT	教师ID	外键
start_time	TIME	上课时间	
end_time	TIME	下课时间	
create_time	DATETIME	创建时间	
#### 4.attendance（考勤记录表）
字段名	类型	说明	备注
id	INT	主键	自增
student_id	VARCHAR(20)	学号	
student_name	VARCHAR(50)	学生姓名	
course_id	VARCHAR(20)	课程编号	
course_name	VARCHAR(100)	课程名称	
check_in_time	DATETIME	打卡时间	
status	VARCHAR(20)	状态	NORMAL/LATE
remark	VARCHAR(255)	备注	
create_time	DATETIME	创建时间	
#### 5.task（打卡任务表）
字段名	类型	说明	备注
id	BIGINT	主键	自增
task_code	VARCHAR(20)	任务码	6位数字，唯一
course_id	VARCHAR(20)	课程编号	
course_name	VARCHAR(100)	课程名称	
task_name	VARCHAR(100)	任务名称	
start_time	DATETIME	开始时间	
end_time	DATETIME	结束时间	
status	VARCHAR(20)	状态	ACTIVE/EXPIRED
create_by	VARCHAR(50)	创建人	
#### 6.leave_request（请假申请表）
字段名	类型	说明	备注
id	BIGINT	主键	自增
student_id	VARCHAR(20)	学号	
student_name	VARCHAR(50)	学生姓名	
course_id	VARCHAR(20)	课程编号	
course_name	VARCHAR(100)	课程名称	
leave_date	DATETIME	请假日期	
reason	VARCHAR(500)	请假原因	
leave_type	VARCHAR(20)	类型	ADVANCE/AFTER
status	VARCHAR(20)	状态	PENDING/APPROVED/REJECTED
approve_remark	VARCHAR(200)	审批意见	
#### 7.class_info（班级信息表）
字段名	类型	说明	备注
id	BIGINT	主键	自增
class_name	VARCHAR(50)	班级名称	唯一
grade	VARCHAR(10)	年级	
major	VARCHAR(50)	专业	
create_time	DATETIME	创建时间	
#### 8.course_selection（选课表）
字段名	类型	说明	备注
id	INT	主键	自增
student_id	VARCHAR(20)	学号	
student_name	VARCHAR(50)	学生姓名	
course_id	VARCHAR(20)	课程编号	
select_time	DATETIME	选课时间	
### 项目结构

src/main/java/com/example/attendancesystem/
├── config/                 # 配置类
├── controller/             # 控制器层
├── service/                # 业务逻辑层
├── repository/             # 数据访问层
├── entity/                 # 实体类
├── dto/                    # 数据传输对象
└── util/                   # 工具类

src/main/resources/
├── templates/              # Thymeleaf 模板
├── static/                 # 静态资源
└── application.properties  # 配置文件

### 联系方式
学号：42411020

姓名：李佳蔚

班级：人工智能

邮箱：42411020@swufe.edu.cn

GitHub：https://github.com/xiaolii-maker/attendance-system


