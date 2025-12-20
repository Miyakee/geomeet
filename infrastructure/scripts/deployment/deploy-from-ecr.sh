#!/bin/bash
# GeoMeet 部署脚本 - 从 ECR 拉取预构建镜像
# 最快部署方式：EC2 不需要构建，只需要拉取镜像（几秒钟）

set -e

echo "🚀 开始部署 GeoMeet 应用（从 ECR 拉取预构建镜像）..."

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 配置
AWS_REGION="${AWS_REGION:-ap-southeast-1}"

# 动态获取 AWS Account ID（如果未设置）
if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo "🔍 获取 AWS Account ID..."
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo "")
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        echo -e "${RED}❌ 无法获取 AWS Account ID${NC}"
        echo "请设置 AWS_ACCOUNT_ID 环境变量或配置 AWS CLI"
        exit 1
    fi
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# 部署目录
DEPLOY_DIR="/home/ec2-user/geomeet"
mkdir -p $DEPLOY_DIR

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker 未安装，请先安装 Docker${NC}"
    exit 1
fi

# 检查 Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null 2>&1; then
    echo -e "${RED}❌ Docker Compose 未安装，请先安装 Docker Compose${NC}"
    exit 1
fi

# 检查 AWS CLI
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI 未安装，请先安装 AWS CLI${NC}"
    echo "   sudo yum install aws-cli -y"
    exit 1
fi

# 确定使用哪个 docker-compose 命令
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

echo -e "${YELLOW}📦 步骤 1: 登录到 ECR...${NC}"

# 登录到 ECR（使用 EC2 IAM 角色，无需额外凭证）
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ ECR 登录成功${NC}"
else
    echo -e "${RED}❌ ECR 登录失败${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}📦 步骤 2: 拉取后端镜像...${NC}"
echo "  从 ECR 拉取: $ECR_REGISTRY/geomeet-api:$IMAGE_TAG"

docker pull $ECR_REGISTRY/geomeet-api:$IMAGE_TAG
docker tag $ECR_REGISTRY/geomeet-api:$IMAGE_TAG geomeet-api:latest

echo -e "${GREEN}✅ 后端镜像拉取完成${NC}"
echo ""

echo -e "${YELLOW}📦 步骤 3: 拉取前端镜像...${NC}"
echo "  从 ECR 拉取: $ECR_REGISTRY/geomeet-ui:$IMAGE_TAG"

docker pull $ECR_REGISTRY/geomeet-ui:$IMAGE_TAG
docker tag $ECR_REGISTRY/geomeet-ui:$IMAGE_TAG geomeet-ui:latest

echo -e "${GREEN}✅ 前端镜像拉取完成${NC}"
echo ""

echo -e "${YELLOW}📦 步骤 4: 准备配置文件...${NC}"

# GitHub 配置（使用 SSH）
GITHUB_REPO="${GITHUB_REPO:-git@github.com:Miyakee/geomeet.git}"
GITHUB_BRANCH="${GITHUB_BRANCH:-master}"
PROJECT_DIR="/tmp/geomeet-repo"

# 检查并安装 Git
if ! command -v git &> /dev/null; then
    echo -e "${YELLOW}⚠️  Git 未安装，尝试安装...${NC}"
    if command -v yum &> /dev/null; then
        sudo yum install -y git 2>/dev/null || {
            echo -e "${RED}❌ 无法安装 Git，请手动安装: sudo yum install git -y${NC}"
            exit 1
        }
    elif command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y git 2>/dev/null || {
            echo -e "${RED}❌ 无法安装 Git，请手动安装: sudo apt-get install git -y${NC}"
            exit 1
        }
    else
        echo -e "${RED}❌ 无法自动安装 Git，请手动安装${NC}"
        exit 1
    fi
fi

# 检查 SSH 密钥是否配置
if [ ! -f ~/.ssh/id_rsa ] && [ ! -f ~/.ssh/id_ed25519 ]; then
    echo -e "${YELLOW}⚠️  未检测到 SSH 密钥${NC}"
    echo "  如果这是第一次使用，GitHub 可能会提示添加主机密钥"
    echo "  或者使用 HTTPS 方式（需要访问令牌）"
fi

