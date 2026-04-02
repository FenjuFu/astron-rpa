# AstronRPA

<div align="center">

![AstronRPA Logo](./docs/images/icon_128px.png)

**🤖 Enterprise-grade Robotic Process Automation (RPA) Development Platform**

[![License](https://img.shields.io/badge/license-Open%20Source-blue.svg)](LICENSE)
[![Python](https://img.shields.io/badge/python-3.13+-blue.svg)](https://www.python.org/)
[![Java](https://img.shields.io/badge/java-8+-orange.svg)](https://openjdk.java.net/)
[![Vue](https://img.shields.io/badge/vue-3+-green.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/docker-ready-brightgreen.svg)](https://www.docker.com/)
[![GitHub Stars](https://img.shields.io/github/stars/iflytek/astron-rpa?style=social)](https://github.com/iflytek/astron-rpa/stargazers)

English | [简体中文](README.zh.md)

</div>

## 📑 Table of Contents

- [📋 Overview](#-overview)
- [🎯 Why Choose AstronRPA](#-why-choose-astronrpa)
- [✨ Core Features](#-core-features)
- [🛠️ Tech Stack](#-tech-stack)
- [📱 Screenshots](#-screenshots)
- [🚀 Quick Start](#-quick-start)
  - [System Requirements](#system-requirements)
  - [Using Docker](#using-docker)
  - [Source Deployment](#source-deployment)
- [📦 Component Ecosystem](#-component-ecosystem)
- [🏗️ Technical Architecture](#-technical-architecture)
- [📚 Documentation](#-documentation)
- [🤝 Contributing](#-contributing)
- [💖 Sponsorship](#-sponsorship)
- [📞 Getting Help](#-getting-help)
- [📄 License](#-license)

## 📋 Overview

AstronRPA is an all-in-one Robotic Process Automation (RPA) development tool that provides enterprises and developers with a complete RPA automation solution from design to deployment. The platform integrates the latest AI language models, rich component libraries, various development modes and frameworks, enabling developers to build powerful automation processes in the most convenient way.

AstronRPA is derived from the "iFlytek RPA Platform" which has served tens of thousands of enterprises and millions of developers, and we have made its core engine completely open source. Through visual design and build tools, developers can quickly create and debug robots, applications, and workflows using no-code or low-code approaches, enabling powerful RPA application development and more customized business logic.

### 🎯 Why Choose AstronRPA?

- **🏭 Production Ready**: Mature platform serving tens of thousands of enterprises
- **👨‍💻 Developer Friendly**: Visual design + comprehensive APIs and documentation
- **☁️ Cloud Native**: Built on microservices architecture with containerization support
- **🔓 Open Source**: Core engine completely open source, community-driven development
- **🤖 AI Powered**: Integrated with DeepSeek and other large language models
- **🧩 Rich Components**: 25+ professional RPA component library

## ✨ Core Features

- 🚀 **High Performance** - Python 3.13+ based high-performance execution engine with distributed support
- 🔒 **Enterprise Security** - Complete permission management, audit logs, and data encryption
- 🔧 **Easy Integration** - Rich API interfaces and SDKs with multi-language support
- 📊 **Real-time Monitoring** - Complete execution status monitoring, performance metrics, and alerting
- 🌍 **Multi-platform Support** - Windows, Linux, and containerized deployment support
- 📈 **Elastic Scaling** - Microservices architecture with horizontal scaling and load balancing

### 🎯 Visual Design
- Drag-and-drop process designer
- Real-time preview and debugging
- Rich component templates
- Smart connection and layout

### 🤖 AI Empowerment
- Intelligent element recognition
- OCR text extraction
- Automatic CAPTCHA recognition
- Natural language process generation

### 🔧 Component-based Development
- 25+ professional RPA components
- Standardized component interfaces
- Custom component extensions
- Component version management

### 📊 Execution Monitoring
- Real-time execution status
- Detailed logging
- Performance metrics statistics
- Exception alert notifications

### 🌐 Multi-platform Support
- Web-based online editing
- Desktop local execution
- Mobile monitoring view
- API interface integration

## 🛠️ Tech Stack

**Frontend**: Vue 3 + TypeScript + Vite + Ant Design Vue
**Backend Services**: Java Spring Boot + Python FastAPI
**Data Storage**: MySQL + Redis
**Message Queue**: Asynchronous task processing support
**Containerization**: Docker + Docker Compose
**Desktop App**: Tauri (Rust + Web)
**Package Management**: pnpm workspace monorepo
**Monitoring**: Integrated SkyWalking distributed tracing

## 📱 Screenshots

<div align="center">
  <img src="https://via.placeholder.com/800x400?text=Visual+Process+Designer" alt="Visual Process Designer" width="45%">
  <img src="https://via.placeholder.com/800x400?text=Execution+Monitoring+Dashboard" alt="Execution Monitoring Dashboard" width="45%">
</div>

## 🏗️ Architecture Overview

![Architecture Overview](./docs/images/Structure.png "Architecture Overview")

### Architecture Details

### Frontend Architecture
- **Framework**: Vue 3 + TypeScript + Vite
- **UI Components**: Ant Design Vue + VXE Table
- **State Management**: Pinia
- **Desktop App**: Tauri (Rust + Web Technologies)
- **Package Management**: pnpm workspace monorepo

### Backend Architecture
- **Main Service**: Java Spring Boot 2.3.11
- **AI Service**: Python FastAPI + DeepSeek Integration
- **OpenAPI Service**: Python FastAPI
- **Resource Service**: Java Spring Boot
- **Database**: MySQL + Redis
- **Message Queue**: Support for asynchronous task processing

### RPA Engine
- **Language**: Python 3.13+
- **Framework**: FastAPI + asyncio
- **Component Architecture**: 25+ professional RPA components
- **Executor**: Support atomic operations, workflows, record & replay
- **Communication**: WebSocket real-time communication
- **Locating Technology**: Image recognition, OCR, UI automation

### Deployment Architecture
- **Containerization**: Docker + Docker Compose
- **Microservices**: Independent service modules, deployable separately
- **Observability**: Integrated SkyWalking distributed tracing
- **Load Balancing**: Nginx reverse proxy

## 🚀 Quick Start

### System Requirements
- **Operating System**: Windows 10/11 (primary support), macOS, Linux
- **Node.js**: >= 22
- **Python**: 3.13.x
- **Java**: JDK 8+
- **pnpm**: >= 9
- **rustc**：>= 1.90.0
- **UV**: Python package management tool
- **7-Zip**: For creating deployment archives

### Using Docker

Recommended for quick deployment:

```bash
# Clone the repository
git clone https://github.com/iflytek/astron-rpa.git
cd astron-rpa

# Enter docker directory
cd docker

# Start the container stack
docker-compose up -d

# Check service status
docker-compose ps
```

- Access the application at `http://localhost:8080`
- For production deployment and security hardening, refer to [Deployment Guide](docker/QUICK_START.md)

### Source Deployment

#### One-Click Launch (Recommended)

1. **Prepare Python Environment**
   ```bash
   # Prepare a Python 3.13.x installation directory
   # Can be a local folder or system installation path
   # The script will copy this directory to create python_base and python_core
   ```

2. **Run Packaging Script**
   ```bash
   # Using local Python313 folder (auto-detected)
   pack.bat
   
   # Specify custom Python directory
   pack.bat "" "C:\Python313"
   pack.bat "" "D:\Python"
   pack.bat "" "Python313"
   
   # With custom 7-Zip path and Python directory
   pack.bat "D:\Tools\7-Zip\7z.exe" "C:\Python313"
   ```

3. **Build Frontend Application**
   ```bash
   cd frontend
   pnpm install
   pnpm build:web
   ```

4. **Build Tauri Application**
   ```bash
   cd frontend
   pnpm install
   pnpm build:tauri 
   ```

#### Development Environment

```bash
# Install dependencies
cd frontend
pnpm install

# Start web development server
pnpm dev:web

# Start Tauri desktop app (development mode)
pnpm dev:tauri

# Start backend services (need to configure database first)
cd backend/robot-service
mvn spring-boot:run
```

## 📦 Component Ecosystem

### Core Component Packages
- **rpasystem**: System operations, process management, screenshots
- **rpabrowser**: Browser automation, web page operations
- **rpagui**: GUI automation, mouse and keyboard operations
- **rpaexcel**: Excel spreadsheet operations, data processing
- **rpacv**: Computer vision, image recognition
- **rpaai**: AI intelligent service integration
- **rpadatabase**: Database connections and operations
- **rpanetwork**: Network requests, API calls
- **rpaemail**: Email sending and receiving
- **rpadocx**: Word document processing
- **rpapdf**: PDF document operations
- **rpaencrypt**: Encryption and decryption functions

### Execution Framework
- **atomic**: Atomic operation definition and execution
- **executor**: Workflow execution engine
- **recording**: Operation recording and playback
- **param_utils**: Parameter processing tools

### Shared Libraries
- **rpaframe**: RPA framework core
- **rpawebsocket**: WebSocket communication
- **locator**: Element locating technology

## 🌟 Core Features

### 🎯 Visual Design
- Drag-and-drop process designer
- Real-time preview and debugging
- Rich component templates
- Smart connection and layout

### 🤖 AI Empowerment
- Intelligent element recognition
- OCR text extraction
- Automatic CAPTCHA recognition
- Natural language process generation

### 🔧 Component-based Development
- 25+ professional RPA components
- Standardized component interfaces
- Custom component extensions
- Component version management

### 📊 Execution Monitoring
- Real-time execution status
- Detailed logging
- Performance metrics statistics
- Exception alert notifications

### 🌐 Multi-platform Support
- Web-based online editing
- Desktop local execution
- Mobile monitoring view
- API interface integration

## ❓ FAQ

### Q: Can RPA be deployed and run in an offline intranet environment? How to import images?

**A:** ✅ **Yes!**
In an offline environment, you need to export the image package using `docker save` in an internet-connected environment, and import it into the intranet using `docker load`. After importing, please check if the image Tag is complete using `docker images`. If an unnamed (dangling) image appears, use `docker tag` to retag it.

### Q: When building the client locally (`build.bat`), do dependencies like `pywinhook`, `psycopg2` fail to install?

**A:**
1. **C++ Build Environment**: Ensure Microsoft Visual C++ 14.0 or higher is installed (including MSVC v143 and Win10/11 SDK).
2. **Permission Issues**: Please run the build script with Administrator privileges.
3. **Dependency Trimming**: If specific database drivers (like Oracle/PostgreSQL) are not needed for your current scenario, you can modify `/engine/components/astronverse-database/pyproject.toml` to remove dependencies like `psycopg2` and `cx-oracle` to bypass complex local compilation.

### Q: Does the open-source version of RPA support frontend-backend separated deployment for secondary development and debugging?

**A:** ✅ **Yes!**
The frontend-backend separated mode is fully supported and recommended in development and debugging scenarios. It allows independent running and debugging of frontend and backend code. Please ensure you pull the latest code for configuration.

## 📚 Documentation

- [📖 User Guide](HOW_TO_RUN.md)
- [🚀 Deployment Guide](docker/QUICK_START.md)
- [📖 API Documentation](backend/openapi-service/api.yaml)
- [🔧 Component Development Guide](engine/components/)
- [🐛 Troubleshooting](docs/TROUBLESHOOTING.md)
- [📝 Changelog](CHANGELOG.md)

## 🤝 Contributing

We welcome any form of contribution! Please check [Contributing Guide](CONTRIBUTING.md)

### Development Guidelines
- Follow existing code style
- Add necessary test cases
- Update relevant documentation
- Ensure all checks pass

### Contributing Steps
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🌟 Star History

<div align="center">
  <img src="https://api.star-history.com/svg?repos=iflytek/astron-rpa&type=Date" alt="Star History Chart" width="600">
</div>

## 💖 Sponsorship

<div align="center">
  <a href="https://github.com/sponsors/iflytek">
    <img src="https://img.shields.io/badge/Sponsor-GitHub%20Sponsors-pink?style=for-the-badge&logo=github" alt="GitHub Sponsors">
  </a>
  <a href="https://opencollective.com/astronrpa">
    <img src="https://img.shields.io/badge/Sponsor-Open%20Collective-blue?style=for-the-badge&logo=opencollective" alt="Open Collective">
  </a>
</div>

## 📞 Getting Help

- 📧 Technical Support: [cbg_rpa_ml@iflytek.com](mailto:cbg_rpa_ml@iflytek.com)
- 💬 Community Discussion: [GitHub Discussions](https://github.com/iflytek/astron-rpa/discussions)
- 🐛 Bug Reports: [Issues](https://github.com/iflytek/astron-rpa/issues)

## 📄 License

This project is open source under the [Open Source License](LICENSE).

---

<div align="center">

**Developed and maintained by iFlytek**

[![Follow](https://img.shields.io/github/followers/iflytek?style=social&label=Follow)](https://github.com/iflytek)
[![Star](https://img.shields.io/github/stars/iflytek/astron-rpa?style=social&label=Star)](https://github.com/iflytek/astron-rpa)
[![Fork](https://img.shields.io/github/forks/iflytek/astron-rpa?style=social&label=Fork)](https://github.com/iflytek/astron-rpa/fork)
[![Watch](https://img.shields.io/github/watchers/iflytek/astron-rpa?style=social&label=Watch)](https://github.com/iflytek/astron-rpa/watchers)

**AstronRPA** - Making RPA development simple and powerful!

If you find this project helpful, please give us a ⭐ Star!

</div>