# 克隆或更新仓库（如果需要）
if [ ! -f "$DEPLOY_DIR/docker-compose.yml" ] || [ ! -f "$DEPLOY_DIR/nginx.conf" ]; then
    echo "配置文件不存在，从 GitHub 克隆仓库..."
    
    if [ ! -d "$PROJECT_DIR" ]; then
        echo "  克隆仓库到 $PROJECT_DIR..."
        # 首次克隆
        if git clone -b "$GITHUB_BRANCH" "$GITHUB_REPO" "$PROJECT_DIR" 2>&1; then
            echo -e "${GREEN}✅ 仓库克隆成功${NC}"
        else
            echo -e "${RED}❌ 仓库克隆失败${NC}"
            echo "  可能的原因："
            echo "    1. SSH 密钥未配置或未添加到 GitHub"
            echo "    2. 仓库 URL 不正确"
            echo "    3. 网络连接问题"
            echo ""
            echo "  尝试使用内联配置作为后备..."
            USE_INLINE_CONFIG=true
        fi
    else
        echo "  更新现有仓库..."
        cd "$PROJECT_DIR" || exit 1
        if git fetch origin 2>&1 && git checkout "$GITHUB_BRANCH" 2>/dev/null && git reset --hard "origin/$GITHUB_BRANCH" 2>&1; then
            echo -e "${GREEN}✅ 仓库更新成功${NC}"
        else
            echo -e "${YELLOW}⚠️  仓库更新失败，使用现有文件${NC}"
        fi
    fi
    
    # 复制配置文件
    if [ "$USE_INLINE_CONFIG" != "true" ] && [ -d "$PROJECT_DIR" ]; then
        # 复制 docker-compose.yml
        if [ -f "$PROJECT_DIR/infrastructure/config/docker-compose.yml" ]; then
            cp "$PROJECT_DIR/infrastructure/config/docker-compose.yml" "$DEPLOY_DIR/"
            echo -e "${GREEN}✅ docker-compose.yml 已复制${NC}"
        else
            echo -e "${YELLOW}⚠️  未找到 docker-compose.yml，使用内联配置...${NC}"
            USE_INLINE_CONFIG=true
        fi
        
        # 复制 nginx.conf
        if [ -f "$PROJECT_DIR/infrastructure/config/nginx.conf" ]; then
            cp "$PROJECT_DIR/infrastructure/config/nginx.conf" "$DEPLOY_DIR/"
            echo -e "${GREEN}✅ nginx.conf 已复制${NC}"
        else
            echo -e "${YELLOW}⚠️  未找到 nginx.conf，使用内联配置...${NC}"
            USE_INLINE_CONFIG=true
        fi
    fi
    
    # 如果克隆失败或文件不存在，使用内联配置
    if [ "$USE_INLINE_CONFIG" = "true" ]; then
        echo ""
        echo -e "${YELLOW}使用内联配置文件...${NC}"
        
        # 创建 docker-compose.yml
        cat > "$DEPLOY_DIR/docker-compose.yml" << 'EOF'
version: '3.8'

services:
  api:
    image: geomeet-api:latest
    container_name: geomeet-api
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/${DB_NAME:-geomeet}
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SERVER_PORT=8080
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - geomeet-network

  nginx:
    image: geomeet-ui:latest
    container_name: geomeet-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - api
    networks:
      - geomeet-network

networks:
  geomeet-network:
    driver: bridge
EOF
        echo -e "${GREEN}✅ docker-compose.yml 已创建（内联配置）${NC}"
        
        # 创建 nginx.conf
        cat > "$DEPLOY_DIR/nginx.conf" << 'EOF'
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss application/rss+xml font/truetype font/opentype application/vnd.ms-fontobject image/svg+xml;

    server {
        listen 80;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location /api {
            proxy_pass http://api:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }

        location /ws {
            proxy_pass http://api:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /health {
            proxy_pass http://api:8080/actuator/health;
            access_log off;
        }
    }
}
EOF
        echo -e "${GREEN}✅ nginx.conf 已创建（内联配置）${NC}"
    fi
else
    echo "✅ 配置文件已存在"
fi

# 验证配置文件存在
if [ ! -f "$DEPLOY_DIR/docker-compose.yml" ]; then
    echo -e "${RED}❌ 错误：docker-compose.yml 不存在${NC}"
    exit 1
fi

if [ ! -f "$DEPLOY_DIR/nginx.conf" ]; then
    echo -e "${RED}❌ 错误：nginx.conf 不存在${NC}"
    exit 1
fi

echo ""

echo -e "${YELLOW}📦 步骤 5: 停止旧服务并清理端口...${NC}"

cd $DEPLOY_DIR

# 停止旧的 Docker 容器
echo "停止旧的 Docker 容器..."
$COMPOSE_CMD down 2>/dev/null || true

docker ps -a --filter "name=geomeet" --format "{{.Names}}" | while read container; do
    if [ -n "$container" ]; then
        echo "  停止容器: $container"
        docker stop "$container" 2>/dev/null || true
        docker rm "$container" 2>/dev/null || true
    fi
done

# 停止系统 Nginx（如果存在）
if systemctl is-active --quiet nginx 2>/dev/null || systemctl is-enabled --quiet nginx 2>/dev/null; then
    echo -e "${YELLOW}⚠️  检测到系统 Nginx 服务，正在停止并禁用...${NC}"
    sudo systemctl stop nginx 2>/dev/null || true
    sudo systemctl disable nginx 2>/dev/null || true
    echo -e "${GREEN}✅ 系统 Nginx 已停止${NC}"
fi

# 等待端口释放
sleep 2

echo ""

echo -e "${YELLOW}📦 步骤 6: 更新 docker-compose.yml 配置...${NC}"

# 更新 docker-compose.yml 使用本地镜像标签（而不是 ECR URL）
# 因为我们已经用 docker tag 创建了本地标签
sed -i.bak 's|image:.*geomeet-api.*|image: geomeet-api:latest|g' $DEPLOY_DIR/docker-compose.yml
sed -i.bak 's|image:.*geomeet-ui.*|image: geomeet-ui:latest|g' $DEPLOY_DIR/docker-compose.yml

# 获取数据库端点（如果可用）
DB_ENDPOINT="${DB_ENDPOINT:-}"
if [ -z "$DB_ENDPOINT" ]; then
    # 尝试从 AWS RDS 获取数据库端点
    echo "尝试获取数据库端点..."
    DB_ENDPOINT=$(aws rds describe-db-instances \
        --db-instance-identifier geomeet-db \
        --region $AWS_REGION \
        --query 'DBInstances[0].Endpoint.Address' \
        --output text 2>/dev/null || echo "")
    
    if [ -n "$DB_ENDPOINT" ] && [ "$DB_ENDPOINT" != "None" ]; then
        echo "✅ 获取到数据库端点: $DB_ENDPOINT"
        # 更新 docker-compose.yml 中的数据库端点
        sed -i.bak "s|geomeet-db\.[^:]*|$DB_ENDPOINT|g" $DEPLOY_DIR/docker-compose.yml
    else
        echo -e "${YELLOW}⚠️  无法自动获取数据库端点，使用默认值${NC}"
        echo "  如需手动设置，请设置环境变量: export DB_ENDPOINT=your-db-endpoint"
    fi
else
    echo "使用环境变量中的数据库端点: $DB_ENDPOINT"
    sed -i.bak "s|geomeet-db\.[^:]*|$DB_ENDPOINT|g" $DEPLOY_DIR/docker-compose.yml
fi

echo -e "${GREEN}✅ docker-compose.yml 已更新${NC}"
echo ""

echo -e "${YELLOW}📦 步骤 7: 启动服务...${NC}"

# 验证 docker-compose.yml 存在
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ 错误：当前目录找不到 docker-compose.yml${NC}"
    echo "  当前目录: $(pwd)"
    exit 1
fi

# 启动服务
$COMPOSE_CMD up -d

# 检查启动状态
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 服务启动成功！${NC}"
else
    echo -e "${RED}❌ 服务启动失败${NC}"
    echo "查看日志: $COMPOSE_CMD logs"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "服务状态："
$COMPOSE_CMD ps

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "查看日志："
echo "  $COMPOSE_CMD logs -f api"
echo "  $COMPOSE_CMD logs -f nginx"
echo ""
echo "访问应用："
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "无法获取")
echo "  http://${PUBLIC_IP}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
